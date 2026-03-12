/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.sharepoint.rest.repository.internal.document.library.repository.authorization.oauth2;

import com.liferay.document.library.repository.authorization.oauth2.Token;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.sharepoint.rest.oauth2.model.SharepointOAuth2TokenEntry;

import java.lang.reflect.Constructor;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Marco Leo
 */
public class SharepointRepositoryTokenTest {

	@ClassRule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testIsExpiredWithExpiredDate() throws Exception {
		Calendar calendar = Calendar.getInstance();

		calendar.add(Calendar.HOUR, -1);

		Token token = _createToken(
			"testAccessToken", "testRefreshToken", calendar.getTime());

		Assert.assertTrue(token.isExpired());
	}

	@Test
	public void testIsExpiredWithFutureDate() throws Exception {
		Calendar calendar = Calendar.getInstance();

		calendar.add(Calendar.HOUR, 1);

		Token token = _createToken(
			"testAccessToken", "testRefreshToken", calendar.getTime());

		Assert.assertFalse(token.isExpired());
	}

	@Test
	public void testIsExpiredWithNullDate() {
		Token token = SharepointRepositoryToken.newInstance("testAccessToken");

		Assert.assertNotNull(token);
		Assert.assertFalse(token.isExpired());
	}

	@Test
	public void testNewInstanceWithNullAccessToken() {
		Token token = SharepointRepositoryToken.newInstance((String)null);

		Assert.assertNull(token);
	}

	@Test
	public void testNewInstanceWithNullEntry() {
		Token token = SharepointRepositoryToken.newInstance(
			(SharepointOAuth2TokenEntry)null);

		Assert.assertNull(token);
	}

	@Test
	public void testNewInstanceWithValidEntry() throws Exception {
		Calendar calendar = Calendar.getInstance();

		calendar.add(Calendar.HOUR, 1);

		Date futureDate = calendar.getTime();

		Token token = _createToken(
			"testAccessToken", "testRefreshToken", futureDate);

		Assert.assertEquals("testAccessToken", token.getAccessToken());
		Assert.assertEquals("testRefreshToken", token.getRefreshToken());
		Assert.assertEquals(futureDate, token.getExpirationDate());
	}

	private Token _createToken(
			String accessToken, String refreshToken, Date expirationDate)
		throws Exception {

		Constructor<SharepointRepositoryToken> constructor =
			SharepointRepositoryToken.class.getDeclaredConstructor(
				String.class, String.class, Date.class);

		constructor.setAccessible(true);

		return constructor.newInstance(accessToken, refreshToken, expirationDate);
	}

}
