/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.internal.resource.v1_0;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.depot.service.DepotEntryService;
import com.liferay.headless.cms.dto.v1_0.SimilarityCluster;
import com.liferay.headless.cms.dto.v1_0.SimilarityClusterAsset;
import com.liferay.headless.cms.resource.v1_0.SimilarityClusterResource;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.IncludeExcludeClause;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.GroupUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Mikel Lorza
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/similarity-cluster.properties",
	scope = ServiceScope.PROTOTYPE, service = SimilarityClusterResource.class
)
public class SimilarityClusterResourceImpl
	extends BaseSimilarityClusterResourceImpl {

	@Override
	public Page<SimilarityCluster> getSimilarityClustersPage(
			Long assetLibraryId, Pagination pagination)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-82226")) {

			return Page.of(new ArrayList<>(), pagination, 0);
		}

		List<ObjectDefinition> objectDefinitions = _getCMSObjectDefinitions();

		Long[] groupIds = _getGroupIds(assetLibraryId);

		if (ArrayUtil.isEmpty(groupIds) || objectDefinitions.isEmpty()) {
			return Page.of(new ArrayList<>(), pagination, 0);
		}

		String[] entryClassNames = ArrayUtil.toStringArray(
			ListUtil.toList(objectDefinitions, ObjectDefinition::getClassName));
		String languageId = contextAcceptLanguage.getPreferredLanguageId();

		List<String> sharedSimilarityKeys = _searchSharedSimilarityKeys(
			entryClassNames, groupIds, languageId);

		Map<Long, List<Long>> objectEntryIdsMap = _getObjectEntryIdsMap(
			_searchClusteredDocuments(
				entryClassNames, groupIds, sharedSimilarityKeys),
			new HashSet<>(sharedSimilarityKeys));

		long totalCount = 0;

		for (List<Long> objectEntryIds : objectEntryIdsMap.values()) {
			totalCount += objectEntryIds.size();
		}

		Map<Long, ObjectDefinition> objectDefinitionsMap = new HashMap<>();

		for (ObjectDefinition objectDefinition : objectDefinitions) {
			objectDefinitionsMap.put(
				objectDefinition.getObjectDefinitionId(), objectDefinition);
		}

		return Page.of(
			_getSimilarityClusters(
				languageId, objectDefinitionsMap, objectEntryIdsMap,
				pagination),
			pagination, totalCount);
	}

	private List<ObjectDefinition> _getCMSObjectDefinitions() throws Exception {
		return _objectDefinitionService.getCMSObjectDefinitions(
			contextCompany.getCompanyId(),
			new String[] {
				ObjectFolderConstants.
					EXTERNAL_REFERENCE_CODE_CONTENT_STRUCTURES,
				ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_FILE_TYPES
			});
	}

	private Map<Long, List<Long>> _getGroupedObjectEntryIdsMap(
		Map<Long, Long> parentObjectEntryIds) {

		Map<Long, List<Long>> objectEntryIdsMap = new LinkedHashMap<>();

		for (Long objectEntryId : parentObjectEntryIds.keySet()) {
			List<Long> rootObjectEntryIds = objectEntryIdsMap.computeIfAbsent(
				_getRootObjectEntryId(objectEntryId, parentObjectEntryIds),
				rootObjectEntryId -> new ArrayList<>());

			rootObjectEntryIds.add(objectEntryId);
		}

		Map<Long, List<Long>> groupedObjectEntryIdsMap = new HashMap<>();

		for (List<Long> objectEntryIds : objectEntryIdsMap.values()) {
			if (objectEntryIds.size() < 2) {
				continue;
			}

			Collections.sort(objectEntryIds);

			groupedObjectEntryIdsMap.put(objectEntryIds.get(0), objectEntryIds);
		}

		return _getSortedObjectEntryIdsMap(groupedObjectEntryIdsMap);
	}

	private Long[] _getGroupIds(Long assetLibraryId) {
		List<Long> depotEntryGroupIds =
			_depotEntryService.getDepotEntryGroupIds(
				contextCompany.getCompanyId(), contextUser.getUserId(),
				DepotConstants.TYPE_SPACE);

		if (assetLibraryId == null) {
			return depotEntryGroupIds.toArray(new Long[0]);
		}

		Long groupId = GroupUtil.getDepotGroupId(
			String.valueOf(assetLibraryId), contextCompany.getCompanyId(),
			_depotEntryLocalService, groupLocalService);

		if ((groupId == null) || !depotEntryGroupIds.contains(groupId)) {
			return new Long[0];
		}

		return new Long[] {groupId};
	}

	private Map<Long, List<Long>> _getObjectEntryIdsMap(
		List<Document> documents, Set<String> sharedSimilarityKeys) {

		Map<Long, Long> parentObjectEntryIds = new LinkedHashMap<>();
		Map<String, List<Long>> objectEntryIdsMap = new HashMap<>();

		for (Document document : documents) {
			Long objectEntryId = document.getLong("objectEntryId");

			if (objectEntryId == null) {
				continue;
			}

			parentObjectEntryIds.putIfAbsent(objectEntryId, objectEntryId);

			for (String similarityKey :
					document.getStrings("textSimilarityKeys")) {

				if (!sharedSimilarityKeys.contains(similarityKey)) {
					continue;
				}

				List<Long> similarityKeyObjectEntryIds =
					objectEntryIdsMap.computeIfAbsent(
						similarityKey, key -> new ArrayList<>());

				similarityKeyObjectEntryIds.add(objectEntryId);
			}
		}

		_mergeDuplicateObjectEntryIds(objectEntryIdsMap, parentObjectEntryIds);

		return _getGroupedObjectEntryIdsMap(parentObjectEntryIds);
	}

	private Long _getRootObjectEntryId(
		Long objectEntryId, Map<Long, Long> parentObjectEntryIds) {

		Long rootObjectEntryId = objectEntryId;

		Long parentObjectEntryId = parentObjectEntryIds.get(rootObjectEntryId);

		while (!parentObjectEntryId.equals(rootObjectEntryId)) {
			rootObjectEntryId = parentObjectEntryId;

			parentObjectEntryId = parentObjectEntryIds.get(rootObjectEntryId);
		}

		while (!objectEntryId.equals(rootObjectEntryId)) {
			objectEntryId = parentObjectEntryIds.put(
				objectEntryId, rootObjectEntryId);
		}

		return rootObjectEntryId;
	}

	private Consumer<SearchContext> _getSearchContextConsumer(Long[] groupIds) {
		long[] scopedGroupIds = ArrayUtil.toArray(groupIds);

		return searchContext -> {
			searchContext.setAttribute(
				Field.STATUS, WorkflowConstants.STATUS_APPROVED);
			searchContext.setGroupIds(scopedGroupIds);

			searchContext.setUserId(contextUser.getUserId());
		};
	}

	private List<SimilarityCluster> _getSimilarityClusters(
			String languageId, Map<Long, ObjectDefinition> objectDefinitionsMap,
			Map<Long, List<Long>> objectEntryIdsMap, Pagination pagination)
		throws Exception {

		List<SimilarityCluster> similarityClusters = new ArrayList<>();

		int endPosition = -1;
		int startPosition = -1;

		if (pagination != null) {
			endPosition = pagination.getEndPosition();
			startPosition = pagination.getStartPosition();
		}

		int position = 0;

		for (List<Long> objectEntryIds : objectEntryIdsMap.values()) {
			int clusterStartPosition = position;

			position += objectEntryIds.size();

			List<Long> pageObjectEntryIds = objectEntryIds;

			if ((endPosition >= 0) && (startPosition >= 0)) {
				if (position <= startPosition) {
					continue;
				}

				if (clusterStartPosition >= endPosition) {
					break;
				}

				pageObjectEntryIds = objectEntryIds.subList(
					Math.max(startPosition - clusterStartPosition, 0),
					Math.min(
						endPosition - clusterStartPosition,
						objectEntryIds.size()));
			}

			similarityClusters.add(
				_toSimilarityCluster(
					languageId, objectDefinitionsMap, pageObjectEntryIds,
					objectEntryIds.size()));
		}

		return similarityClusters;
	}

	private Map<Long, List<Long>> _getSortedObjectEntryIdsMap(
		Map<Long, List<Long>> objectEntryIdsMap) {

		List<Long> lowestObjectEntryIds = ListUtil.fromMapKeys(
			objectEntryIdsMap);

		lowestObjectEntryIds.sort(
			Comparator.comparingInt(
				(Long lowestObjectEntryId) -> {
					List<Long> objectEntryIds = objectEntryIdsMap.get(
						lowestObjectEntryId);

					return objectEntryIds.size();
				}
			).reversed(
			).thenComparing(
				Comparator.naturalOrder()
			));

		Map<Long, List<Long>> sortedObjectEntryIdsMap = new LinkedHashMap<>();

		for (Long lowestObjectEntryId : lowestObjectEntryIds) {
			sortedObjectEntryIdsMap.put(
				lowestObjectEntryId,
				objectEntryIdsMap.get(lowestObjectEntryId));
		}

		return sortedObjectEntryIdsMap;
	}

	private void _mergeDuplicateObjectEntryIds(
		Map<String, List<Long>> objectEntryIdsMap,
		Map<Long, Long> parentObjectEntryIds) {

		Map<Long, List<String>> similarityKeysMap = new LinkedHashMap<>();
		Map<Long, Map<Long, Integer>> sharedSimilarityKeyCounts =
			new HashMap<>();

		for (Map.Entry<String, List<Long>> entry :
				objectEntryIdsMap.entrySet()) {

			List<Long> objectEntryIds = entry.getValue();

			if (objectEntryIds.size() > _MAX_SIMILARITY_KEY_ASSETS) {
				for (Long objectEntryId : objectEntryIds) {
					List<String> similarityKeys =
						similarityKeysMap.computeIfAbsent(
							objectEntryId, key -> new ArrayList<>());

					similarityKeys.add(entry.getKey());
				}

				continue;
			}

			for (int i = 0; i < objectEntryIds.size(); i++) {
				for (int j = i + 1; j < objectEntryIds.size(); j++) {
					Long objectEntryId1 = objectEntryIds.get(i);
					Long objectEntryId2 = objectEntryIds.get(j);

					Long rootObjectEntryId1 = _getRootObjectEntryId(
						objectEntryId1, parentObjectEntryIds);
					Long rootObjectEntryId2 = _getRootObjectEntryId(
						objectEntryId2, parentObjectEntryIds);

					if (rootObjectEntryId1.equals(rootObjectEntryId2)) {
						continue;
					}

					Map<Long, Integer> counts =
						sharedSimilarityKeyCounts.computeIfAbsent(
							Math.min(objectEntryId1, objectEntryId2),
							objectEntryId -> new HashMap<>());

					int count = counts.merge(
						Math.max(objectEntryId1, objectEntryId2), 1,
						Integer::sum);

					if (count >= _MIN_SHARED_SIMILARITY_KEYS) {
						_mergeObjectEntryIds(
							objectEntryId1, objectEntryId2,
							parentObjectEntryIds);
					}
				}
			}
		}

		_mergeObjectEntryIds(similarityKeysMap, parentObjectEntryIds);
	}

	private void _mergeObjectEntryIds(
		Long objectEntryId1, Long objectEntryId2,
		Map<Long, Long> parentObjectEntryIds) {

		Long rootObjectEntryId1 = _getRootObjectEntryId(
			objectEntryId1, parentObjectEntryIds);
		Long rootObjectEntryId2 = _getRootObjectEntryId(
			objectEntryId2, parentObjectEntryIds);

		if (!rootObjectEntryId1.equals(rootObjectEntryId2)) {
			parentObjectEntryIds.put(rootObjectEntryId1, rootObjectEntryId2);
		}
	}

	private void _mergeObjectEntryIds(
		Map<Long, List<String>> similarityKeysMap,
		Map<Long, Long> parentObjectEntryIds) {

		Map<String, Long> objectEntryIdsMap = new HashMap<>();

		for (Map.Entry<Long, List<String>> entry :
				similarityKeysMap.entrySet()) {

			List<String> similarityKeys = entry.getValue();

			if (similarityKeys.size() < _MIN_SHARED_SIMILARITY_KEYS) {
				continue;
			}

			Long objectEntryId = objectEntryIdsMap.putIfAbsent(
				StringUtil.merge(similarityKeys), entry.getKey());

			if (objectEntryId != null) {
				_mergeObjectEntryIds(
					objectEntryId, entry.getKey(), parentObjectEntryIds);
			}
		}
	}

	private List<Document> _searchClusteredDocuments(
		String[] entryClassNames, Long[] groupIds,
		List<String> sharedSimilarityKeys) {

		List<Document> documents = new ArrayList<>();

		if (sharedSimilarityKeys.isEmpty()) {
			return documents;
		}

		TermsQuery termsQuery = QueriesUtil.terms("textSimilarityKeys");

		termsQuery.addValues(sharedSimilarityKeys.toArray());

		SearchResponse searchResponse = _searcher.search(
			_searchRequestBuilderFactory.builder(
			).addComplexQueryPart(
				_complexQueryPartBuilderFactory.builder(
				).occur(
					"must"
				).query(
					termsQuery
				).build()
			).companyId(
				contextCompany.getCompanyId()
			).emptySearchEnabled(
				true
			).entryClassNames(
				entryClassNames
			).fetchSourceIncludes(
				new String[] {"objectEntryId", "textSimilarityKeys"}
			).size(
				_MAX_CLUSTERED_DOCUMENTS
			).withSearchContext(
				_getSearchContextConsumer(groupIds)
			).build());

		SearchHits searchHits = searchResponse.getSearchHits();

		for (SearchHit searchHit : searchHits.getSearchHits()) {
			documents.add(searchHit.getDocument());
		}

		return documents;
	}

	private List<String> _searchSharedSimilarityKeys(
		String[] entryClassNames, Long[] groupIds, String languageId) {

		List<String> sharedSimilarityKeys = new ArrayList<>();

		TermsAggregation termsAggregation = _aggregations.terms(
			_SIMILARITY_KEYS_AGGREGATION_NAME, "textSimilarityKeys");

		termsAggregation.setMinDocCount(2);
		termsAggregation.setIncludeExcludeClause(
			new IncludeExcludeClauseImpl(languageId + "_.*", null));
		termsAggregation.setSize(_MAX_SIMILARITY_KEYS);

		SearchResponse searchResponse = _searcher.search(
			_searchRequestBuilderFactory.builder(
			).addAggregation(
				termsAggregation
			).companyId(
				contextCompany.getCompanyId()
			).emptySearchEnabled(
				true
			).entryClassNames(
				entryClassNames
			).size(
				0
			).withSearchContext(
				_getSearchContextConsumer(groupIds)
			).build());

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)searchResponse.getAggregationResult(
				_SIMILARITY_KEYS_AGGREGATION_NAME);

		if (termsAggregationResult == null) {
			return sharedSimilarityKeys;
		}

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			sharedSimilarityKeys.add(bucket.getKey());
		}

		return sharedSimilarityKeys;
	}

	private SimilarityCluster _toSimilarityCluster(
			String languageId, Map<Long, ObjectDefinition> objectDefinitionsMap,
			List<Long> objectEntryIds, int size)
		throws Exception {

		SimilarityCluster similarityCluster = new SimilarityCluster();

		SimilarityClusterAsset[] similarityClusterAssets = transformToArray(
			objectEntryIds,
			objectEntryId -> {
				ObjectEntry objectEntry =
					_objectEntryLocalService.fetchObjectEntry(objectEntryId);

				if (objectEntry == null) {
					return null;
				}

				SimilarityClusterAsset similarityClusterAsset =
					new SimilarityClusterAsset();

				similarityClusterAsset.setDateModified(
					objectEntry::getModifiedDate);
				similarityClusterAsset.setId(() -> objectEntryId);
				similarityClusterAsset.setTitle(
					() -> objectEntry.getTitleValue(languageId, true));

				ObjectDefinition objectDefinition = objectDefinitionsMap.get(
					objectEntry.getObjectDefinitionId());

				if (objectDefinition != null) {
					similarityClusterAsset.setContentType(
						() -> objectDefinition.getLabel(languageId, true));
				}

				return similarityClusterAsset;
			},
			SimilarityClusterAsset.class);

		similarityCluster.setSimilarityClusterAssets(
			() -> similarityClusterAssets);

		similarityCluster.setSize(() -> size);

		return similarityCluster;
	}

	private static final int _MAX_CLUSTERED_DOCUMENTS = 10000;

	private static final int _MAX_SIMILARITY_KEY_ASSETS = 500;

	private static final int _MAX_SIMILARITY_KEYS = 10000;

	private static final int _MIN_SHARED_SIMILARITY_KEYS = 3;

	private static final String _SIMILARITY_KEYS_AGGREGATION_NAME =
		"similarityKeys";

	@Reference
	private Aggregations _aggregations;

	@Reference
	private ComplexQueryPartBuilderFactory _complexQueryPartBuilderFactory;

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private DepotEntryService _depotEntryService;

	@Reference
	private ObjectDefinitionService _objectDefinitionService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	private static class IncludeExcludeClauseImpl
		implements IncludeExcludeClause {

		public IncludeExcludeClauseImpl(
			String includeRegex, String excludeRegex) {

			_includeRegex = includeRegex;
			_excludeRegex = excludeRegex;
		}

		@Override
		public String[] getExcludedValues() {
			return null;
		}

		@Override
		public String getExcludeRegex() {
			return _excludeRegex;
		}

		@Override
		public String[] getIncludedValues() {
			return null;
		}

		@Override
		public String getIncludeRegex() {
			return _includeRegex;
		}

		private final String _excludeRegex;
		private final String _includeRegex;

	}

}