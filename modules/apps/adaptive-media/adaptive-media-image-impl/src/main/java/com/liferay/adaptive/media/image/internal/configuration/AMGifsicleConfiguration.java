/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.adaptive.media.image.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Sam Ziemer
 */
@ExtendedObjectClassDefinition(category = "adaptive-media")
@Meta.OCD(
	id = "com.liferay.adaptive.media.image.internal.configuration.AMGifsicleConfiguration",
	localization = "content/Language",
	name = "adaptive-media-gifsicle-configuration-name"
)
public interface AMGifsicleConfiguration {

	/**
	 * Set this to <code>true</code> to enable animated gif image scaling with
	 * gifsicle library. See https://www.lcdf.org/gifsicle for more information.
	 */
	@Meta.AD(
		deflt = "false", description = "gifsicle-enabled-key-description",
		name = "gifsicle-enabled", required = false
	)
	public boolean gifsicleEnabled();

}