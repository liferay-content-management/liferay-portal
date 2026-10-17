/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.bundle;

import com.liferay.portal.kernel.servlet.InitialRequestSyncUtil;
import com.liferay.portal.kernel.test.rule.NewEnv;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;

/**
 * @author Brian Wulbern
 */
@NewEnv(type = NewEnv.Type.JVM)
public class BatchEngineBundleTrackerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testDeactivate() throws Exception {
		BundleContext deactivatedBundleContext = Mockito.mock(
			BundleContext.class);

		Mockito.when(
			deactivatedBundleContext.createFilter(Mockito.anyString())
		).thenThrow(
			new IllegalStateException(RandomTestUtil.randomString())
		);

		BatchEngineBundleTracker deactivatedBatchEngineBundleTracker =
			new BatchEngineBundleTracker();

		deactivatedBatchEngineBundleTracker.activate(
			deactivatedBundleContext, Map.of());

		deactivatedBatchEngineBundleTracker.deactivate();

		BundleContext bundleContext = Mockito.mock(BundleContext.class);

		Mockito.when(
			bundleContext.createFilter(Mockito.anyString())
		).thenReturn(
			Mockito.mock(Filter.class)
		);

		BatchEngineBundleTracker batchEngineBundleTracker =
			new BatchEngineBundleTracker();

		batchEngineBundleTracker.activate(bundleContext, Map.of());

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				InitialRequestSyncUtil.class.getName(), LoggerTestUtil.ERROR)) {

			InitialRequestSyncUtil.sync();

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertTrue(logEntries.toString(), logEntries.isEmpty());
		}

		Mockito.verify(
			bundleContext, Mockito.atLeastOnce()
		).createFilter(
			Mockito.anyString()
		);

		Mockito.verify(
			deactivatedBundleContext, Mockito.never()
		).createFilter(
			Mockito.anyString()
		);
	}

}