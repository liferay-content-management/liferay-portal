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
import com.liferay.headless.cms.dto.v1_0.SimilarityClusterResult;
import com.liferay.headless.cms.resource.v1_0.SimilarityClusterResultResource;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.filter.ComplexQueryPartBuilderFactory;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.vulcan.util.GroupUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Mikel Lorza
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/similarity-cluster-result.properties",
	scope = ServiceScope.PROTOTYPE,
	service = SimilarityClusterResultResource.class
)
public class SimilarityClusterResultResourceImpl
	extends BaseSimilarityClusterResultResourceImpl {

	@Override
	public SimilarityClusterResult getSimilarityCluster(
			Long assetLibraryId, String dimension)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-17564")) {

			throw new UnsupportedOperationException();
		}

		String field = _getBandField(dimension);

		List<ObjectDefinition> objectDefinitions = _getCMSObjectDefinitions();

		Long[] groupIds = _getGroupIds(assetLibraryId);

		if ((field == null) || ArrayUtil.isEmpty(groupIds) ||
			objectDefinitions.isEmpty()) {

			return _toSimilarityClusterResult(
				new ArrayList<>(), new HashMap<>(), new HashMap<>());
		}

		String[] entryClassNames = ArrayUtil.toStringArray(
			ListUtil.toList(objectDefinitions, ObjectDefinition::getClassName));

		List<List<Long>> clusters = _getClusters(
			_search(field, groupIds, entryClassNames));

		Map<Long, ObjectDefinition> objectDefinitionsMap = new HashMap<>();

		for (ObjectDefinition objectDefinition : objectDefinitions) {
			objectDefinitionsMap.put(
				objectDefinition.getObjectDefinitionId(), objectDefinition);
		}

		return _toSimilarityClusterResult(
			clusters, objectDefinitionsMap,
			_getSignatures(clusters, groupIds, entryClassNames));
	}

	private Long _find(Map<Long, Long> parents, Long objectEntryId) {
		Long parent = parents.get(objectEntryId);

		while (!parent.equals(objectEntryId)) {
			objectEntryId = parent;

			parent = parents.get(objectEntryId);
		}

		return objectEntryId;
	}

	private String _getBandField(String dimension) {
		if ((dimension == null) || dimension.equals("TEXT")) {
			return "textSimilarityBands";
		}

		// Other dimensions (TITLE, METADATA) require their own indexed
		// signature fields, which do not exist yet.

		return null;
	}

	private List<List<Long>> _getClusters(SearchResponse searchResponse) {
		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)searchResponse.getAggregationResult(
				_BANDS_AGGREGATION_NAME);

		if (termsAggregationResult == null) {
			return new ArrayList<>();
		}

		Map<Long, Long> parents = new LinkedHashMap<>();

		for (Bucket bucket : termsAggregationResult.getBuckets()) {
			TermsAggregationResult objectEntryIdsTermsAggregationResult =
				(TermsAggregationResult)bucket.getChildAggregationResult(
					_OBJECT_ENTRY_IDS_AGGREGATION_NAME);

			if (objectEntryIdsTermsAggregationResult == null) {
				continue;
			}

			Long representativeObjectEntryId = null;

			for (Bucket objectEntryIdBucket :
					objectEntryIdsTermsAggregationResult.getBuckets()) {

				long objectEntryId = GetterUtil.getLong(
					objectEntryIdBucket.getKey());

				parents.putIfAbsent(objectEntryId, objectEntryId);

				if (representativeObjectEntryId == null) {
					representativeObjectEntryId = objectEntryId;
				}
				else {
					_union(parents, representativeObjectEntryId, objectEntryId);
				}
			}
		}

		Map<Long, List<Long>> clusters = new LinkedHashMap<>();

		for (Long objectEntryId : parents.keySet()) {
			Long root = _find(parents, objectEntryId);

			List<Long> cluster = clusters.computeIfAbsent(
				root, key -> new ArrayList<>());

			cluster.add(objectEntryId);
		}

		List<List<Long>> similarityClusters = new ArrayList<>();

		for (List<Long> cluster : clusters.values()) {
			if (cluster.size() >= 2) {
				similarityClusters.add(cluster);
			}
		}

		return similarityClusters;
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

	private Map<Long, long[]> _getSignatures(
		List<List<Long>> clusters, Long[] groupIds, String[] entryClassNames) {

		List<String> objectEntryIds = new ArrayList<>();

		for (List<Long> cluster : clusters) {
			for (Long objectEntryId : cluster) {
				objectEntryIds.add(String.valueOf(objectEntryId));
			}
		}

		if (objectEntryIds.isEmpty()) {
			return new HashMap<>();
		}

		TermsQuery termsQuery = QueriesUtil.terms("objectEntryId");

		termsQuery.addValues(objectEntryIds.toArray());

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder();

		long[] scopedGroupIds = _toPrimitiveArray(groupIds);

		searchRequestBuilder.addComplexQueryPart(
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
		).size(
			objectEntryIds.size()
		).withSearchContext(
			searchContext -> {
				searchContext.setAttribute(
					Field.STATUS, WorkflowConstants.STATUS_APPROVED);
				searchContext.setGroupIds(scopedGroupIds);
			}
		);

		SearchResponse searchResponse = _searcher.search(
			searchRequestBuilder.build());

		Map<Long, long[]> signaturesMap = new HashMap<>();

		SearchHits searchHits = searchResponse.getSearchHits();

		for (SearchHit searchHit : searchHits.getSearchHits()) {
			Document document = searchHit.getDocument();

			Long objectEntryId = document.getLong("objectEntryId");

			if (objectEntryId == null) {
				continue;
			}

			long[] signature = _parseSignature(
				document.getStrings("textSimilaritySignature"));

			if (signature != null) {
				signaturesMap.put(objectEntryId, signature);
			}
		}

		return signaturesMap;
	}

	private double _getSimilarity(long[] signature1, long[] signature2) {
		int matches = 0;

		for (int i = 0; i < _SIGNATURE_SIZE; i++) {
			if (signature1[i] == signature2[i]) {
				matches++;
			}
		}

		return (double)matches / _SIGNATURE_SIZE;
	}

	private Long _getTopObjectEntryId(
		List<Long> cluster, Map<Long, long[]> signaturesMap) {

		Long topObjectEntryId = null;

		double topMeanSimilarity = -1;

		for (Long objectEntryId : cluster) {
			long[] signature = signaturesMap.get(objectEntryId);

			if (signature == null) {
				continue;
			}

			double totalSimilarity = 0;
			int count = 0;

			for (Long otherObjectEntryId : cluster) {
				if (otherObjectEntryId.equals(objectEntryId)) {
					continue;
				}

				long[] otherSignature = signaturesMap.get(otherObjectEntryId);

				if (otherSignature == null) {
					continue;
				}

				totalSimilarity += _getSimilarity(signature, otherSignature);
				count++;
			}

			double meanSimilarity = 0;

			if (count > 0) {
				meanSimilarity = totalSimilarity / count;
			}

			if (meanSimilarity > topMeanSimilarity) {
				topMeanSimilarity = meanSimilarity;
				topObjectEntryId = objectEntryId;
			}
		}

		return topObjectEntryId;
	}

	private long[] _parseSignature(List<String> tokens) {
		if ((tokens == null) || (tokens.size() != _SIGNATURE_SIZE)) {
			return null;
		}

		long[] signature = new long[_SIGNATURE_SIZE];
		boolean[] filled = new boolean[_SIGNATURE_SIZE];
		int count = 0;

		for (String token : tokens) {
			int index = token.indexOf('_');

			if ((index <= 1) || (token.charAt(0) != 'p')) {
				continue;
			}

			int position = GetterUtil.getInteger(token.substring(1, index));

			if ((position < 0) || (position >= _SIGNATURE_SIZE) ||
				filled[position]) {

				continue;
			}

			signature[position] = GetterUtil.getLong(
				token.substring(index + 1));
			filled[position] = true;

			count++;
		}

		if (count != _SIGNATURE_SIZE) {
			return null;
		}

		return signature;
	}

	private SearchResponse _search(
		String field, Long[] groupIds, String[] entryClassNames) {

		TermsAggregation termsAggregation = _aggregations.terms(
			_BANDS_AGGREGATION_NAME, field);

		termsAggregation.setMinDocCount(2);
		termsAggregation.setSize(_MAX_BANDS);

		TermsAggregation objectEntryIdsTermsAggregation = _aggregations.terms(
			_OBJECT_ENTRY_IDS_AGGREGATION_NAME, "objectEntryId");

		objectEntryIdsTermsAggregation.setSize(_MAX_CLUSTER_SIZE);

		termsAggregation.addChildAggregation(objectEntryIdsTermsAggregation);

		SearchRequestBuilder searchRequestBuilder =
			_searchRequestBuilderFactory.builder();

		long[] scopedGroupIds = _toPrimitiveArray(groupIds);

		searchRequestBuilder.addAggregation(
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
			searchContext -> {
				searchContext.setAttribute(
					Field.STATUS, WorkflowConstants.STATUS_APPROVED);
				searchContext.setGroupIds(scopedGroupIds);
			}
		);

		return _searcher.search(searchRequestBuilder.build());
	}

	private long[] _toPrimitiveArray(Long[] groupIds) {
		long[] scopedGroupIds = new long[groupIds.length];

		for (int i = 0; i < groupIds.length; i++) {
			scopedGroupIds[i] = groupIds[i];
		}

		return scopedGroupIds;
	}

	private SimilarityClusterResult _toSimilarityClusterResult(
			List<List<Long>> clusters,
			Map<Long, ObjectDefinition> objectDefinitionsMap,
			Map<Long, long[]> signaturesMap)
		throws Exception {

		List<SimilarityCluster> similarityClusters = new ArrayList<>();

		long totalCount = 0;

		String languageId = contextAcceptLanguage.getPreferredLanguageId();

		for (List<Long> cluster : clusters) {
			Long topObjectEntryId = _getTopObjectEntryId(
				cluster, signaturesMap);

			long[] topSignature = null;

			if (topObjectEntryId != null) {
				topSignature = signaturesMap.get(topObjectEntryId);
			}

			List<SimilarityClusterAsset> similarityClusterAssets =
				new ArrayList<>();

			for (Long objectEntryId : cluster) {
				SimilarityClusterAsset similarityClusterAsset =
					new SimilarityClusterAsset();

				similarityClusterAsset.setId(() -> objectEntryId);

				ObjectEntry objectEntry =
					_objectEntryLocalService.fetchObjectEntry(objectEntryId);

				if (objectEntry != null) {
					similarityClusterAsset.setDateModified(
						objectEntry::getModifiedDate);
					similarityClusterAsset.setTitle(
						() -> objectEntry.getTitleValue(languageId, true));

					ObjectDefinition objectDefinition =
						objectDefinitionsMap.get(
							objectEntry.getObjectDefinitionId());

					if (objectDefinition != null) {
						similarityClusterAsset.setContentType(
							() -> objectDefinition.getLabel(languageId, true));
					}
				}

				boolean topAsset = objectEntryId.equals(topObjectEntryId);

				similarityClusterAsset.setTopAsset(() -> topAsset);

				if (!topAsset && (topSignature != null)) {
					long[] signature = signaturesMap.get(objectEntryId);

					if (signature != null) {
						double similarityPercent = Math.round(
							_getSimilarity(signature, topSignature) * 100.0);

						similarityClusterAsset.setSimilarityPercent(
							() -> similarityPercent);
					}
				}

				similarityClusterAssets.add(similarityClusterAsset);
			}

			SimilarityClusterAsset[] similarityClusterAssetsArray =
				similarityClusterAssets.toArray(new SimilarityClusterAsset[0]);

			SimilarityCluster similarityCluster = new SimilarityCluster();

			similarityCluster.setSimilarityClusterAssets(
				() -> similarityClusterAssetsArray);
			similarityCluster.setSize(
				() -> similarityClusterAssetsArray.length);

			similarityClusters.add(similarityCluster);

			totalCount += cluster.size();
		}

		SimilarityCluster[] similarityClustersArray =
			similarityClusters.toArray(new SimilarityCluster[0]);

		long totalCountValue = totalCount;

		SimilarityClusterResult similarityClusterResult =
			new SimilarityClusterResult();

		similarityClusterResult.setSimilarityClusters(
			() -> similarityClustersArray);
		similarityClusterResult.setTotalCount(() -> totalCountValue);

		return similarityClusterResult;
	}

	private void _union(
		Map<Long, Long> parents, Long objectEntryId1, Long objectEntryId2) {

		Long root1 = _find(parents, objectEntryId1);
		Long root2 = _find(parents, objectEntryId2);

		if (!root1.equals(root2)) {
			parents.put(root1, root2);
		}
	}

	private static final String _BANDS_AGGREGATION_NAME = "bands";

	private static final int _MAX_BANDS = 10000;

	private static final int _MAX_CLUSTER_SIZE = 1000;

	private static final String _OBJECT_ENTRY_IDS_AGGREGATION_NAME =
		"objectEntryIds";

	private static final int _SIGNATURE_SIZE = 128;

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

}