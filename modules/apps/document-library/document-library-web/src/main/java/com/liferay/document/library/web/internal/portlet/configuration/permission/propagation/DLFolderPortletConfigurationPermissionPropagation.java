/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.portlet.configuration.permission.propagation;

import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.portal.kernel.portlet.configuration.permission.propagation.BasePortletConfigurationPermissionPropagation;
import com.liferay.portal.kernel.portlet.configuration.permission.propagation.PortletConfigurationPermissionPropagation;
import com.liferay.portlet.documentlibrary.constants.DLConstants;

import org.osgi.service.component.annotations.Component;

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
	public String getAlertMessage() {
		return "changes-will-be-applied-to-new-documents-and-folder-uploaded-" +
			"into-it";
	}

	@Override
	public String getMessage() {
		return "set-permissions-for-documents-and-folders-uploaded-to-this-" +
			"folder";
	}

	@Override
	protected String getClassName() {
		return DLFolder.class.getName();
	}

	@Override
	protected boolean getDefaultPermissionPropagation(
		long groupId, long classPK) {

		if ((classPK == groupId) ||
			(classPK == DLFolderConstants.DEFAULT_PARENT_FOLDER_ID)) {

			return DLFolderConstants.
				DEFAULT_FOLDER_PERMISSION_PROPAGATION_ENABLED;
		}

		return false;
	}

	@Override
	protected String[] getModelResources() {
		return new String[] {
			DLFolder.class.getName(), DLConstants.RESOURCE_NAME
		};
	}

}