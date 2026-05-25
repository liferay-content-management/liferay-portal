/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.internal.struts;

import com.liferay.object.storage.sharepoint.configuration.SharepointConfiguration;
import com.liferay.object.storage.sharepoint.constants.SharepointConstants;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	property = "path=/portal/object_storage_sharepoint_get_space_settings",
	service = StrutsAction.class
)
public class GetSharepointSpaceSettingsStrutsAction implements StrutsAction {

	@Override
	public String execute(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		long groupId = ParamUtil.getLong(httpServletRequest, "groupId");

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!_groupModelResourcePermission.contains(
				permissionChecker, groupId, ActionKeys.UPDATE)) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker, Group.class.getName(), groupId,
				ActionKeys.UPDATE);
		}

		SharepointConfiguration sharepointConfiguration =
			_configurationProvider.getConfiguration(
				SharepointConfiguration.class,
				new GroupServiceSettingsLocator(
					groupId, SharepointConstants.SERVICE_NAME,
					SharepointConfiguration.class.getName()));

		Group group = _groupLocalService.getGroup(groupId);

		String folderUrl = group.getTypeSettingsProperty("sharepointFolderUrl");

		JSONObject jsonObject = JSONUtil.put(
			"clientId", sharepointConfiguration.clientId()
		).put(
			"clientSecret",
			_jsonFactory.createJSONObject(
			).put(
				"set",
				Validator.isNotNull(sharepointConfiguration.clientSecret())
			)
		).put(
			"folderUrl", folderUrl
		).put(
			"tenantId", sharepointConfiguration.tenantId()
		);

		httpServletResponse.setContentType("application/json");

		PrintWriter printWriter = httpServletResponse.getWriter();

		printWriter.write(jsonObject.toString());

		return null;
	}

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.portal.kernel.model.Group)"
	)
	private ModelResourcePermission<Group> _groupModelResourcePermission;

	@Reference
	private JSONFactory _jsonFactory;

}