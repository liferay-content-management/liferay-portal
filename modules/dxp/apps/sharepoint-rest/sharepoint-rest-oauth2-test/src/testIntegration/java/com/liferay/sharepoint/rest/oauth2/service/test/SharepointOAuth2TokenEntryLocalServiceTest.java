/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.sharepoint.rest.oauth2.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;
import com.liferay.sharepoint.rest.oauth2.model.SharepointOAuth2TokenEntry;
import com.liferay.sharepoint.rest.oauth2.service.SharepointOAuth2TokenEntryLocalServiceUtil;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marco Leo
 */
@RunWith(Arquillian.class)
public class SharepointOAuth2TokenEntryLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PersistenceTestRule.INSTANCE);

	@Test
	public void testAddAndDeleteSharepointOAuth2TokenEntry() throws Exception {
		long userId = TestPropsValues.getUserId();
		String configurationPid = RandomTestUtil.randomString();

		Calendar calendar = Calendar.getInstance();

		calendar.add(Calendar.HOUR, 1);

		Date expirationDate = calendar.getTime();

		SharepointOAuth2TokenEntry sharepointOAuth2TokenEntry =
			SharepointOAuth2TokenEntryLocalServiceUtil.
				addSharepointOAuth2TokenEntry(
					userId, configurationPid, "testAccessToken",
					"testRefreshToken", expirationDate);

		Assert.assertNotNull(sharepointOAuth2TokenEntry);
		Assert.assertEquals(
			"testAccessToken", sharepointOAuth2TokenEntry.getAccessToken());
		Assert.assertEquals(
			"testRefreshToken",
			sharepointOAuth2TokenEntry.getRefreshToken());

		SharepointOAuth2TokenEntry fetchedEntry =
			SharepointOAuth2TokenEntryLocalServiceUtil.
				fetchSharepointOAuth2TokenEntry(userId, configurationPid);

		Assert.assertNotNull(fetchedEntry);

		SharepointOAuth2TokenEntryLocalServiceUtil.
			deleteSharepointOAuth2TokenEntry(userId, configurationPid);

		fetchedEntry =
			SharepointOAuth2TokenEntryLocalServiceUtil.
				fetchSharepointOAuth2TokenEntry(userId, configurationPid);

		Assert.assertNull(fetchedEntry);
	}

	@Test
	public void testAddSharepointOAuth2TokenEntryUpdatesExisting()
		throws Exception {

		long userId = TestPropsValues.getUserId();
		String configurationPid = RandomTestUtil.randomString();

		Calendar calendar = Calendar.getInstance();

		calendar.add(Calendar.HOUR, 1);

		Date expirationDate = calendar.getTime();

		SharepointOAuth2TokenEntryLocalServiceUtil.
			addSharepointOAuth2TokenEntry(
				userId, configurationPid, "originalAccessToken",
				"originalRefreshToken", expirationDate);

		calendar.add(Calendar.HOUR, 1);

		Date newExpirationDate = calendar.getTime();

		SharepointOAuth2TokenEntryLocalServiceUtil.
			addSharepointOAuth2TokenEntry(
				userId, configurationPid, "updatedAccessToken",
				"updatedRefreshToken", newExpirationDate);

		SharepointOAuth2TokenEntry fetchedEntry =
			SharepointOAuth2TokenEntryLocalServiceUtil.
				fetchSharepointOAuth2TokenEntry(userId, configurationPid);

		Assert.assertNotNull(fetchedEntry);
		Assert.assertEquals(
			"updatedAccessToken", fetchedEntry.getAccessToken());
		Assert.assertEquals(
			"updatedRefreshToken", fetchedEntry.getRefreshToken());

		SharepointOAuth2TokenEntryLocalServiceUtil.
			deleteSharepointOAuth2TokenEntry(userId, configurationPid);
	}

	@Test
	public void testFetchNonExistentTokenEntry() throws Exception {
		long userId = TestPropsValues.getUserId();
		String configurationPid = RandomTestUtil.randomString();

		SharepointOAuth2TokenEntry fetchedEntry =
			SharepointOAuth2TokenEntryLocalServiceUtil.
				fetchSharepointOAuth2TokenEntry(userId, configurationPid);

		Assert.assertNull(fetchedEntry);
	}

	@Test
	public void testTokenEntryWithExpiredDate() throws Exception {
		long userId = TestPropsValues.getUserId();
		String configurationPid = RandomTestUtil.randomString();

		Calendar calendar = Calendar.getInstance();

		calendar.add(Calendar.HOUR, -1);

		Date expiredDate = calendar.getTime();

		SharepointOAuth2TokenEntry sharepointOAuth2TokenEntry =
			SharepointOAuth2TokenEntryLocalServiceUtil.
				addSharepointOAuth2TokenEntry(
					userId, configurationPid, "expiredAccessToken",
					"expiredRefreshToken", expiredDate);

		Assert.assertNotNull(sharepointOAuth2TokenEntry);

		SharepointOAuth2TokenEntry fetchedEntry =
			SharepointOAuth2TokenEntryLocalServiceUtil.
				fetchSharepointOAuth2TokenEntry(userId, configurationPid);

		Assert.assertNotNull(fetchedEntry);
		Assert.assertEquals(
			"expiredAccessToken", fetchedEntry.getAccessToken());

		Assert.assertTrue(fetchedEntry.getExpirationDate().before(new Date()));

		SharepointOAuth2TokenEntryLocalServiceUtil.
			deleteSharepointOAuth2TokenEntry(userId, configurationPid);
	}

}
