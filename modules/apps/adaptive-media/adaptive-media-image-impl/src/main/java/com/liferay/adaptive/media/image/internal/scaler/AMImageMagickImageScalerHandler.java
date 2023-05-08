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

package com.liferay.adaptive.media.image.internal.scaler;

import com.liferay.adaptive.media.image.internal.configuration.AMImageMagickConfiguration;
import com.liferay.adaptive.media.image.scaler.AMImageScaler;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.image.ImageMagick;
import com.liferay.portal.kernel.image.ImageTool;
import com.liferay.portal.kernel.util.File;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;

import java.util.Arrays;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Roberto Díaz
 */
@Component(
	configurationPid = "com.liferay.adaptive.media.image.internal.configuration.AMImageMagickConfiguration",
	service = {}
)
public class AMImageMagickImageScalerHandler {

	@Activate
	@Modified
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		_amImageMagickConfiguration = ConfigurableUtil.createConfigurable(
			AMImageMagickConfiguration.class, properties);

		_serviceRegistration = bundleContext.registerService(
			AMImageScaler.class,
			new AMImageMagickImageScaler(_file, _imageMagick, _imageTool),
			HashMapDictionaryBuilder.put(
				"mime.type",
				Arrays.asList(
					_amImageMagickConfiguration.imageMagickSupportedMimeTypes())
			).build());
	}

	@Deactivate
	protected void deactivate() {
		_serviceRegistration.unregister();
	}

	private volatile AMImageMagickConfiguration _amImageMagickConfiguration;

	@Reference
	private File _file;

	@Reference
	private ImageMagick _imageMagick;

	@Reference
	private ImageTool _imageTool;

	private volatile ServiceRegistration<?> _serviceRegistration;

}