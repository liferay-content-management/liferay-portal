/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.portlet.action;

import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RepositoryLocalService;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Attila Bakay
 */
@Component(
	property = {
		"javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY,
		"javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY_ADMIN,
		"mvc.command.name=/document_library/get_external_reference_code"
	},
	service = MVCResourceCommand.class
)
public class GetExternalReferenceCodeMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	public void doServeResource(
		ResourceRequest resourceRequest, ResourceResponse resourceResponse) {

		try {
			HttpServletRequest httpServletRequest =
				_portal.getOriginalServletRequest(
					_portal.getHttpServletRequest(resourceRequest));

			long selectedRepositoryId = ParamUtil.getLong(
				httpServletRequest, "selectedRepositoryId");

			long rootFolderId = ParamUtil.getLong(
				httpServletRequest, "rootFolderId");

			String folderExternalReferenceCode = "";

			if (rootFolderId != 0) {
				Folder folder = _dlAppLocalService.getFolder(rootFolderId);

				folderExternalReferenceCode = folder.getExternalReferenceCode();
			}

			Repository selectedRepository =
				_repositoryLocalService.fetchRepository(selectedRepositoryId);

			String selectedRepositoryExternalReferenceCode = "";
			long repositoryGroupId = 0;

			if (selectedRepository != null) {
				selectedRepositoryExternalReferenceCode =
					selectedRepository.getExternalReferenceCode();
				repositoryGroupId = selectedRepository.getGroupId();
			}
			else {
				Group group = _groupLocalService.getGroup(selectedRepositoryId);

				selectedRepositoryExternalReferenceCode =
					group.getExternalReferenceCode();

				repositoryGroupId = selectedRepositoryId;
			}

			HttpServletResponse httpServletResponse =
				_portal.getHttpServletResponse(resourceResponse);

			httpServletResponse.setContentType(ContentTypes.APPLICATION_JSON);

			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse,
				JSONUtil.put(
					"repositoryGroupId", repositoryGroupId
				).put(
					"rootFolderExternalReferenceCode",
					folderExternalReferenceCode
				).put(
					"selectedRepositoryExternalReferenceCode",
					selectedRepositoryExternalReferenceCode
				));
		}
		catch (Exception exception) {
			_log.error(exception);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GetExternalReferenceCodeMVCResourceCommand.class);

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private RepositoryLocalService _repositoryLocalService;

}