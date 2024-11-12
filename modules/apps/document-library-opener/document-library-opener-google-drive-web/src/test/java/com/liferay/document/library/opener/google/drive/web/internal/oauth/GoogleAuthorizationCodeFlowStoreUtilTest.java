/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.opener.google.drive.web.internal.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;

import com.liferay.portal.kernel.cluster.ClusterExecutorUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.IOException;

import java.security.GeneralSecurityException;

import java.util.Collections;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Marco Galluzzi
 */
public class GoogleAuthorizationCodeFlowStoreUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		Mockito.when(
			ClusterExecutorUtil.isEnabled()
		).thenReturn(
			false
		);
	}

	@AfterClass
	public static void tearDownClass() {
		_clusterExecutorUtilMockedStatic.close();
	}

	@Test
	public void testAdd() throws GeneralSecurityException, IOException {
		GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow1 =
			_addGoogleAuthorizationCodeFlow();

		long companyId = RandomTestUtil.randomInt();

		GoogleAuthorizationCodeFlowStoreUtil.add(
			companyId, googleAuthorizationCodeFlow1);

		GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow2 =
			GoogleAuthorizationCodeFlowStoreUtil.get(companyId);

		Assert.assertEquals(
			googleAuthorizationCodeFlow1, googleAuthorizationCodeFlow2);
	}

	@Test
	public void testClear() throws GeneralSecurityException, IOException {
		GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow =
			_addGoogleAuthorizationCodeFlow();

		long companyId = RandomTestUtil.randomInt();

		GoogleAuthorizationCodeFlowStoreUtil.add(
			companyId, googleAuthorizationCodeFlow);

		GoogleAuthorizationCodeFlowStoreUtil.clear();

		Assert.assertNull(GoogleAuthorizationCodeFlowStoreUtil.get(companyId));
	}

	@Test
	public void testGetWithEmptyGoogleAuthorizationCodeFlowStore() {
		Assert.assertNull(
			GoogleAuthorizationCodeFlowStoreUtil.get(
				RandomTestUtil.randomInt()));
	}

	private GoogleAuthorizationCodeFlow _addGoogleAuthorizationCodeFlow()
		throws GeneralSecurityException, IOException {

		return new GoogleAuthorizationCodeFlow(
			GoogleNetHttpTransport.newTrustedTransport(),
			JacksonFactory.getDefaultInstance(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(),
			Collections.singleton(DriveScopes.DRIVE_FILE));
	}

	private static final MockedStatic<ClusterExecutorUtil>
		_clusterExecutorUtilMockedStatic = Mockito.mockStatic(
			ClusterExecutorUtil.class);

}