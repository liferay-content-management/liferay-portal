/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.locked.layouts.web.internal.locked.items.display;

import com.liferay.layout.locked.layouts.web.internal.display.context.LockedLayoutsDisplayContext;
import com.liferay.layout.manager.LayoutLockManager;
import com.liferay.locked.items.display.LockedItemsScreen;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.util.Portal;

import java.util.Locale;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.ServletContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Galluzzi
 */
@Component(service = LockedItemsScreen.class)
public class LayoutLockedItemsScreen implements LockedItemsScreen {

	@Override
	public String getDescription(Locale locale) {
		return _language.get(
			locale,
			"administrators-can-manually-unlock-pages-that-are-being-used-by-" +
				"other-users");
	}

	@Override
	public String getJspPath() {
		return "/locked-items/view.jsp";
	}

	@Override
	public String getKey() {
		return "layouts";
	}

	@Override
	public String getName(Locale locale) {
		return _language.get(locale, "pages");
	}

	@Override
	public ServletContext getServletContext() {
		return _servletContext;
	}

	@Override
	public boolean isVisible() {
		if (FeatureFlagManagerUtil.isEnabled("LPS-180328")) {
			return true;
		}

		return false;
	}

	@Override
	public void setAttributes(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		renderRequest.setAttribute(
			LockedLayoutsDisplayContext.class.getName(),
			new LockedLayoutsDisplayContext(
				_language, _layoutLocalService, _layoutLockManager,
				_portal.getLiferayPortletRequest(renderRequest),
				_portal.getLiferayPortletResponse(renderResponse), _portal));
	}

	@Reference
	private Language _language;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutLockManager _layoutLockManager;

	@Reference
	private Portal _portal;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.layout.locked.layouts.web)"
	)
	private ServletContext _servletContext;

}