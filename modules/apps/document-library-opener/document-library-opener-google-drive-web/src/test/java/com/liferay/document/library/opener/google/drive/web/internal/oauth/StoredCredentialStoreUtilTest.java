/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.opener.google.drive.web.internal.oauth;

import com.google.api.client.auth.oauth2.StoredCredential;

import com.liferay.petra.function.UnsafeTriConsumer;
import com.liferay.portal.kernel.cluster.ClusterExecutorUtil;
import com.liferay.portal.kernel.io.Deserializer;
import com.liferay.portal.kernel.io.Serializer;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Marco Galluzzi
 */
public class StoredCredentialStoreUtilTest {

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

	@Before
	public void setUp() {
		for (int i = 0; i < _COMPANY_COUNT; i++) {
			_companyIds[i] = RandomTestUtil.randomLong();
		}

		for (int i = 0; i < _USER_COUNT; i++) {
			_userIds[i] = RandomTestUtil.randomLong();
		}

		for (int i = 0; i < _COMPANY_COUNT; i++) {
			for (int j = 0; j < _USER_COUNT; j++) {
				_storedCredentials[i][j] = _addStoredCredential();
			}
		}
	}

	@After
	public void tearDown() {
		Map<Long, Map<Long, StoredCredential>> storedCredentials =
			ReflectionTestUtil.getFieldValue(
				StoredCredentialStoreUtil.class, "_storedCredentials");

		storedCredentials.clear();
	}

	@Test
	public void testAdd() throws Throwable {
		_process(StoredCredentialStoreUtil::add);

		_process(
			(companyId, userId, storedCredential) -> Assert.assertEquals(
				storedCredential,
				StoredCredentialStoreUtil.get(companyId, userId)));
	}

	@Test
	public void testClear() throws Throwable {
		_process(StoredCredentialStoreUtil::add);

		StoredCredentialStoreUtil.clear();

		_process(
			(companyId, userId, storedCredential) -> Assert.assertNull(
				StoredCredentialStoreUtil.get(companyId, userId)));
	}

	@Test
	public void testDelete() throws Throwable {
		_process(StoredCredentialStoreUtil::add);

		StoredCredentialStoreUtil.delete(_companyIds[0], _userIds[0]);

		_process(
			(companyId, userId, storedCredential) -> {
				if ((companyId == _companyIds[0]) && (userId == _userIds[0])) {
					Assert.assertNull(
						StoredCredentialStoreUtil.get(companyId, userId));
				}
				else {
					Assert.assertEquals(
						storedCredential,
						StoredCredentialStoreUtil.get(companyId, userId));
				}
			});
	}

	@Test
	public void testGetNonexistentStoredCredential() {
		Assert.assertNull(
			StoredCredentialStoreUtil.get(
				RandomTestUtil.randomLong(), RandomTestUtil.randomLong()));
	}

	@Test
	public void testStoredCredentialIsSerializable()
		throws ClassNotFoundException {

		StoredCredential storedCredential = _addStoredCredential();

		Serializer serializer = new Serializer();

		serializer.writeObject(storedCredential);

		Deserializer deserializer = new Deserializer(serializer.toByteBuffer());

		StoredCredential deserializedStoredCredential =
			deserializer.readObject();

		Assert.assertEquals(storedCredential, deserializedStoredCredential);
	}

	private StoredCredential _addStoredCredential() {
		StoredCredential storedCredential = new StoredCredential();

		storedCredential.setAccessToken(RandomTestUtil.randomString());
		storedCredential.setExpirationTimeMilliseconds(
			RandomTestUtil.randomLong());
		storedCredential.setRefreshToken(RandomTestUtil.randomString());

		return storedCredential;
	}

	private void _process(
			UnsafeTriConsumer<Long, Long, StoredCredential, Exception>
				unsafeTriConsumer)
		throws Throwable {

		for (int i = 0; i < _COMPANY_COUNT; i++) {
			for (int j = 0; j < _USER_COUNT; j++) {
				unsafeTriConsumer.accept(
					_companyIds[i], _userIds[j], _storedCredentials[i][j]);
			}
		}
	}

	private static final int _COMPANY_COUNT = 3;

	private static final int _USER_COUNT = 3;

	private static final MockedStatic<ClusterExecutorUtil>
		_clusterExecutorUtilMockedStatic = Mockito.mockStatic(
			ClusterExecutorUtil.class);

	private final long[] _companyIds = new long[_COMPANY_COUNT];
	private final StoredCredential[][] _storedCredentials =
		new StoredCredential[_COMPANY_COUNT][_USER_COUNT];
	private final long[] _userIds = new long[_USER_COUNT];

}