/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.feature.flag.web.internal.feature.flag.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.feature.flag.FeatureFlagListener;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.util.promise.Promise;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class FeatureFlagsBagProviderImplTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testActivate() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(
			FeatureFlagsBagProviderImplTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		Set<Boolean> featureFlagsBagProviderAvailabilities =
			ConcurrentHashMap.newKeySet();

		ServiceRegistration<FeatureFlagListener> serviceRegistration =
			bundleContext.registerService(
				FeatureFlagListener.class,
				(companyId, featureFlagKey, enabled) -> {
					ServiceReference<?> serviceReference =
						bundleContext.getServiceReference(
							_CLASS_NAME_FEATURE_FLAGS_BAG_PROVIDER);

					try {
						featureFlagsBagProviderAvailabilities.add(
							bundleContext.getService(serviceReference) != null);
					}
					finally {
						bundleContext.ungetService(serviceReference);
					}
				},
				HashMapDictionaryBuilder.put(
					"feature.flag.key", "*"
				).build());

		try {
			featureFlagsBagProviderAvailabilities.clear();

			ServiceReference<?> serviceReference =
				bundleContext.getServiceReference(
					_CLASS_NAME_FEATURE_FLAGS_BAG_PROVIDER);

			ComponentDescriptionDTO componentDescriptionDTO =
				_serviceComponentRuntime.getComponentDescriptionDTO(
					serviceReference.getBundle(),
					"com.liferay.feature.flag.web.internal.feature.flag." +
						"FeatureFlagsBagProviderImpl");

			try {
				Promise<Void> promise =
					_serviceComponentRuntime.disableComponent(
						componentDescriptionDTO);

				promise.getValue();
			}
			finally {
				Promise<Void> promise =
					_serviceComponentRuntime.enableComponent(
						componentDescriptionDTO);

				promise.getValue();
			}

			Assert.assertEquals(
				Collections.singleton(true),
				featureFlagsBagProviderAvailabilities);
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	private static final String _CLASS_NAME_FEATURE_FLAGS_BAG_PROVIDER =
		"com.liferay.feature.flag.web.internal.feature.flag." +
			"FeatureFlagsBagProvider";

	@Inject
	private ServiceComponentRuntime _serviceComponentRuntime;

}