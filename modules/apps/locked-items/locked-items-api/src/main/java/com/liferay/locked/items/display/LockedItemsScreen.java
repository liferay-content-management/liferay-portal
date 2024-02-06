/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.locked.items.display;

import java.util.Locale;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.ServletContext;

/**
 * @author Marco Galluzzi
 */
public interface LockedItemsScreen {

	public String getDescription(Locale locale);

	public String getJspPath();

	public String getKey();

	public String getName(Locale locale);

	public ServletContext getServletContext();

	public default boolean isVisible() {
		return true;
	}

	public void setAttributes(
		RenderRequest renderRequest, RenderResponse renderResponse);

}