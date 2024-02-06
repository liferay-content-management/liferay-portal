/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.locked.items.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.VerticalNavItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.VerticalNavItemList;
import com.liferay.locked.items.display.LockedItemsScreen;
import com.liferay.locked.items.web.internal.util.LockedItemsScreenRetriever;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Objects;

import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Marco Galluzzi
 */
public class LockedItemsDisplayContext {

	public LockedItemsDisplayContext(
		HttpServletRequest httpServletRequest,
		LockedItemsScreenRetriever lockedItemsScreenRetriever,
		RenderResponse renderResponse) {

		_httpServletRequest = httpServletRequest;
		_lockedItemsScreenRetriever = lockedItemsScreenRetriever;
		_renderResponse = renderResponse;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public LockedItemsScreen getLockedItemsScreen() {
		if (_lockedItemsScreen != null) {
			return _lockedItemsScreen;
		}

		_lockedItemsScreen = _lockedItemsScreenRetriever.getLockedItemsScreen(
			getNavigation());

		return _lockedItemsScreen;
	}

	public String getNavigation() {
		if (Validator.isNotNull(_navigation)) {
			return _navigation;
		}

		_navigation = ParamUtil.getString(
			_httpServletRequest, "navigation", "layouts");

		return _navigation;
	}

	public VerticalNavItemList getVerticalNavItemList() {
		VerticalNavItemList verticalNavItemList = new VerticalNavItemList();

		for (LockedItemsScreen lockedItemsScreen :
				_lockedItemsScreenRetriever.getLockedItemsScreens()) {

			verticalNavItemList.add(
				_getVerticalNavItemUnsafeConsumer(
					lockedItemsScreen.getKey(),
					lockedItemsScreen.getName(_themeDisplay.getLocale())));
		}

		return verticalNavItemList;
	}

	private UnsafeConsumer<VerticalNavItem, Exception>
		_getVerticalNavItemUnsafeConsumer(String key, String name) {

		return verticalNavItem -> {
			verticalNavItem.setActive(Objects.equals(getNavigation(), key));
			verticalNavItem.setHref(
				PortletURLBuilder.createRenderURL(
					_renderResponse
				).setMVCPath(
					"/view.jsp"
				).setNavigation(
					key
				).buildString());
			verticalNavItem.setId(name);
			verticalNavItem.setLabel(name);
		};
	}

	private final HttpServletRequest _httpServletRequest;
	private LockedItemsScreen _lockedItemsScreen;
	private final LockedItemsScreenRetriever _lockedItemsScreenRetriever;
	private String _navigation;
	private final RenderResponse _renderResponse;
	private final ThemeDisplay _themeDisplay;

}