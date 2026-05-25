/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.internal.struts;

import com.liferay.object.storage.sharepoint.configuration.SharepointConfiguration;
import com.liferay.object.storage.sharepoint.constants.SharepointConstants;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	property = "path=/portal/object_storage_sharepoint_save_space_settings",
	service = StrutsAction.class
)
public class SaveSharepointSpaceSettingsStrutsAction implements StrutsAction {

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

		String clientSecret = ParamUtil.getString(
			httpServletRequest, "clientSecret");

		if (Validator.isNull(clientSecret)) {
			SharepointConfiguration sharepointConfiguration =
				_configurationProvider.getConfiguration(
					SharepointConfiguration.class,
					new GroupServiceSettingsLocator(
						groupId, SharepointConstants.SERVICE_NAME,
						SharepointConfiguration.class.getName()));

			clientSecret = sharepointConfiguration.clientSecret();
		}

		Group group = _groupLocalService.getGroup(groupId);

		_configurationProvider.saveGroupConfiguration(
			SharepointConfiguration.class, group.getCompanyId(), groupId,
			HashMapDictionaryBuilder.<String, Object>put(
				"clientId", ParamUtil.getString(httpServletRequest, "clientId")
			).put(
				"clientSecret", clientSecret
			).put(
				"tenantId", ParamUtil.getString(httpServletRequest, "tenantId")
			).build());

		UnicodeProperties unicodeProperties = UnicodePropertiesBuilder.create(
			group.getTypeSettingsProperties(), true
		).put(
			"sharepointFolderUrl",
			ParamUtil.getString(httpServletRequest, "folderUrl")
		).build();

		group.setTypeSettingsProperties(unicodeProperties);

		_groupLocalService.updateGroup(group);

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

}