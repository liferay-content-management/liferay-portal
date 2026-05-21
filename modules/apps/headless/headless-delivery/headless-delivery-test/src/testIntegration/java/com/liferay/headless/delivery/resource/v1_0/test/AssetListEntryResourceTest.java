/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.list.constants.AssetListEntryTypeConstants;
import com.liferay.asset.list.service.AssetListEntryLocalService;
import com.liferay.headless.delivery.client.dto.v1_0.AssetListEntry;
import com.liferay.headless.delivery.client.pagination.Page;
import com.liferay.headless.delivery.client.pagination.Pagination;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.Inject;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class AssetListEntryResourceTest
	extends BaseAssetListEntryResourceTestCase {

	@Test
	public void testGetSiteAssetListEntriesPageWithItemSubtypeFilter()
		throws Exception {

		Long siteId = testGroup.getGroupId();

		String itemSubtype1 = RandomTestUtil.randomString();
		String itemSubtype2 = RandomTestUtil.randomString();
		String itemType = RandomTestUtil.randomString();

		AssetListEntry assetListEntry1 = _addAssetListEntryWithTypeAndSubtype(
			itemSubtype1, itemType, siteId, RandomTestUtil.randomString());
		AssetListEntry assetListEntry2 = _addAssetListEntryWithTypeAndSubtype(
			itemSubtype2, itemType, siteId, RandomTestUtil.randomString());

		String filter = StringBundler.concat(
			"itemSubtype eq '", itemSubtype1, "'");

		Page<AssetListEntry> page =
			assetListEntryResource.getSiteAssetListEntriesPage(
				siteId, null, filter, Pagination.of(1, 50), null);

		List<AssetListEntry> assetListEntries =
			(List<AssetListEntry>)page.getItems();

		Assert.assertTrue(
			_containsAssetListEntryId(
				assetListEntries, assetListEntry1.getAssetListEntryId()));
		Assert.assertFalse(
			_containsAssetListEntryId(
				assetListEntries, assetListEntry2.getAssetListEntryId()));
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithItemTypeFilter()
		throws Exception {

		Long siteId = testGroup.getGroupId();

		String itemType1 = RandomTestUtil.randomString();
		String itemType2 = RandomTestUtil.randomString();

		AssetListEntry assetListEntry1 = _addAssetListEntryWithTypeAndSubtype(
			null, itemType1, siteId, RandomTestUtil.randomString());
		AssetListEntry assetListEntry2 = _addAssetListEntryWithTypeAndSubtype(
			null, itemType2, siteId, RandomTestUtil.randomString());

		String filter = StringBundler.concat("itemType eq '", itemType1, "'");

		Page<AssetListEntry> page =
			assetListEntryResource.getSiteAssetListEntriesPage(
				siteId, null, filter, Pagination.of(1, 50), null);

		List<AssetListEntry> assetListEntries =
			(List<AssetListEntry>)page.getItems();

		Assert.assertTrue(
			_containsAssetListEntryId(
				assetListEntries, assetListEntry1.getAssetListEntryId()));
		Assert.assertFalse(
			_containsAssetListEntryId(
				assetListEntries, assetListEntry2.getAssetListEntryId()));
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithItemTypeInFilter()
		throws Exception {

		Long siteId = testGroup.getGroupId();

		String itemType1 = RandomTestUtil.randomString();
		String itemType2 = RandomTestUtil.randomString();
		String itemType3 = RandomTestUtil.randomString();

		AssetListEntry assetListEntry1 = _addAssetListEntryWithTypeAndSubtype(
			null, itemType1, siteId, RandomTestUtil.randomString());
		AssetListEntry assetListEntry2 = _addAssetListEntryWithTypeAndSubtype(
			null, itemType2, siteId, RandomTestUtil.randomString());
		AssetListEntry assetListEntry3 = _addAssetListEntryWithTypeAndSubtype(
			null, itemType3, siteId, RandomTestUtil.randomString());

		String filter = StringBundler.concat(
			"itemType in ('", itemType1, "', '", itemType2, "')");

		Page<AssetListEntry> page =
			assetListEntryResource.getSiteAssetListEntriesPage(
				siteId, null, filter, Pagination.of(1, 50), null);

		List<AssetListEntry> assetListEntries =
			(List<AssetListEntry>)page.getItems();

		Assert.assertTrue(
			_containsAssetListEntryId(
				assetListEntries, assetListEntry1.getAssetListEntryId()));
		Assert.assertTrue(
			_containsAssetListEntryId(
				assetListEntries, assetListEntry2.getAssetListEntryId()));
		Assert.assertFalse(
			_containsAssetListEntryId(
				assetListEntries, assetListEntry3.getAssetListEntryId()));
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithSearch() throws Exception {
		Long siteId = testGroup.getGroupId();

		String keyword = RandomTestUtil.randomString();

		AssetListEntry assetListEntry1 = _addAssetListEntry(
			siteId, "Title " + keyword);

		AssetListEntry assetListEntry2 = _addAssetListEntry(siteId, "Title");

		Page<AssetListEntry> page =
			assetListEntryResource.getSiteAssetListEntriesPage(
				siteId, keyword, null, Pagination.of(1, 50), null);

		List<AssetListEntry> assetListEntries =
			(List<AssetListEntry>)page.getItems();

		Assert.assertTrue(
			_containsAssetListEntryId(
				assetListEntries, assetListEntry1.getAssetListEntryId()));
		Assert.assertFalse(
			_containsAssetListEntryId(
				assetListEntries, assetListEntry2.getAssetListEntryId()));
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"classNameId", "classPK", "externalReferenceCode", "itemSubtype",
			"itemType", "title"
		};
	}

	@Override
	protected AssetListEntry
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				Long assetLibraryId, AssetListEntry assetListEntry)
		throws Exception {

		return _addAssetListEntryFromDTO(assetListEntry, assetLibraryId);
	}

	@Override
	protected Long testGetAssetLibraryAssetListEntriesPage_getAssetLibraryId()
		throws Exception {

		return testDepotEntryGroup.getGroupId();
	}

	@Override
	protected Long
			testGetAssetLibraryAssetListEntriesPage_getIrrelevantAssetLibraryId()
		throws Exception {

		return irrelevantDepotEntryGroup.getGroupId();
	}

	@Override
	protected AssetListEntry testGetSiteAssetListEntriesPage_addAssetListEntry(
			Long siteId, AssetListEntry assetListEntry)
		throws Exception {

		return _addAssetListEntryFromDTO(assetListEntry, siteId);
	}

	private AssetListEntry _addAssetListEntry(Long groupId, String title)
		throws Exception {

		com.liferay.asset.list.model.AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				groupId, title, AssetListEntryTypeConstants.TYPE_MANUAL,
				ServiceContextTestUtil.getServiceContext(
					groupId, TestPropsValues.getUserId()));

		return _toClientDTO(assetListEntry);
	}

	private AssetListEntry _addAssetListEntryFromDTO(
			AssetListEntry assetListEntry, Long groupId)
		throws Exception {

		com.liferay.asset.list.model.AssetListEntry persistedAssetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				assetListEntry.getExternalReferenceCode(),
				TestPropsValues.getUserId(), groupId, assetListEntry.getTitle(),
				AssetListEntryTypeConstants.TYPE_MANUAL,
				ServiceContextTestUtil.getServiceContext(
					groupId, TestPropsValues.getUserId()));

		if ((assetListEntry.getItemType() != null) ||
			(assetListEntry.getItemSubtype() != null)) {

			persistedAssetListEntry.setAssetEntrySubtype(
				assetListEntry.getItemSubtype());
			persistedAssetListEntry.setAssetEntryType(
				assetListEntry.getItemType());

			persistedAssetListEntry =
				_assetListEntryLocalService.updateAssetListEntry(
					persistedAssetListEntry);
		}

		return _toClientDTO(persistedAssetListEntry);
	}

	private AssetListEntry _addAssetListEntryWithTypeAndSubtype(
			String assetEntrySubtype, String assetEntryType, Long groupId,
			String title)
		throws Exception {

		com.liferay.asset.list.model.AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				groupId, title, AssetListEntryTypeConstants.TYPE_MANUAL,
				ServiceContextTestUtil.getServiceContext(
					groupId, TestPropsValues.getUserId()));

		assetListEntry.setAssetEntrySubtype(assetEntrySubtype);
		assetListEntry.setAssetEntryType(assetEntryType);

		assetListEntry = _assetListEntryLocalService.updateAssetListEntry(
			assetListEntry);

		return _toClientDTO(assetListEntry);
	}

	private boolean _containsAssetListEntryId(
		List<AssetListEntry> assetListEntries, long assetListEntryId) {

		for (AssetListEntry assetListEntry : assetListEntries) {
			Long actualAssetListEntryId = assetListEntry.getAssetListEntryId();

			if ((actualAssetListEntryId != null) &&
				(actualAssetListEntryId == assetListEntryId)) {

				return true;
			}
		}

		return false;
	}

	private AssetListEntry _toClientDTO(
		com.liferay.asset.list.model.AssetListEntry assetListEntry) {

		AssetListEntry dto = new AssetListEntry();

		dto.setAssetListEntryId(assetListEntry.getAssetListEntryId());
		dto.setClassNameId(
			PortalUtil.getClassNameId(
				com.liferay.asset.list.model.AssetListEntry.class));
		dto.setClassPK(assetListEntry.getAssetListEntryId());
		dto.setDateCreated(assetListEntry.getCreateDate());
		dto.setDateModified(assetListEntry.getModifiedDate());
		dto.setExternalReferenceCode(assetListEntry.getExternalReferenceCode());
		dto.setItemSubtype(assetListEntry.getAssetEntrySubtype());
		dto.setItemType(assetListEntry.getAssetEntryType());
		dto.setTitle(assetListEntry.getTitle());

		return dto;
	}

	@Inject
	private AssetListEntryLocalService _assetListEntryLocalService;

}