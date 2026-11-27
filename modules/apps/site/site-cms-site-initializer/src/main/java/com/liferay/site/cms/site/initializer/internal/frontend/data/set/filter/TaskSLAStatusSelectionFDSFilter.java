/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set.filter;

import com.liferay.frontend.data.set.constants.FDSEntityFieldTypes;
import com.liferay.frontend.data.set.filter.BaseSelectionFDSFilter;
import com.liferay.frontend.data.set.filter.FDSFilter;
import com.liferay.frontend.data.set.filter.SelectionFDSFilterItem;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.workflow.metrics.sla.task.WorkflowMetricsSLATaskResultProvider;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;

import java.util.List;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;

/**
 * @author Mikel Lorza
 */
@Component(
	property = {
		"frontend.data.set.name=" + CMSSiteInitializerFDSNames.TASKS_SECTION,
		"service.ranking:Integer=85"
	},
	service = FDSFilter.class
)
public class TaskSLAStatusSelectionFDSFilter extends BaseSelectionFDSFilter {

	@Override
	public String getEntityFieldType() {
		return FDSEntityFieldTypes.BOOLEAN;
	}

	@Override
	public String getId() {
		return "slaBreached";
	}

	@Override
	public String getLabel() {
		return "sla-status";
	}

	@Override
	public List<SelectionFDSFilterItem> getSelectionFDSFilterItems(
		Locale locale) {

		return ListUtil.fromArray(
			new SelectionFDSFilterItem(
				LanguageUtil.get(locale, "breached"), "true"),
			new SelectionFDSFilterItem(
				LanguageUtil.get(locale, "not-breached"), "false"));
	}

	@Override
	public boolean isAutocompleteEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		if (_workflowMetricsSLATaskResultProviderSnapshot.get() != null) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isMultiple() {
		return false;
	}

	private static final Snapshot<WorkflowMetricsSLATaskResultProvider>
		_workflowMetricsSLATaskResultProviderSnapshot = new Snapshot<>(
			TaskSLAStatusSelectionFDSFilter.class,
			WorkflowMetricsSLATaskResultProvider.class, null, true);

}