/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.portlet.configuration.permission.propagation;

import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.portal.kernel.model.PermissionPropagationEntry;
import com.liferay.portal.kernel.portlet.configuration.permission.propagation.BasePortletConfigurationPermissionPropagation;
import com.liferay.portal.kernel.portlet.configuration.permission.propagation.PortletConfigurationPermissionPropagation;
import com.liferay.portal.kernel.service.PermissionPropagationEntryLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portlet.documentlibrary.constants.DLConstants;

import java.util.Objects;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author To Trinh
 */
@Component(
	property = {
		"javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY,
		"javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY_ADMIN,
		"javax.portlet.name=" + DLPortletKeys.MEDIA_GALLERY_DISPLAY
	},
	service = PortletConfigurationPermissionPropagation.class
)
public class DLFolderPortletConfigurationPermissionPropagation
	extends BasePortletConfigurationPermissionPropagation {

	@Override
	public String getMessage() {
		return "set-permissions-for-documents-and-folders-uploaded-to-this-" +
			"folder";
	}

	@Override
	public boolean getPermissionPropagation(PortletRequest portletRequest) {
		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long groupId = getGroupId(portletRequest);

		long classPK = getClassPK(portletRequest);

		if (classPK == groupId) {
			classPK = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;
		}

		PermissionPropagationEntry permissionPropagationEntry =
			_permissionPropagationEntryLocalService.
				fetchPermissionPropagationEntry(
					themeDisplay.getCompanyId(), groupId,
					DLFolder.class.getName(), classPK);

		if (permissionPropagationEntry != null) {
			return permissionPropagationEntry.isPropagation();
		}

		if (classPK == DLFolderConstants.DEFAULT_PARENT_FOLDER_ID) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isShow(PortletRequest portletRequest) {
		if ((!Objects.equals(
				getClassName(portletRequest), DLFolder.class.getName()) &&
			 !Objects.equals(
				 getClassName(portletRequest), DLConstants.RESOURCE_NAME)) ||
			_isBulkSelection(portletRequest)) {

			return false;
		}

		return true;
	}

	@Override
	public void updatePermissionPropagation(
		ActionRequest actionRequest, ActionResponse actionResponse) {
	}

	private boolean _isBulkSelection(PortletRequest portletRequest) {
		String[] resourcePrimKeys = ParamUtil.getStringValues(
			portletRequest, "resourcePrimKey");

		if (resourcePrimKeys.length > 1) {
			return true;
		}

		return false;
	}

	@Reference
	private PermissionPropagationEntryLocalService
		_permissionPropagationEntryLocalService;

}