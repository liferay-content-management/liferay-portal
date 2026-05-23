/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.internal.struts;

import com.liferay.object.storage.sharepoint.configuration.SharepointConfiguration;
import com.liferay.object.storage.sharepoint.constants.SharepointConstants;
import com.liferay.object.storage.sharepoint.internal.oauth2.SharepointRequestState;
import com.liferay.object.storage.sharepoint.internal.oauth2.SharepointTokenBroker;
import com.liferay.object.storage.sharepoint.service.TokenEntryLocalService;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;

import com.microsoft.aad.msal4j.IAuthenticationResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	property = "path=/portal/object_storage_sharepoint_oauth2",
	service = StrutsAction.class
)
public class SharepointOAuth2CallbackStrutsAction implements StrutsAction {

	@Override
	public String execute(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		String state = ParamUtil.getString(httpServletRequest, "state");

		SharepointRequestState sharepointRequestState =
			SharepointRequestState.pop(httpServletRequest, state);

		if (sharepointRequestState == null) {
			httpServletResponse.sendError(
				HttpServletResponse.SC_BAD_REQUEST, "Invalid OAuth state");

			return null;
		}

		long groupId = sharepointRequestState.getGroupId();

		SharepointConfiguration sharepointConfiguration =
			_configurationProvider.getConfiguration(
				SharepointConfiguration.class,
				new GroupServiceSettingsLocator(
					groupId, SharepointConstants.SERVICE_NAME,
					SharepointConfiguration.class.getName()));

		String code = ParamUtil.getString(httpServletRequest, "code");

		String redirectURI = _getRedirectURI(httpServletRequest);

		SharepointTokenBroker sharepointTokenBroker = new SharepointTokenBroker(
			sharepointConfiguration);

		IAuthenticationResult iAuthenticationResult =
			sharepointTokenBroker.requestAccessToken(code, redirectURI);

		String accessToken = iAuthenticationResult.accessToken();

		Date expirationDate = iAuthenticationResult.expiresOnDate();

		_tokenEntryLocalService.addTokenEntry(
			accessToken, expirationDate, groupId, null,
			PortalUtil.getUserId(httpServletRequest));

		httpServletResponse.sendRedirect(sharepointRequestState.getReturnURL());

		return null;
	}

	private String _getRedirectURI(HttpServletRequest httpServletRequest) {
		return PortalUtil.getPortalURL(httpServletRequest) +
			"/c/portal/object_storage_sharepoint_oauth2";
	}

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private TokenEntryLocalService _tokenEntryLocalService;

}