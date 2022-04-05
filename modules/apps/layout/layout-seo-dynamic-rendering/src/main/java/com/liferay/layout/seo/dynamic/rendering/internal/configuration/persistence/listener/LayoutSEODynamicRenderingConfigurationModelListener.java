/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.layout.seo.dynamic.rendering.internal.configuration.persistence.listener;

import com.liferay.layout.seo.dynamic.rendering.internal.LayoutSEODynamicRenderingLinkManagerImpl;
import com.liferay.portal.configuration.persistence.listener.ConfigurationModelListener;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;

import java.util.Dictionary;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;

/**
 * @author Jamie Sammons
 */
@Component(
	configurationPid = "com.liferay.layout.seo.dynamic.rendering.internal.configuration.LayoutSEODynamicRenderingConfiguration",
	immediate = true,
	property = "model.class.name=com.liferay.layout.seo.dynamic.rendering.internal.configuration.LayoutSEODynamicRenderingConfiguration",
	service = ConfigurationModelListener.class
)
public class LayoutSEODynamicRenderingConfigurationModelListener
	implements ConfigurationModelListener {

	@Override
	public void onAfterSave(String pid, Dictionary<String, Object> properties) {
		try {
			ComponentDescriptionDTO componentDescriptionDTO =
				_serviceComponentRuntime.getComponentDescriptionDTO(
					_bundleContext.getBundle(),
					LayoutSEODynamicRenderingLinkManagerImpl.class.getName());

			Configuration configuration = _getConfiguration();

			Dictionary<String, Object> configurationProperties =
				configuration.getProperties();

			if (GetterUtil.getBoolean(properties.get("enabled"))) {
				if (configurationProperties == null) {
					configurationProperties = new HashMapDictionary<>();
				}

				String className =
					LayoutSEODynamicRenderingLinkManagerImpl.class.getName();

				configurationProperties.put(
					"_layoutSEOLinkManager.target",
					"(component.name=" + className + ")");

				_serviceComponentRuntime.enableComponent(
					componentDescriptionDTO);
			}
			else if (configurationProperties != null) {
				configurationProperties.remove("_layoutSEOLinkManager.target");

				_serviceComponentRuntime.disableComponent(
					componentDescriptionDTO);
			}

			configuration.update(configurationProperties);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Activate
	@Modified
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		_bundleContext = bundleContext;

		ComponentDescriptionDTO componentDescriptionDTO =
			_serviceComponentRuntime.getComponentDescriptionDTO(
				_bundleContext.getBundle(),
				LayoutSEODynamicRenderingLinkManagerImpl.class.getName());

		if (GetterUtil.getBoolean(properties.get("enabled"))) {
			_serviceComponentRuntime.enableComponent(componentDescriptionDTO);
		}
		else {
			_serviceComponentRuntime.disableComponent(componentDescriptionDTO);
		}
	}

	private Configuration _getConfiguration() throws Exception {
		return _configurationAdmin.getConfiguration(
			"com.liferay.layout.seo.web.internal.servlet.taglib." +
				"OpenGraphTopHeadDynamicInclude",
			"?");
	}

	private volatile BundleContext _bundleContext;

	@Reference
	private ConfigurationAdmin _configurationAdmin;

	@Reference
	private ServiceComponentRuntime _serviceComponentRuntime;

}