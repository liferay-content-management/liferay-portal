/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.app.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.service.CTCollectionLocalService;
import com.liferay.document.library.app.service.test.util.DLAppServiceTestUtil;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.document.library.test.util.BaseDLAppTestCase;
import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.service.ExpandoColumnLocalService;
import com.liferay.expando.kernel.util.ExpandoBridgeFactory;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.comment.CommentManagerUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alexander Chow
 */
@RunWith(Arquillian.class)
public class DLAppServiceWhenDeletingAFileEntryTest extends BaseDLAppTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testShouldDeleteDiscussion() throws Exception {
		FileEntry fileEntry = DLAppServiceTestUtil.addFileEntry(
			group.getGroupId(), parentFolder.getFolderId());

		dlAppService.deleteFileEntry(fileEntry.getFileEntryId());

		Assert.assertFalse(
			CommentManagerUtil.hasDiscussion(
				DLFileEntryConstants.getClassName(),
				fileEntry.getFileEntryId()));
	}

	@Test
	public void testShouldDeleteFieEntryWithCustomFieldInPublication()
		throws Exception {

		ExpandoBridge expandoBridge = _expandoBridgeFactory.getExpandoBridge(
			group.getCompanyId(), DLFileEntry.class.getName());

		expandoBridge.addAttribute(
			RandomTestUtil.randomString(), ExpandoColumnConstants.BOOLEAN);

		FileEntry fileEntry = DLAppServiceTestUtil.addFileEntry(
			group.getGroupId(), parentFolder.getFolderId());

		CTCollection ctCollection = _ctCollectionLocalService.addCTCollection(
			null, group.getCompanyId(), TestPropsValues.getUserId(), 0,
			RandomTestUtil.randomString(), null);

		try (SafeCloseable safeCloseable =
				CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(
					ctCollection.getCtCollectionId())) {

			dlAppService.deleteFileEntry(fileEntry.getFileEntryId());
		}
	}

	@Inject
	private CTCollectionLocalService _ctCollectionLocalService;

	@Inject
	private ExpandoBridgeFactory _expandoBridgeFactory;

	@Inject
	private ExpandoColumnLocalService _expandoColumnLocalService;

}