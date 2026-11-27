/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.spi.model.index.contributor.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;
import com.liferay.portal.workflow.metrics.sla.task.WorkflowMetricsSLATaskResult;
import com.liferay.portal.workflow.metrics.sla.task.WorkflowMetricsSLATaskResultProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class CMSKaleoTaskInstanceTokenModelDocumentContributorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testContribute() throws Exception {
		KaleoTaskInstanceToken kaleoTaskInstanceToken1 =
			_kaleoTaskInstanceTokenLocalService.createKaleoTaskInstanceToken(
				RandomTestUtil.randomLong());
		KaleoTaskInstanceToken kaleoTaskInstanceToken2 =
			_kaleoTaskInstanceTokenLocalService.createKaleoTaskInstanceToken(
				RandomTestUtil.randomLong());
		KaleoTaskInstanceToken kaleoTaskInstanceToken3 =
			_kaleoTaskInstanceTokenLocalService.createKaleoTaskInstanceToken(
				RandomTestUtil.randomLong());

		long time = (System.currentTimeMillis() / Time.SECOND) * Time.SECOND;

		Map<Long, List<WorkflowMetricsSLATaskResult>>
			workflowMetricsSLATaskResults =
				HashMapBuilder.<Long, List<WorkflowMetricsSLATaskResult>>put(
					kaleoTaskInstanceToken1.getKaleoTaskInstanceTokenId(),
					Arrays.asList(
						new WorkflowMetricsSLATaskResult(
							false, new Date(time - (Time.HOUR * 2)),
							RandomTestUtil.randomLong()),
						new WorkflowMetricsSLATaskResult(
							true, new Date(time - Time.HOUR),
							RandomTestUtil.randomLong()),
						new WorkflowMetricsSLATaskResult(
							true, new Date(time - Time.MINUTE),
							RandomTestUtil.randomLong()))
				).put(
					kaleoTaskInstanceToken2.getKaleoTaskInstanceTokenId(),
					Arrays.asList(
						new WorkflowMetricsSLATaskResult(
							false, new Date(time + Time.HOUR),
							RandomTestUtil.randomLong()))
				).build();

		Bundle bundle = FrameworkUtil.getBundle(
			CMSKaleoTaskInstanceTokenModelDocumentContributorTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceRegistration<WorkflowMetricsSLATaskResultProvider>
			serviceRegistration = bundleContext.registerService(
				WorkflowMetricsSLATaskResultProvider.class,
				taskId -> workflowMetricsSLATaskResults.getOrDefault(
					taskId, Collections.emptyList()),
				new HashMapDictionary<>(
					Collections.singletonMap(
						Constants.SERVICE_RANKING, Integer.MAX_VALUE)));

		try {
			Document document = new DocumentImpl();

			_modelDocumentContributor.contribute(
				document, kaleoTaskInstanceToken1);

			Assert.assertEquals("true", document.get("slaBreached"));
			Assert.assertEquals(
				new Date(time - Time.HOUR), document.getDate("slaOverdueDate"));

			document = new DocumentImpl();

			_modelDocumentContributor.contribute(
				document, kaleoTaskInstanceToken2);

			Assert.assertEquals("false", document.get("slaBreached"));
			Assert.assertNull(document.getField("slaOverdueDate"));

			document = new DocumentImpl();

			_modelDocumentContributor.contribute(
				document, kaleoTaskInstanceToken3);

			Assert.assertEquals("false", document.get("slaBreached"));
			Assert.assertNull(document.getField("slaOverdueDate"));
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	@Inject
	private KaleoTaskInstanceTokenLocalService
		_kaleoTaskInstanceTokenLocalService;

	@Inject(
		filter = "component.name=com.liferay.site.cms.site.initializer.internal.search.spi.model.index.contributor.CMSKaleoTaskInstanceTokenModelDocumentContributor"
	)
	private ModelDocumentContributor<KaleoTaskInstanceToken>
		_modelDocumentContributor;

}