/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.info.item.provider;

import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Jan Brychta
 */
public class FileEntryInfoItemFriendlyURLProviderTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			_fileEntryInfoItemFriendlyURLProvider, "_portal",
			Mockito.mock(Portal.class));
	}

	@Test
	public void testGetFriendlyURLReturnsLocalizedUrlTitle() {
		FriendlyURLEntry friendlyURLEntry = Mockito.mock(
			FriendlyURLEntry.class);

		Mockito.when(
			friendlyURLEntry.getCategorizedUrlTitle("en_US")
		).thenReturn(
			"aa"
		);

		Mockito.when(
			friendlyURLEntry.getCategorizedUrlTitle("de_DE")
		).thenReturn(
			"aade"
		);

		FriendlyURLEntryLocalService friendlyURLEntryLocalService =
			Mockito.mock(FriendlyURLEntryLocalService.class);

		Mockito.when(
			friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				Mockito.anyLong(), Mockito.anyLong())
		).thenReturn(
			friendlyURLEntry
		);

		ReflectionTestUtil.setFieldValue(
			_fileEntryInfoItemFriendlyURLProvider,
			"_friendlyURLEntryLocalService", friendlyURLEntryLocalService);

		FileEntry fileEntry = Mockito.mock(FileEntry.class);

		Assert.assertEquals(
			"aade",
			_fileEntryInfoItemFriendlyURLProvider.getFriendlyURL(
				fileEntry, "de_DE"));
		Assert.assertEquals(
			"aa",
			_fileEntryInfoItemFriendlyURLProvider.getFriendlyURL(
				fileEntry, "en_US"));
	}

	@Test
	public void testGetFriendlyURLWhenMainFriendlyURLEntryIsNull() {
		FriendlyURLEntryLocalService friendlyURLEntryLocalService =
			Mockito.mock(FriendlyURLEntryLocalService.class);

		ReflectionTestUtil.setFieldValue(
			_fileEntryInfoItemFriendlyURLProvider,
			"_friendlyURLEntryLocalService", friendlyURLEntryLocalService);

		FileEntry fileEntry = Mockito.mock(FileEntry.class);

		long fileEntryId = RandomTestUtil.randomLong();

		Mockito.when(
			fileEntry.getFileEntryId()
		).thenReturn(
			fileEntryId
		);

		Assert.assertEquals(
			String.valueOf(fileEntryId),
			_fileEntryInfoItemFriendlyURLProvider.getFriendlyURL(
				fileEntry, RandomTestUtil.randomString()));
	}

	private final FileEntryInfoItemFriendlyURLProvider
		_fileEntryInfoItemFriendlyURLProvider =
			new FileEntryInfoItemFriendlyURLProvider();

}