/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.locked.items.web.internal.util;

import com.liferay.locked.items.display.LockedItemsScreen;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Marco Galluzzi
 */
@Component(service = LockedItemsScreenRetriever.class)
public class LockedItemsScreenRetrieverImpl
	implements LockedItemsScreenRetriever {

	@Override
	public LockedItemsScreen getLockedItemsScreen(String key) {
		return _serviceTrackerMap.getService(key);
	}

	@Override
	public List<LockedItemsScreen> getLockedItemsScreens() {
		List<LockedItemsScreen> lockedItemsScreens = new ArrayList<>();

		for (LockedItemsScreen lockedItemsScreen :
				_serviceTrackerMap.values()) {

			if (lockedItemsScreen.isVisible()) {
				lockedItemsScreens.add(lockedItemsScreen);
			}
		}

		return lockedItemsScreens;
	}

	@Override
	public int getLockedItemsScreensCount() {
		List<LockedItemsScreen> lockedItemsScreens = getLockedItemsScreens();

		return lockedItemsScreens.size();
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, LockedItemsScreen.class, null,
			(serviceReference, emitter) -> {
				LockedItemsScreen lockedItemsScreen = bundleContext.getService(
					serviceReference);

				emitter.emit(lockedItemsScreen.getKey());
			});
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private ServiceTrackerMap<String, LockedItemsScreen> _serviceTrackerMap;

}