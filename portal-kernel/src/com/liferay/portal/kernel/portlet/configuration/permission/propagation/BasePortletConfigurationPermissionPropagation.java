/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.portlet.configuration.permission.propagation;

import com.liferay.portal.kernel.model.PermissionPropagationEntry;
import com.liferay.portal.kernel.service.PermissionPropagationEntryLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

/**
 * @author To Trinh
 */
public abstract class BasePortletConfigurationPermissionPropagation
	implements PortletConfigurationPermissionPropagation {

	@Override
	public boolean getPermissionPropagation(PortletRequest portletRequest) {
		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionPropagationEntry permissionPropagationEntry =
			PermissionPropagationEntryLocalServiceUtil.
				fetchPermissionPropagationEntry(
					themeDisplay.getCompanyId(), getGroupId(portletRequest),
					getClassName(), getClassPK(portletRequest));

		if (permissionPropagationEntry != null) {
			return permissionPropagationEntry.isPropagation();
		}

		return getDefaultPermissionPropagation(
			getGroupId(portletRequest), getClassPK(portletRequest));
	}

	@Override
	public boolean isAvailable(PortletRequest portletRequest) {
		String modelResource = ParamUtil.getString(
			portletRequest, "modelResource");

		if (!ArrayUtil.contains(getModelResources(), modelResource) ||
			(getClassPK(portletRequest) == 0)) {

			return false;
		}

		return true;
	}

	@Override
	public void updatePermissionPropagation(
		ActionRequest actionRequest, ActionResponse actionResponse) {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionPropagationEntryLocalServiceUtil.
			updatePermissionPropagationEntry(
				themeDisplay.getCompanyId(), getGroupId(actionRequest),
				getClassName(), getClassPK(actionRequest),
				ParamUtil.getBoolean(
					actionRequest, "permissionPropagationEnabled"));
	}

	protected abstract String getClassName();

	protected long getClassPK(PortletRequest portletRequest) {
		return ParamUtil.getLong(portletRequest, "resourcePrimKey");
	}

	protected abstract boolean getDefaultPermissionPropagation(
		long groupId, long classPK);

	protected long getGroupId(PortletRequest portletRequest) {
		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return ParamUtil.getLong(
			portletRequest, "resourceGroupId", themeDisplay.getScopeGroupId());
	}

	protected abstract String[] getModelResources();

}