/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.depot.model.DepotEntry;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.TabsItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.TabsItemList;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.TabsItemListBuilder;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.util.SpaceAbstractHeaderUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Roberto Díaz
 */
public class ViewSpaceMembersAbstractSectionDisplayContext {

	public ViewSpaceMembersAbstractSectionDisplayContext(
		long groupId, HttpServletRequest httpServletRequest, Language language,
		Portal portal, UserGroupLocalService userGroupLocalService) {

		_groupId = groupId;
		_httpServletRequest = httpServletRequest;
		_language = language;
		_portal = portal;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		_userGroupLocalService = userGroupLocalService;
	}

	public String getAPIURL(String type) {
		StringBundler sb = new StringBundler(5);

		sb.append("/o/headless-asset-library/v1.0/asset-libraries/");
		sb.append(_groupId);
		sb.append("/");
		sb.append(type);
		sb.append("?page=1&pageSize=6");

		return sb.toString();
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems(String type) {
		return ListUtil.fromArray(
			new FDSActionDropdownItem(
				_language.get(
					_httpServletRequest,
					"are-you-sure-you-want-to-delete-this-user"),
				StringBundler.concat(
					"/o/headless-asset-library/v1.0/asset-libraries/", _groupId,
					"/", type, "/{id}"),
				"times-small", "delete",
				_language.get(_httpServletRequest, "delete"), "delete", null,
				"headless"));
	}

	public Map<String, Object> getHeaderProps() throws Exception {
		return SpaceAbstractHeaderUtil.getSpaceAbstractHeaderProps(
			_httpServletRequest, "view-all-members", "members",
			StringPool.BLANK);
	}

	public List<TabsItem> getTabsItems() {
		TabsItemList tabsItemList = TabsItemListBuilder.add(
			tabsItem -> {
				tabsItem.setActive(true);
				tabsItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "users"));
			}
		).build();

		if (hasUserGroups()) {
			tabsItemList.add(
				tabsItem -> {
					tabsItem.setActive(true);
					tabsItem.setLabel(
						LanguageUtil.get(_httpServletRequest, "user-groups"));
				});
		}

		return tabsItemList;
	}

	public boolean hasUserGroups() {
		if (_userGroupLocalService.getGroupUserGroupsCount(_groupId) > 0) {
			return true;
		}

		return false;
	}

	private final long _groupId;
	private final HttpServletRequest _httpServletRequest;
	private final Language _language;
	private final Portal _portal;
	private final ThemeDisplay _themeDisplay;
	private final UserGroupLocalService _userGroupLocalService;

}