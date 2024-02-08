/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.locked.items.web.internal.util;

import com.liferay.locked.items.display.LockedItemsScreen;

import java.util.List;

/**
 * @author Marco Galluzzi
 */
public interface LockedItemsScreenRetriever {

	public LockedItemsScreen getLockedItemsScreen(String key);

	public List<LockedItemsScreen> getLockedItemsScreens();

	public int getLockedItemsScreensCount();

}