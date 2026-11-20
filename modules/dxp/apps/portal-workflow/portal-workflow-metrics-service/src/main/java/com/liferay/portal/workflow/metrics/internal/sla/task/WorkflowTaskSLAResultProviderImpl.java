/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.internal.sla.task;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.capabilities.SearchCapabilities;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;
import com.liferay.portal.workflow.metrics.search.index.constants.WorkflowMetricsIndexNameConstants;
import com.liferay.portal.workflow.metrics.service.WorkflowMetricsSLADefinitionLocalService;
import com.liferay.portal.workflow.metrics.sla.processor.WorkflowMetricsSLAStatus;
import com.liferay.portal.workflow.metrics.sla.task.WorkflowTaskSLAResult;
import com.liferay.portal.workflow.metrics.sla.task.WorkflowTaskSLAResultProvider;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(service = WorkflowTaskSLAResultProvider.class)
public class WorkflowTaskSLAResultProviderImpl
	implements WorkflowTaskSLAResultProvider {

	@Override
	public List<WorkflowTaskSLAResult> getWorkflowTaskSLAResults(
		long workflowTaskId) {

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			_kaleoTaskInstanceTokenLocalService.fetchKaleoTaskInstanceToken(
				workflowTaskId);

		if (kaleoTaskInstanceToken == null) {
			return Collections.emptyList();
		}

		try {
			return _getWorkflowTaskSLAResults(kaleoTaskInstanceToken);
		}
		catch (Exception exception) {
			_log.error(exception);

			return Collections.emptyList();
		}
	}

	private Map<String, Date> _getOverdueDates(
		long companyId, String indexName, long instanceId,
		List<String> slaDefinitionIds) {

		Map<String, Date> overdueDates = new HashMap<>();

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(
			indexName +
				WorkflowMetricsIndexNameConstants.SUFFIX_SLA_INSTANCE_RESULT);

		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		TermsQuery termsQuery = QueriesUtil.terms("slaDefinitionId");

		termsQuery.addValues(slaDefinitionIds.toArray(new String[0]));

		booleanQuery.addFilterQueryClauses(
			QueriesUtil.term("active", true),
			QueriesUtil.term("blocked", false),
			QueriesUtil.term("companyId", companyId),
			QueriesUtil.term("deleted", false),
			QueriesUtil.term("instanceId", instanceId), termsQuery);

		searchSearchRequest.setQuery(booleanQuery);

		searchSearchRequest.setSelectedFieldNames(
			"overdueDate", "slaDefinitionId");
		searchSearchRequest.setSize(slaDefinitionIds.size());

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		for (SearchHit searchHit : searchHits.getSearchHits()) {
			Document document = searchHit.getDocument();

			LocalDateTime overdueLocalDateTime = LocalDateTime.parse(
				document.getString("overdueDate"), _dateTimeFormatter);

			overdueDates.put(
				document.getString("slaDefinitionId"),
				Date.from(
					overdueLocalDateTime.atZone(
						ZoneId.systemDefault()
					).toInstant()));
		}

		return overdueDates;
	}

	private List<WorkflowTaskSLAResult> _getWorkflowTaskSLAResults(
		KaleoTaskInstanceToken kaleoTaskInstanceToken) {

		if (!_searchCapabilities.isWorkflowMetricsSupported()) {
			return Collections.emptyList();
		}

		int workflowMetricsSLADefinitionsCount =
			_workflowMetricsSLADefinitionLocalService.
				getWorkflowMetricsSLADefinitionsCount(
					kaleoTaskInstanceToken.getCompanyId(), true,
					kaleoTaskInstanceToken.getKaleoDefinitionId());

		if (workflowMetricsSLADefinitionsCount == 0) {
			return Collections.emptyList();
		}

		String indexName = _indexNameBuilder.getIndexName(
			kaleoTaskInstanceToken.getCompanyId());

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(
			indexName +
				WorkflowMetricsIndexNameConstants.SUFFIX_SLA_TASK_RESULT);

		BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

		booleanQuery.addFilterQueryClauses(
			QueriesUtil.term(
				"companyId", kaleoTaskInstanceToken.getCompanyId()),
			QueriesUtil.term("deleted", false),
			QueriesUtil.term(
				"taskId",
				kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId()));
		booleanQuery.addMustNotQueryClauses(
			QueriesUtil.term("slaDefinitionId", 0),
			QueriesUtil.term("status", WorkflowMetricsSLAStatus.NEW.name()));

		searchSearchRequest.setQuery(booleanQuery);

		searchSearchRequest.setSelectedFieldNames(
			"breached", "slaDefinitionId");
		searchSearchRequest.setSize(workflowMetricsSLADefinitionsCount);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		SearchHits searchHits = searchSearchResponse.getSearchHits();

		List<SearchHit> searchHitsList = searchHits.getSearchHits();

		if (searchHitsList.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, Date> overdueDates = _getOverdueDates(
			kaleoTaskInstanceToken.getCompanyId(), indexName,
			kaleoTaskInstanceToken.getKaleoInstanceId(),
			TransformUtil.transform(
				searchHitsList,
				searchHit -> {
					Document document = searchHit.getDocument();

					return document.getString("slaDefinitionId");
				}));

		List<WorkflowTaskSLAResult> workflowTaskSLAResults =
			TransformUtil.transform(
				searchHitsList,
				searchHit -> {
					Document document = searchHit.getDocument();

					Date overdueDate = overdueDates.get(
						document.getString("slaDefinitionId"));

					if (overdueDate == null) {
						return null;
					}

					return new WorkflowTaskSLAResult(
						document.getBoolean("breached"), overdueDate,
						document.getLong("slaDefinitionId"));
				});

		workflowTaskSLAResults.sort(
			Comparator.comparing(WorkflowTaskSLAResult::getOverdueDate));

		return workflowTaskSLAResults;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		WorkflowTaskSLAResultProviderImpl.class);

	private final DateTimeFormatter _dateTimeFormatter =
		DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	@Reference
	private IndexNameBuilder _indexNameBuilder;

	@Reference
	private KaleoTaskInstanceTokenLocalService
		_kaleoTaskInstanceTokenLocalService;

	@Reference
	private SearchCapabilities _searchCapabilities;

	@Reference
	private SearchEngineAdapter _searchEngineAdapter;

	@Reference
	private WorkflowMetricsSLADefinitionLocalService
		_workflowMetricsSLADefinitionLocalService;

}