/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectDefinitionSettingLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.site.cms.site.initializer.internal.util.ActionUtil;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Marco Galluzzi
 */
public class ViewFolderDisplayContext extends BaseSectionDisplayContext {

	public ViewFolderDisplayContext(
		DepotEntryLocalService depotEntryLocalService,
		GroupLocalService groupLocalService,
		HttpServletRequest httpServletRequest, Language language,
		ObjectDefinitionService objectDefinitionService,
		ObjectDefinitionSettingLocalService objectDefinitionSettingLocalService,
		ObjectEntryFolderLocalService objectEntryFolderLocalService,
		Portal portal) {

		super(
			depotEntryLocalService, groupLocalService, httpServletRequest,
			language, objectDefinitionService,
			objectDefinitionSettingLocalService, portal);

		_objectEntryFolderLocalService = objectEntryFolderLocalService;
	}

	public Map<String, Object> getBreadcrumbProps() {
		if (objectEntryFolder == null) {
			return Collections.emptyMap();
		}

		Group group = groupLocalService.fetchGroup(
			objectEntryFolder.getGroupId());

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		_addBreadcrumbItem(
			jsonArray, false,
			ActionUtil.getSpaceFullURL(group.getClassPK(), themeDisplay),
			group.getName(themeDisplay.getLocale()));

		String[] parts = StringUtil.split(
			objectEntryFolder.getTreePath(), CharPool.SLASH);

		if (parts.length > 2) {
			for (int i = 1; i < (parts.length - 1); i++) {
				ObjectEntryFolder objectEntryFolder =
					_objectEntryFolderLocalService.fetchObjectEntryFolder(
						GetterUtil.getLong(parts[i]));

				_addBreadcrumbItem(
					jsonArray, false,
					ActionUtil.geViewFolderFullURL(
						objectEntryFolder.getObjectEntryFolderId(),
						themeDisplay),
					objectEntryFolder.getName());
			}
		}

		_addBreadcrumbItem(jsonArray, true, null, objectEntryFolder.getName());

		return HashMapBuilder.<String, Object>put(
			"breadcrumbItems", jsonArray
		).build();
	}

	@Override
	public Map<String, Object> getEmptyState() {
		String rootObjectEntryFolderExternalReferenceCode =
			getRootObjectEntryFolderExternalReferenceCode();

		String description = "click-new-to-create-your-first-asset";
		String image = "/states/cms_empty_state.svg";
		String title = "no-assets-yet";

		if (ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS.equals(
				rootObjectEntryFolderExternalReferenceCode)) {

			description = "click-new-to-create-your-first-file";
			image = "/states/cms_empty_state_content.svg";
			title = "no-content-yet";
		}

		if (ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_FILES.equals(
				rootObjectEntryFolderExternalReferenceCode)) {

			description = "click-new-to-create-your-first-piece-of-content";
			image = "/states/cms_empty_state_files.svg";
			title = "no-files-yet";
		}

		return HashMapBuilder.<String, Object>put(
			"description", LanguageUtil.get(httpServletRequest, description)
		).put(
			"image", image
		).put(
			"title", LanguageUtil.get(httpServletRequest, title)
		).build();
	}

	@Override
	public String[] getObjectFolderExternalReferenceCodes() {
		if (_objectFolderExternalReferenceCode != null) {
			return new String[] {_objectFolderExternalReferenceCode};
		}

		String rootObjectEntryFolderExternalReferenceCode =
			getRootObjectEntryFolderExternalReferenceCode();

		if (rootObjectEntryFolderExternalReferenceCode == null) {
			return new String[0];
		}

		if (rootObjectEntryFolderExternalReferenceCode.equals(
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS)) {

			_objectFolderExternalReferenceCode =
				ObjectFolderConstants.
					EXTERNAL_REFERENCE_CODE_CONTENT_STRUCTURES;
		}

		if (rootObjectEntryFolderExternalReferenceCode.equals(
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_FILES)) {

			_objectFolderExternalReferenceCode =
				ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_FILE_TYPES;
		}

		return new String[] {_objectFolderExternalReferenceCode};
	}

	@Override
	public String getRootObjectEntryFolderExternalReferenceCode() {
		if (_rootObjectEntryFolderExternalReferenceCode != null) {
			return _rootObjectEntryFolderExternalReferenceCode;
		}

		if (objectEntryFolder == null) {
			return null;
		}

		_rootObjectEntryFolderExternalReferenceCode =
			objectEntryFolder.getExternalReferenceCode();

		String[] parts = StringUtil.split(
			objectEntryFolder.getTreePath(), CharPool.SLASH);

		if (parts.length > 2) {
			ObjectEntryFolder rootObjectEntryFolder =
				_objectEntryFolderLocalService.fetchObjectEntryFolder(
					GetterUtil.getLong(parts[1]));

			_rootObjectEntryFolderExternalReferenceCode =
				rootObjectEntryFolder.getExternalReferenceCode();
		}

		return _rootObjectEntryFolderExternalReferenceCode;
	}

	@Override
	protected String getCMSSectionFilterString() {
		return null;
	}

	private void _addBreadcrumbItem(
		JSONArray jsonArray, boolean active, String friendlyURL, String label) {

		jsonArray.put(
			JSONUtil.put(
				"active", active
			).put(
				"href", () -> friendlyURL
			).put(
				"label", label
			));
	}

	private final ObjectEntryFolderLocalService _objectEntryFolderLocalService;
	private String _objectFolderExternalReferenceCode;
	private String _rootObjectEntryFolderExternalReferenceCode;

}