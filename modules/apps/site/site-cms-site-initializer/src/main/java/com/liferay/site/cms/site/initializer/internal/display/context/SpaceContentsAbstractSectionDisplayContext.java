/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectDefinitionSettingLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * @author Roberto Díaz
 */
public class SpaceContentsAbstractSectionDisplayContext
	extends ContentsSectionDisplayContext {

	public SpaceContentsAbstractSectionDisplayContext(
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

		_assetLibraryId = assetLibraryId;
		_portal = portal;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	@Override
	public String getAPIURL() {
		return StringBundler.concat(
			super.getAPIURL(), "&page=", _PAGE, "&pageSize=", _PAGE_SIZE);
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

	public Map<String, Object> getHeaderProps() throws Exception {
		Group group = groupLocalService.fetchGroup(_assetLibraryId);

		String logoColor = "outline-0";
		String name = StringPool.BLANK;

		if (group != null) {
			UnicodeProperties unicodeProperties =
				group.getTypeSettingsProperties();

			logoColor = GetterUtil.get(
				unicodeProperties.get("logoColor"), "outline-0");

			name = group.getDescriptiveName(_themeDisplay.getLocale());
		}

		return HashMapBuilder.<String, Object>put(
			"viewAllLabel",
			language.get(httpServletRequest, "view-all-content")
		).put(
			"viewAllURL",
			StringBundler.concat(
				themeDisplay.getPathFriendlyURLPublic(),
				GroupConstants.CMS_FRIENDLY_URL, "/e/space-contents/",
				_portal.getClassNameId(DepotEntry.class), StringPool.SLASH,
				_assetLibraryId)
		).build();
	}

	private static final int _PAGE = 1;

	private static final int _PAGE_SIZE = 6;

	private final long _assetLibraryId;
	private final Portal _portal;
	private final ThemeDisplay _themeDisplay;

}