/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.portlet.configuration.permission.propagation;

import com.liferay.portal.kernel.util.ParamUtil;

import javax.portlet.PortletRequest;

/**
 * @author To Trinh
 */
public abstract class BasePortletConfigurationPermissionPropagation
	implements PortletConfigurationPermissionPropagation {

	@Override
	public boolean isShow(PortletRequest portletRequest) {
		return false;
	}

	protected String getClassName(PortletRequest portletRequest) {
		return ParamUtil.getString(portletRequest, "modelResource");
	}

	protected long getClassPK(PortletRequest portletRequest) {
		return ParamUtil.getLong(portletRequest, "resourcePrimKey");
	}

	protected long getGroupId(PortletRequest portletRequest) {
		return ParamUtil.getLong(portletRequest, "resourceGroupId");
	}

}