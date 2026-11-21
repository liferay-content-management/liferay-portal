/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.util;

import com.liferay.frontend.data.set.view.FDSView;
import com.liferay.frontend.data.set.view.FDSViewRegistry;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;

import java.util.List;

/**
 * @author Veronica Gonzalez
 */
public class TasksSectionUtil {

	public static String getFDSName(FDSViewRegistry fdsViewRegistry) {
		List<FDSView> fdsViews = fdsViewRegistry.getFDSViews(
			_CMP_WORKFLOW_TASKS_FDS_NAME);

		if (fdsViews.isEmpty()) {
			return CMSSiteInitializerFDSNames.TASKS_SECTION;
		}

		return _CMP_WORKFLOW_TASKS_FDS_NAME;
	}

	private static final String _CMP_WORKFLOW_TASKS_FDS_NAME =
		"com.liferay.content.marketing.platform.web-workflow-tasks";

}