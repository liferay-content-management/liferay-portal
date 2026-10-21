/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.language.extender.internal;

import com.liferay.portal.kernel.dependency.manager.DependencyManagerSyncUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;

/**
 * @author Brian Wulbern
 */
public class LanguageResourcesExtenderTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDeactivate() throws Exception {
		BundleContext deactivatedBundleContext = Mockito.mock(
			BundleContext.class);

		LanguageResourcesExtender deactivatedLanguageResourcesExtender =
			new LanguageResourcesExtender();

		deactivatedLanguageResourcesExtender.activate(deactivatedBundleContext);

		deactivatedLanguageResourcesExtender.deactivate();

		BundleContext bundleContext = Mockito.mock(BundleContext.class);

		LanguageResourcesExtender languageResourcesExtender =
			new LanguageResourcesExtender();

		languageResourcesExtender.activate(bundleContext);

		Runnable runnable = ReflectionTestUtil.getFieldValue(
			DependencyManagerSyncUtil.class,
			"_syncCallableDefaultNoticeableFuture");

		runnable.run();

		Mockito.verify(
			deactivatedBundleContext, Mockito.never()
		).addServiceListener(
			Mockito.any(ServiceListener.class), Mockito.anyString()
		);

		Mockito.verify(
			bundleContext
		).addServiceListener(
			Mockito.any(ServiceListener.class), Mockito.anyString()
		);

		languageResourcesExtender.deactivate();
	}

}