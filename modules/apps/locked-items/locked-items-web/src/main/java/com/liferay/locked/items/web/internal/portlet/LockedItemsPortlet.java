/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.locked.items.web.internal.portlet;

import com.liferay.locked.items.constants.LockedItemsPortletKeys;
import com.liferay.locked.items.display.LockedItemsScreen;
import com.liferay.locked.items.web.internal.display.context.LockedItemsDisplayContext;
import com.liferay.locked.items.web.internal.util.LockedItemsScreenRetriever;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.Portal;

import java.io.IOException;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Galluzzi
 */
@Component(
	property = {
		"com.liferay.portlet.display-category=category.hidden",
		"com.liferay.portlet.instanceable=false",
		"com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=Locked Items",
		"javax.portlet.init-param.template-path=/META-INF/resources/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + LockedItemsPortletKeys.LOCKED_ITEMS,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=administrator",
		"javax.portlet.version=3.0"
	},
	service = Portlet.class
)
public class LockedItemsPortlet extends MVCPortlet {

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		renderRequest.setAttribute(
			LockedItemsDisplayContext.class.getName(),
			new LockedItemsDisplayContext(
				_portal.getHttpServletRequest(renderRequest),
				_lockedItemsScreenRetriever, renderResponse));

		for (LockedItemsScreen lockedItemsScreen :
				_lockedItemsScreenRetriever.getLockedItemsScreens()) {

			lockedItemsScreen.setAttributes(renderRequest, renderResponse);
		}

		super.render(renderRequest, renderResponse);
	}

	@Override
	protected String getTitle(RenderRequest renderRequest) {
		if (!FeatureFlagManagerUtil.isEnabled("LPD-11003")) {
			return translate(renderRequest, "locked-pages");
		}

		return super.getTitle(renderRequest);
	}

	@Reference
	private LockedItemsScreenRetriever _lockedItemsScreenRetriever;

	@Reference
	private Portal _portal;

}