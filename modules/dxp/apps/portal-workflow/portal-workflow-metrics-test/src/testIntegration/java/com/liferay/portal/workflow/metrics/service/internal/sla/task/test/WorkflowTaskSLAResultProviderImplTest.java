/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.service.internal.sla.task.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.scheduler.SchedulerJobConfiguration;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;
import com.liferay.portal.workflow.metrics.model.WorkflowMetricsSLADefinition;
import com.liferay.portal.workflow.metrics.service.WorkflowMetricsSLADefinitionLocalService;
import com.liferay.portal.workflow.metrics.service.util.BaseWorkflowMetricsIndexerTestCase;
import com.liferay.portal.workflow.metrics.sla.task.WorkflowTaskSLAResult;
import com.liferay.portal.workflow.metrics.sla.task.WorkflowTaskSLAResultProvider;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class WorkflowTaskSLAResultProviderImplTest
	extends BaseWorkflowMetricsIndexerTestCase {

	@Test
	public void testGetWorkflowTaskSLAResults() throws Exception {
		WorkflowMetricsSLADefinition workflowMetricsSLADefinition1 =
			_addWorkflowMetricsSLADefinition(Time.DAY);
		WorkflowMetricsSLADefinition workflowMetricsSLADefinition2 =
			_addWorkflowMetricsSLADefinition(Time.SECOND);

		KaleoInstance kaleoInstance = getKaleoInstance(addBlogsEntry());

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		serviceContext.setUserId(0);

		List<KaleoTaskInstanceToken> kaleoTaskInstanceTokens =
			_kaleoTaskInstanceTokenLocalService.getKaleoTaskInstanceTokens(
				kaleoInstance.getKaleoInstanceId(), false, -1, -1, null,
				serviceContext);

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			kaleoTaskInstanceTokens.get(0);

		Thread.sleep(Time.SECOND * 2);

		UnsafeRunnable<Exception> jobExecutorUnsafeRunnable =
			_schedulerJobConfiguration.getJobExecutorUnsafeRunnable();

		jobExecutorUnsafeRunnable.run();

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			() -> {
				List<WorkflowTaskSLAResult> workflowTaskSLAResults =
					_workflowTaskSLAResultProvider.getWorkflowTaskSLAResults(
						kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());

				Assert.assertEquals(
					workflowTaskSLAResults.toString(), 2,
					workflowTaskSLAResults.size());

				WorkflowTaskSLAResult workflowTaskSLAResult =
					workflowTaskSLAResults.get(0);

				Assert.assertTrue(workflowTaskSLAResult.isBreached());
				Assert.assertEquals(
					workflowMetricsSLADefinition2.
						getWorkflowMetricsSLADefinitionId(),
					workflowTaskSLAResult.getSLADefinitionId());

				Date overdueDate = workflowTaskSLAResult.getOverdueDate();

				workflowTaskSLAResult = workflowTaskSLAResults.get(1);

				Assert.assertFalse(workflowTaskSLAResult.isBreached());
				Assert.assertEquals(
					workflowMetricsSLADefinition1.
						getWorkflowMetricsSLADefinitionId(),
					workflowTaskSLAResult.getSLADefinitionId());
				Assert.assertTrue(
					overdueDate.before(workflowTaskSLAResult.getOverdueDate()));

				return null;
			});
	}

	@Test
	public void testGetWorkflowTaskSLAResultsWhenSLADefinitionIsDeactivated()
		throws Exception {

		WorkflowMetricsSLADefinition workflowMetricsSLADefinition1 =
			_addWorkflowMetricsSLADefinition(Time.DAY);
		WorkflowMetricsSLADefinition workflowMetricsSLADefinition2 =
			_addWorkflowMetricsSLADefinition(Time.DAY);

		KaleoInstance kaleoInstance = getKaleoInstance(addBlogsEntry());

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		serviceContext.setUserId(0);

		List<KaleoTaskInstanceToken> kaleoTaskInstanceTokens =
			_kaleoTaskInstanceTokenLocalService.getKaleoTaskInstanceTokens(
				kaleoInstance.getKaleoInstanceId(), false, -1, -1, null,
				serviceContext);

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			kaleoTaskInstanceTokens.get(0);

		completeKaleoTaskInstanceToken(kaleoInstance);

		completeKaleoInstanceToken(kaleoInstance);

		completeKaleoInstance(kaleoInstance);

		UnsafeRunnable<Exception> jobExecutorUnsafeRunnable =
			_schedulerJobConfiguration.getJobExecutorUnsafeRunnable();

		jobExecutorUnsafeRunnable.run();

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			() -> {
				List<WorkflowTaskSLAResult> workflowTaskSLAResults =
					_workflowTaskSLAResultProvider.getWorkflowTaskSLAResults(
						kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());

				Assert.assertEquals(
					workflowTaskSLAResults.toString(), 2,
					workflowTaskSLAResults.size());

				return null;
			});

		_workflowMetricsSLADefinitionLocalService.
			deactivateWorkflowMetricsSLADefinition(
				workflowMetricsSLADefinition1.
					getWorkflowMetricsSLADefinitionId(),
				ServiceContextTestUtil.getServiceContext());

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			() -> {
				List<WorkflowTaskSLAResult> workflowTaskSLAResults =
					_workflowTaskSLAResultProvider.getWorkflowTaskSLAResults(
						kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());

				Assert.assertEquals(
					workflowTaskSLAResults.toString(), 1,
					workflowTaskSLAResults.size());

				WorkflowTaskSLAResult workflowTaskSLAResult =
					workflowTaskSLAResults.get(0);

				Assert.assertEquals(
					workflowMetricsSLADefinition2.
						getWorkflowMetricsSLADefinitionId(),
					workflowTaskSLAResult.getSLADefinitionId());

				return null;
			});
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