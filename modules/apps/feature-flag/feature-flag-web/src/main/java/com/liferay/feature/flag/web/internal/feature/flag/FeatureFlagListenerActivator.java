/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.feature.flag.web.internal.feature.flag;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(service = {})
public class FeatureFlagListenerActivator {

	@Activate
	protected void activate() {
		_featureFlagsBagProvider.activateFeatureFlagListeners();
	}

	@Reference
	private FeatureFlagsBagProvider _featureFlagsBagProvider;

}