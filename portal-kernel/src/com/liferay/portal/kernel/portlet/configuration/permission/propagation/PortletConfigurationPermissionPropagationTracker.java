/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.portlet.configuration.permission.propagation;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.model.ClassedModel;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.portlet.PortletIdCodec;

import org.osgi.framework.BundleContext;

/**
 * @author To Trinh
 */
public class PortletConfigurationPermissionPropagationTracker {

	public static <T extends ClassedModel>
		PortletConfigurationPermissionPropagation
			getPortletConfigurationPermissionPropagation(
				String portletResource) {

		String rootPortletId = PortletIdCodec.decodePortletName(
			portletResource);

		return (PortletConfigurationPermissionPropagation)
			_serviceTrackerMap.getService(rootPortletId);
	}

	private static final ServiceTrackerMap
		<String, PortletConfigurationPermissionPropagation> _serviceTrackerMap;

	static {
		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, PortletConfigurationPermissionPropagation.class,
			"javax.portlet.name");
	}

}