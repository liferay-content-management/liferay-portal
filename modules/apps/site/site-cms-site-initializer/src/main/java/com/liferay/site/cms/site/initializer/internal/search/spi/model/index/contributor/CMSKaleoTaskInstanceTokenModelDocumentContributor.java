/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.spi.model.index.contributor;

import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.metrics.sla.task.WorkflowMetricsSLATaskResult;
import com.liferay.portal.workflow.metrics.sla.task.WorkflowMetricsSLATaskResultProvider;

import org.osgi.service.component.annotations.Component;

/**
 * @author Mikel Lorza
 */
@Component(
	property = "indexer.class.name=com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken",
	service = ModelDocumentContributor.class
)
public class CMSKaleoTaskInstanceTokenModelDocumentContributor
	implements ModelDocumentContributor<KaleoTaskInstanceToken> {

	@Override
	public void contribute(
		Document document, KaleoTaskInstanceToken kaleoTaskInstanceToken) {

		WorkflowMetricsSLATaskResultProvider
			workflowMetricsSLATaskResultProvider =
				_workflowMetricsSLATaskResultProviderSnapshot.get();

		if (workflowMetricsSLATaskResultProvider == null) {
			return;
		}

		for (WorkflowMetricsSLATaskResult workflowMetricsSLATaskResult :
				workflowMetricsSLATaskResultProvider.
					getWorkflowMetricsSLATaskResults(
						kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId())) {

			if (!workflowMetricsSLATaskResult.isBreached()) {
				continue;
			}

			document.addKeyword("slaBreached", true);
			document.addDate(
				"slaOverdueDate",
				workflowMetricsSLATaskResult.getOverdueDate());

			return;
		}

		document.addKeyword("slaBreached", false);
	}

	private static final Snapshot<WorkflowMetricsSLATaskResultProvider>
		_workflowMetricsSLATaskResultProviderSnapshot = new Snapshot<>(
			CMSKaleoTaskInstanceTokenModelDocumentContributor.class,
			WorkflowMetricsSLATaskResultProvider.class, null, true);

}