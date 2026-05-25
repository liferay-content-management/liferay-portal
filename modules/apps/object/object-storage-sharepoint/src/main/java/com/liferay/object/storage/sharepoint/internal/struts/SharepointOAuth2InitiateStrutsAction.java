/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.internal.struts;

import com.liferay.object.storage.sharepoint.configuration.SharepointConfiguration;
import com.liferay.object.storage.sharepoint.constants.SharepointConstants;
import com.liferay.object.storage.sharepoint.internal.oauth2.SharepointRequestState;
import com.liferay.object.storage.sharepoint.internal.oauth2.SharepointTokenBroker;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	property = "path=/portal/object_storage_sharepoint_oauth2_initiate",
	service = StrutsAction.class
)
public class SharepointOAuth2InitiateStrutsAction implements StrutsAction {

	@Override
	public String execute(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		long groupId = ParamUtil.getLong(httpServletRequest, "groupId");
		String returnURL = ParamUtil.getString(httpServletRequest, "returnURL");

		SharepointConfiguration sharepointConfiguration =
			_configurationProvider.getConfiguration(
				SharepointConfiguration.class,
				new GroupServiceSettingsLocator(
					groupId, SharepointConstants.SERVICE_NAME,
					SharepointConfiguration.class.getName()));

		SharepointTokenBroker sharepointTokenBroker = new SharepointTokenBroker(
			sharepointConfiguration);

		String state = SharepointRequestState.save(
			groupId, httpServletRequest, returnURL);

		httpServletResponse.sendRedirect(
			sharepointTokenBroker.getAuthorizationRequestUrl(
				String.valueOf(UUID.randomUUID()),
				PortalUtil.getPortalURL(httpServletRequest) +
					"/c/portal/object_storage_sharepoint_oauth2",
				state));

		return null;
	}

	@Reference
	private ConfigurationProvider _configurationProvider;

}