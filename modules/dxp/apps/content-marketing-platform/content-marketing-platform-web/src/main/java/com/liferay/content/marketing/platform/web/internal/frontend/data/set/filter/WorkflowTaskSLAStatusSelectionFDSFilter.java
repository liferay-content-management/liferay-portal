/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.marketing.platform.web.internal.frontend.data.set.filter;

import com.liferay.frontend.data.set.constants.FDSEntityFieldTypes;
import com.liferay.frontend.data.set.filter.BaseSelectionFDSFilter;
import com.liferay.frontend.data.set.filter.SelectionFDSFilterItem;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.ListUtil;

import java.util.List;
import java.util.Locale;

/**
 * @author Mikel Lorza
 */
public class WorkflowTaskSLAStatusSelectionFDSFilter
	extends BaseSelectionFDSFilter {

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
	public boolean isMultiple() {
		return false;
	}

}