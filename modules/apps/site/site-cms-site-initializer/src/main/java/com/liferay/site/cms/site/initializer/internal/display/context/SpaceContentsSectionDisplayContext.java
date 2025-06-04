/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectDefinitionSettingLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * @author Roberto Díaz
 */
public class SpaceContentsSectionDisplayContext
	extends ContentsSectionDisplayContext {

	public SpaceContentsSectionDisplayContext(
		long assetLibraryId, DepotEntryLocalService depotEntryLocalService,
		GroupLocalService groupLocalService,
		HttpServletRequest httpServletRequest, Language language,
		ObjectDefinitionService objectDefinitionService,
		ObjectDefinitionSettingLocalService objectDefinitionSettingLocalService,
		Portal portal) {

		super(
			depotEntryLocalService, groupLocalService, httpServletRequest,
			language, objectDefinitionService,
			objectDefinitionSettingLocalService, portal);

		this.assetLibraryId = assetLibraryId;
		this.portal = portal;
	}

	@Override
	public String getAPIURL() {
		String[] objectFolderExternalReferenceCodes =
			getObjectFolderExternalReferenceCodes();

		StringBundler sb = new StringBundler(13);

		sb.append("/o/search/v1.0/search?emptySearch=true&filter=(");

		sb.append(" groupIds in (");
		sb.append(assetLibraryId);
		sb.append(") and ");

		if (objectEntryFolder != null) {
			sb.append(" folderId eq ");
			sb.append(objectEntryFolder.getObjectEntryFolderId());
			sb.append(" and ");
		}

		sb.append("(objectFolderExternalReferenceCode in ('");
		sb.append(StringUtil.merge(objectFolderExternalReferenceCodes, "','"));
		sb.append("')");

		String cmsSectionFilterString = getCMSSectionFilterString();

		if (Validator.isNotNull(cmsSectionFilterString)) {
			sb.append(" or ");
			sb.append(cmsSectionFilterString);
		}

		sb.append("))&nestedFields=embedded,file.thumbnailURL");

		return sb.toString();
	}

	@Override
	public Map<String, Object> getEmptyState() {
		return HashMapBuilder.<String, Object>put(
			"description",
			language.get(
				httpServletRequest,
				"create-and-manage-content-within-this-space")
		).put(
			"image", "/states/cms_empty_state_content.svg"
		).put(
			"title", language.get(httpServletRequest, "no-content-yet")
		).build();
	}

	protected final long assetLibraryId;
	protected final Portal portal;

}