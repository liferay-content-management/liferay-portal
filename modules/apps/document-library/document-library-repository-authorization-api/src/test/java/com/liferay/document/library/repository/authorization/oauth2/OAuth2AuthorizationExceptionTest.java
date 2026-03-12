/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.repository.authorization.oauth2;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Marco Leo
 */
public class OAuth2AuthorizationExceptionTest {

	@ClassRule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetErrorExceptionForAccessDenied() {
		OAuth2AuthorizationException exception =
			OAuth2AuthorizationException.getErrorException(
				"access_denied", "test description");

		Assert.assertTrue(
			exception instanceof OAuth2AuthorizationException.AccessDenied);
	}

	@Test
	public void testGetErrorExceptionForInvalidGrant() {
		OAuth2AuthorizationException exception =
			OAuth2AuthorizationException.getErrorException(
				"invalid_grant",
				"AADSTS70000: Provided grant is invalid or malformed");

		Assert.assertTrue(
			exception instanceof OAuth2AuthorizationException.InvalidGrant);
		Assert.assertTrue(
			exception.getMessage().contains(
				"invalid, expired, or revoked"));
	}

	@Test
	public void testGetErrorExceptionForUnknownError() {
		OAuth2AuthorizationException exception =
			OAuth2AuthorizationException.getErrorException(
				"unknown_error", "test description");

		Assert.assertNotNull(exception);
		Assert.assertFalse(
			exception instanceof OAuth2AuthorizationException.InvalidGrant);
		Assert.assertFalse(
			exception instanceof OAuth2AuthorizationException.AccessDenied);
	}

	@Test
	public void testInvalidGrantWithCause() {
		RuntimeException cause = new RuntimeException("original error");

		OAuth2AuthorizationException.InvalidGrant invalidGrant =
			new OAuth2AuthorizationException.InvalidGrant(
				"test description", cause);

		Assert.assertTrue(
			invalidGrant.getMessage().contains(
				"invalid, expired, or revoked"));
		Assert.assertEquals(cause, invalidGrant.getCause());
	}

}
