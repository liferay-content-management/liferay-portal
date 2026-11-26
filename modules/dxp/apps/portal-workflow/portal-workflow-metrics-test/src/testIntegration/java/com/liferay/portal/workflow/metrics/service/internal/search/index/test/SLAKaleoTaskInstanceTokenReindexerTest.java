/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.service.internal.search.index.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.scheduler.SchedulerJobConfiguration;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.index.IndexNameBuilder;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.QueriesUtil;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;
import com.liferay.portal.workflow.metrics.model.WorkflowMetricsSLADefinition;
import com.liferay.portal.workflow.metrics.search.index.constants.WorkflowMetricsIndexNameConstants;
import com.liferay.portal.workflow.metrics.service.WorkflowMetricsSLADefinitionLocalService;
import com.liferay.portal.workflow.metrics.service.util.BaseWorkflowMetricsIndexerTestCase;
import com.liferay.portal.workflow.metrics.sla.task.WorkflowTaskSLAResultProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Mikel Lorza
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class SLAKaleoTaskInstanceTokenReindexerTest
	extends BaseWorkflowMetricsIndexerTestCase {

	@Test
	public void testReindex() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(
			SLAKaleoTaskInstanceTokenReindexerTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceRegistration<ModelDocumentContributor<?>> serviceRegistration =
			bundleContext.registerService(
				(Class<ModelDocumentContributor<?>>)
					(Class<?>)ModelDocumentContributor.class,
				(ModelDocumentContributor<KaleoTaskInstanceToken>)
					(document, kaleoTaskInstanceToken) -> document.addKeyword(
						"breachedSLADefinitionIds",
						_getBreachedSLADefinitionIds(
							kaleoTaskInstanceToken.
								getKaleoTaskInstanceTokenId())),
				new HashMapDictionary<>(
					Collections.singletonMap(
						"indexer.class.name",
						KaleoTaskInstanceToken.class.getName())));

		try {
			WorkflowMetricsSLADefinition workflowMetricsSLADefinition1 =
				_addWorkflowMetricsSLADefinition(Time.SECOND * 10);
			WorkflowMetricsSLADefinition workflowMetricsSLADefinition2 =
				_addWorkflowMetricsSLADefinition(Time.SECOND * 11);

			_addWorkflowMetricsSLADefinition(Time.DAY);

			KaleoTaskInstanceToken kaleoTaskInstanceToken1 =
				_getKaleoTaskInstanceToken();
			KaleoTaskInstanceToken kaleoTaskInstanceToken2 =
				_getKaleoTaskInstanceToken();

			_testReindex(
				Collections.emptyList(), kaleoTaskInstanceToken1,
				kaleoTaskInstanceToken2);

			UnsafeRunnable<Exception> jobExecutorUnsafeRunnable =
				_schedulerJobConfiguration.getJobExecutorUnsafeRunnable();

			jobExecutorUnsafeRunnable.run();

			_testReindex(
				Collections.emptyList(), kaleoTaskInstanceToken1,
				kaleoTaskInstanceToken2);

			Thread.sleep(Time.SECOND * 12);

			jobExecutorUnsafeRunnable.run();

			_testReindex(
				Arrays.asList(
					workflowMetricsSLADefinition1.
						getWorkflowMetricsSLADefinitionId(),
					workflowMetricsSLADefinition2.
						getWorkflowMetricsSLADefinitionId()),
				kaleoTaskInstanceToken1, kaleoTaskInstanceToken2);

			_kaleoTaskInstanceTokenLocalService.assignKaleoTaskInstanceToken(
				kaleoTaskInstanceToken2.getKaleoTaskInstanceTokenId(),
				User.class.getName(), TestPropsValues.getUserId(), null,
				ServiceContextTestUtil.getServiceContext());

			kaleoTaskInstanceToken2 =
				_kaleoTaskInstanceTokenLocalService.
					completeKaleoTaskInstanceToken(
						kaleoTaskInstanceToken2.getKaleoTaskInstanceTokenId(),
						ServiceContextTestUtil.getServiceContext());

			Assert.assertTrue(kaleoTaskInstanceToken2.isCompleted());

			_testReindex(
				Arrays.asList(
					workflowMetricsSLADefinition1.
						getWorkflowMetricsSLADefinitionId(),
					workflowMetricsSLADefinition2.
						getWorkflowMetricsSLADefinitionId()),
				kaleoTaskInstanceToken2);

			_workflowMetricsSLADefinitionLocalService.
				deactivateWorkflowMetricsSLADefinition(
					workflowMetricsSLADefinition1.
						getWorkflowMetricsSLADefinitionId(),
					ServiceContextTestUtil.getServiceContext());

			_testReindex(
				Arrays.asList(
					workflowMetricsSLADefinition2.
						getWorkflowMetricsSLADefinitionId()),
				kaleoTaskInstanceToken1, kaleoTaskInstanceToken2);

			_workflowMetricsSLADefinitionLocalService.
				updateWorkflowMetricsSLADefinition(
					workflowMetricsSLADefinition2.
						getWorkflowMetricsSLADefinitionId(),
					StringPool.BLANK, StringPool.BLANK, Time.DAY,
					workflowMetricsSLADefinition2.getName(), new String[0],
					new String[] {getInitialNodeKey(workflowDefinition)},
					new String[] {getTerminalNodeKey(workflowDefinition)},
					WorkflowConstants.STATUS_APPROVED,
					ServiceContextTestUtil.getServiceContext());

			_testReindex(
				Collections.emptyList(), kaleoTaskInstanceToken1,
				kaleoTaskInstanceToken2);
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	@Test
	public void testReindexWhenSLABreachIsUnchanged() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(
			SLAKaleoTaskInstanceTokenReindexerTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		Map<Long, Integer> reindexCounts = new ConcurrentHashMap<>();

		ServiceRegistration<ModelDocumentContributor<?>> serviceRegistration =
			bundleContext.registerService(
				(Class<ModelDocumentContributor<?>>)
					(Class<?>)ModelDocumentContributor.class,
				(ModelDocumentContributor<KaleoTaskInstanceToken>)
					(document, kaleoTaskInstanceToken) -> {
						reindexCounts.merge(
							kaleoTaskInstanceToken.
								getKaleoTaskInstanceTokenId(),
							1, Integer::sum);

						document.addKeyword(
							"breachedSLADefinitionIds",
							_getBreachedSLADefinitionIds(
								kaleoTaskInstanceToken.
									getKaleoTaskInstanceTokenId()));
					},
				new HashMapDictionary<>(
					Collections.singletonMap(
						"indexer.class.name",
						KaleoTaskInstanceToken.class.getName())));

		try {
			WorkflowMetricsSLADefinition workflowMetricsSLADefinition =
				_addWorkflowMetricsSLADefinition(Time.SECOND * 3);

			KaleoTaskInstanceToken kaleoTaskInstanceToken =
				_getKaleoTaskInstanceToken();

			Thread.sleep(Time.SECOND * 4);

			UnsafeRunnable<Exception> jobExecutorUnsafeRunnable =
				_schedulerJobConfiguration.getJobExecutorUnsafeRunnable();

			jobExecutorUnsafeRunnable.run();

			_testReindex(
				Arrays.asList(
					workflowMetricsSLADefinition.
						getWorkflowMetricsSLADefinitionId()),
				kaleoTaskInstanceToken);

			Integer reindexCount = reindexCounts.get(
				kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());

			Thread.sleep(Time.SECOND * 2);

			LocalDateTime localDateTime = LocalDateTime.now();

			jobExecutorUnsafeRunnable.run();

			String indexName = _indexNameBuilder.getIndexName(
				kaleoTaskInstanceToken.getCompanyId());

			retryAssertCount(
				booleanQuery -> booleanQuery.addMustQueryClauses(
					QueriesUtil.rangeTerm(
						"modifiedDate", true, true,
						_dateTimeFormatter.format(localDateTime), null)),
				1,
				indexName +
					WorkflowMetricsIndexNameConstants.
						SUFFIX_SLA_INSTANCE_RESULT,
				"WorkflowMetricsSLAInstanceResultType", "slaDefinitionId",
				workflowMetricsSLADefinition.
					getWorkflowMetricsSLADefinitionId());

			Assert.assertEquals(
				reindexCount,
				reindexCounts.get(
					kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId()));
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	private WorkflowMetricsSLADefinition _addWorkflowMetricsSLADefinition(
			long duration)
		throws Exception {

		return _workflowMetricsSLADefinitionLocalService.
			addWorkflowMetricsSLADefinition(
				StringPool.BLANK, StringPool.BLANK, duration,
				RandomTestUtil.randomString(), new String[0],
				workflowDefinition.getWorkflowDefinitionId(),
				new String[] {getInitialNodeKey(workflowDefinition)},
				new String[] {getTerminalNodeKey(workflowDefinition)},
				ServiceContextTestUtil.getServiceContext());
	}

	private long[] _getBreachedSLADefinitionIds(long workflowTaskId) {
		return TransformUtil.transformToLongArray(
			_workflowTaskSLAResultProvider.getWorkflowTaskSLAResults(
				workflowTaskId),
			workflowTaskSLAResult -> {
				if (!workflowTaskSLAResult.isBreached()) {
					return null;
				}

				return workflowTaskSLAResult.getSLADefinitionId();
			});
	}

	private KaleoTaskInstanceToken _getKaleoTaskInstanceToken()
		throws Exception {

		KaleoInstance kaleoInstance = getKaleoInstance(addBlogsEntry());

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		serviceContext.setUserId(0);

		List<KaleoTaskInstanceToken> kaleoTaskInstanceTokens =
			_kaleoTaskInstanceTokenLocalService.getKaleoTaskInstanceTokens(
				kaleoInstance.getKaleoInstanceId(), false, -1, -1, null,
				serviceContext);

		return kaleoTaskInstanceTokens.get(0);
	}

	private void _testReindex(
			List<Long> expectedBreachedSLADefinitionIds,
			KaleoTaskInstanceToken kaleoTaskInstanceToken)
		throws Exception {

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			() -> {
				SearchSearchRequest searchSearchRequest =
					new SearchSearchRequest();

				searchSearchRequest.setIndexNames(
					_indexNameBuilder.getIndexName(
						kaleoTaskInstanceToken.getCompanyId()));

				BooleanQuery booleanQuery = QueriesUtil.booleanQuery();

				searchSearchRequest.setQuery(
					booleanQuery.addFilterQueryClauses(
						QueriesUtil.term(
							Field.ENTRY_CLASS_NAME,
							KaleoTaskInstanceToken.class.getName()),
						QueriesUtil.term(
							Field.ENTRY_CLASS_PK,
							kaleoTaskInstanceToken.
								getKaleoTaskInstanceTokenId())));

				SearchSearchResponse searchSearchResponse =
					searchEngineAdapter.execute(searchSearchRequest);

				SearchHits searchHits = searchSearchResponse.getSearchHits();

				List<SearchHit> searchHitsList = searchHits.getSearchHits();

				Assert.assertEquals(
					searchHitsList.toString(), 1, searchHitsList.size());

				SearchHit searchHit = searchHitsList.get(0);

				Document document = searchHit.getDocument();

				Assert.assertEquals(
					expectedBreachedSLADefinitionIds,
					document.getLongs("breachedSLADefinitionIds"));

				return null;
			});
	}

	private void _testReindex(
			List<Long> expectedBreachedSLADefinitionIds,
			KaleoTaskInstanceToken... kaleoTaskInstanceTokens)
		throws Exception {

		for (KaleoTaskInstanceToken kaleoTaskInstanceToken :
				kaleoTaskInstanceTokens) {

			_testReindex(
				expectedBreachedSLADefinitionIds, kaleoTaskInstanceToken);
		}
	}

	private final DateTimeFormatter _dateTimeFormatter =
		DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	@Inject
	private IndexNameBuilder _indexNameBuilder;

	@Inject
	private KaleoTaskInstanceTokenLocalService
		_kaleoTaskInstanceTokenLocalService;

	@Inject(
		filter = "component.name=com.liferay.portal.workflow.metrics.internal.scheduler.WorkflowMetricsSLAProcessSchedulerJobConfiguration"
	)
	private SchedulerJobConfiguration _schedulerJobConfiguration;

	@Inject
	private WorkflowMetricsSLADefinitionLocalService
		_workflowMetricsSLADefinitionLocalService;

	@Inject
	private WorkflowTaskSLAResultProvider _workflowTaskSLAResultProvider;

}