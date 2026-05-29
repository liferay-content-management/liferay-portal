/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.categories.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetCategoryConstants;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.test.util.AssetTestUtil;
import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Roberto Díaz
 */
@RunWith(Arquillian.class)
public class AssetCategoryFriendlyURLModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@FeatureFlag("LPD-70396")
	@Test
	@TestInfo("LPD-90910")
	public void testOnAfterCreate() throws Exception {
		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			_group.getGroupId(),
			StringUtil.toLowerCase(StringUtil.randomString()));

		AssetCategory assetCategory = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				_classNameLocalService.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId());

		Assert.assertNotNull(friendlyURLEntry);
		Assert.assertEquals(
			assetCategory.getCategoryId(), friendlyURLEntry.getClassPK());
		Assert.assertEquals(
			assetVocabulary.getVocabularyId(),
			friendlyURLEntry.getParentClassPK());
	}

	@FeatureFlag("LPD-70396")
	@Test
	@TestInfo("LPD-90910")
	public void testOnAfterUpdate() throws Exception {
		_testOnAfterUpdateWhenMovedToDifferentParentCategory();
		_testOnAfterUpdateWhenMovedToRoot();
		_testOnAfterUpdateWhenSameParentCategory();
	}

	@FeatureFlag("LPD-70396")
	@Test
	@TestInfo("LPD-90910")
	public void testOnAfterUpdateMovingCategoryFreesURLTitleUnderOldParent()
		throws Exception {

		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			_group.getGroupId(),
			StringUtil.toLowerCase(StringUtil.randomString()));

		AssetCategory parentAssetCategory1 = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		AssetCategory parentAssetCategory2 = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		String name = StringUtil.randomString();

		AssetCategory assetCategory = _addAssetCategory(
			name, assetVocabulary.getVocabularyId(),
			parentAssetCategory1.getCategoryId());

		String urlTitle = _getURLTitle(assetCategory);

		assetCategory.setParentCategoryId(parentAssetCategory2.getCategoryId());

		_assetCategoryLocalService.updateAssetCategory(assetCategory);

		AssetCategory siblingAssetCategory = _addAssetCategory(
			name, assetVocabulary.getVocabularyId(),
			parentAssetCategory1.getCategoryId());

		Assert.assertEquals(urlTitle, _getURLTitle(siblingAssetCategory));
	}

	@FeatureFlag("LPD-70396")
	@Test
	@TestInfo("LPD-90910")
	public void testOnBeforeRemove() throws Exception {
		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			_group.getGroupId(),
			StringUtil.toLowerCase(StringUtil.randomString()));

		AssetCategory assetCategory = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		long classNameId = _classNameLocalService.getClassNameId(
			AssetCategory.class);

		Assert.assertNotNull(
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				classNameId, assetCategory.getCategoryId()));

		_assetCategoryLocalService.deleteCategory(assetCategory);

		Assert.assertNull(
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				classNameId, assetCategory.getCategoryId()));
	}

	private AssetCategory _addAssetCategory(
			long assetVocabularyId, long parentAssetCategoryId)
		throws Exception {

		return _assetCategoryLocalService.addCategory(
			null, TestPropsValues.getUserId(), _group.getGroupId(),
			parentAssetCategoryId,
			HashMapBuilder.put(
				LocaleUtil.getDefault(), StringUtil.randomString()
			).build(),
			new HashMap<>(), assetVocabularyId, null,
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));
	}

	private AssetCategory _addAssetCategory(
			String name, long assetVocabularyId, long parentAssetCategoryId)
		throws Exception {

		return _assetCategoryLocalService.addCategory(
			null, TestPropsValues.getUserId(), _group.getGroupId(),
			parentAssetCategoryId,
			HashMapBuilder.put(
				LocaleUtil.getDefault(), name
			).build(),
			new HashMap<>(), assetVocabularyId, null,
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));
	}

	private String _getURLTitle(AssetCategory assetCategory) {
		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				_classNameLocalService.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId());

		return friendlyURLEntry.getUrlTitle(
			LocaleUtil.toLanguageId(LocaleUtil.getDefault()));
	}

	private void _testOnAfterUpdateWhenMovedToDifferentParentCategory()
		throws Exception {

		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			_group.getGroupId(),
			StringUtil.toLowerCase(StringUtil.randomString()));

		AssetCategory parentAssetCategory1 = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		AssetCategory assetCategory = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			parentAssetCategory1.getCategoryId());

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				_classNameLocalService.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId());

		Assert.assertEquals(
			parentAssetCategory1.getCategoryId(),
			friendlyURLEntry.getParentClassPK());

		AssetCategory parentAssetCategory2 = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		assetCategory.setParentCategoryId(parentAssetCategory2.getCategoryId());

		assetCategory = _assetCategoryLocalService.updateAssetCategory(
			assetCategory);

		FriendlyURLEntry updatedFriendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				_classNameLocalService.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId());

		Assert.assertEquals(
			parentAssetCategory2.getCategoryId(),
			updatedFriendlyURLEntry.getParentClassPK());
	}

	private void _testOnAfterUpdateWhenMovedToRoot() throws Exception {
		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			_group.getGroupId(),
			StringUtil.toLowerCase(StringUtil.randomString()));

		AssetCategory parentAssetCategory = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		AssetCategory assetCategory = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			parentAssetCategory.getCategoryId());

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				_classNameLocalService.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId());

		Assert.assertEquals(
			parentAssetCategory.getCategoryId(),
			friendlyURLEntry.getParentClassPK());

		assetCategory.setParentCategoryId(
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		assetCategory = _assetCategoryLocalService.updateAssetCategory(
			assetCategory);

		FriendlyURLEntry updatedFriendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				_classNameLocalService.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId());

		Assert.assertEquals(
			assetVocabulary.getVocabularyId(),
			updatedFriendlyURLEntry.getParentClassPK());
	}

	private void _testOnAfterUpdateWhenSameParentCategory() throws Exception {
		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			_group.getGroupId(),
			StringUtil.toLowerCase(StringUtil.randomString()));

		AssetCategory assetCategory = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				_classNameLocalService.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId());

		Assert.assertNotNull(friendlyURLEntry);
		Assert.assertEquals(
			assetVocabulary.getVocabularyId(),
			friendlyURLEntry.getParentClassPK());

		assetCategory.setTitle(StringUtil.randomString());

		assetCategory = _assetCategoryLocalService.updateAssetCategory(
			assetCategory);

		FriendlyURLEntry updatedFriendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				_classNameLocalService.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId());

		Assert.assertEquals(
			assetVocabulary.getVocabularyId(),
			updatedFriendlyURLEntry.getParentClassPK());
	}

	@Inject
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Inject
	private ClassNameLocalService _classNameLocalService;

	@Inject
	private FriendlyURLEntryLocalService _friendlyURLEntryLocalService;

	@DeleteAfterTestRun
	private Group _group;

}