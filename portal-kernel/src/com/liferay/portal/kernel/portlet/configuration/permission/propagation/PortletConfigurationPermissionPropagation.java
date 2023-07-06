/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.portlet.configuration.permission.propagation;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

/**
 * @author To Trinh
 */
public interface PortletConfigurationPermissionPropagation {

	public String getMessage();

	public boolean getPermissionPropagation(PortletRequest portletRequest);

	public boolean isShow(PortletRequest portletRequest);

	public void updatePermissionPropagation(
		ActionRequest actionRequest, ActionResponse actionResponse);

}