/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.categories.internal.feature.flag.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetCategoryConstants;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.test.util.AssetTestUtil;
import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.model.FriendlyURLEntryLocalization;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.portal.kernel.feature.flag.FeatureFlagListener;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
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
public class AssetCategoryFriendlyURLEntryFeatureFlagListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	@TestInfo("LPD-90910")
	public void testDisablingFeatureFlagResetsParentClassPK() throws Exception {
		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			_group.getGroupId(),
			StringUtil.toLowerCase(StringUtil.randomString()));

		AssetCategory assetCategory = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		_setParentClassPK(assetCategory, assetVocabulary.getVocabularyId());

		_featureFlagListener.onValue(
			TestPropsValues.getCompanyId(), "LPD-70396", false);

		_assertParentClassPK(
			assetCategory, AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);
	}

	@FeatureFlag("LPD-70396")
	@Test
	@TestInfo("LPD-90910")
	public void testEnablingFeatureFlagAddsParentCategoryToParentClassPK()
		throws Exception {

		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			_group.getGroupId(),
			StringUtil.toLowerCase(StringUtil.randomString()));

		AssetCategory parentAssetCategory = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		AssetCategory assetCategory = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			parentAssetCategory.getCategoryId());

		_setParentClassPK(
			assetCategory, AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		_featureFlagListener.onValue(
			TestPropsValues.getCompanyId(), "LPD-70396", true);

		_assertParentClassPK(
			assetCategory, parentAssetCategory.getCategoryId());
	}

	@FeatureFlag("LPD-70396")
	@Test
	@TestInfo("LPD-90910")
	public void testEnablingFeatureFlagAddsVocabularyIdToParentClassPK()
		throws Exception {

		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			_group.getGroupId(),
			StringUtil.toLowerCase(StringUtil.randomString()));

		AssetCategory assetCategory = _addAssetCategory(
			assetVocabulary.getVocabularyId(),
			AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		_setParentClassPK(
			assetCategory, AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);

		_featureFlagListener.onValue(
			TestPropsValues.getCompanyId(), "LPD-70396", true);

		_assertParentClassPK(assetCategory, assetVocabulary.getVocabularyId());
	}

	@FeatureFlag("LPD-70396")
	@Test
	@TestInfo("LPD-90910")
	public void testEnablingFeatureFlagDeletesOrphanedFriendlyURLEntry()
		throws Exception {

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.addFriendlyURLEntry(
				_group.getGroupId(),
				_classNameLocalService.getClassNameId(AssetCategory.class),
				AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID,
				RandomTestUtil.nextLong(),
				LocaleUtil.toLanguageId(LocaleUtil.getDefault()),
				HashMapBuilder.put(
					LocaleUtil.toLanguageId(LocaleUtil.getDefault()),
					StringUtil.randomString()
				).build(),
				ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		long friendlyURLEntryId = friendlyURLEntry.getFriendlyURLEntryId();

		Assert.assertFalse(
			_friendlyURLEntryLocalService.getFriendlyURLEntryLocalizations(
				friendlyURLEntryId
			).isEmpty());

		_triggerFeatureFlag(true);

		Assert.assertNull(
			_friendlyURLEntryLocalService.fetchFriendlyURLEntry(
				friendlyURLEntryId));

		Assert.assertTrue(
			_friendlyURLEntryLocalService.getFriendlyURLEntryLocalizations(
				friendlyURLEntryId
			).isEmpty());
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

	private void _assertParentClassPK(
		AssetCategory assetCategory, long parentClassPK) {

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				_classNameLocalService.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId());

		Assert.assertEquals(parentClassPK, friendlyURLEntry.getParentClassPK());

		for (FriendlyURLEntryLocalization friendlyURLEntryLocalization :
				_friendlyURLEntryLocalService.getFriendlyURLEntryLocalizations(
					friendlyURLEntry.getFriendlyURLEntryId())) {

			Assert.assertEquals(
				parentClassPK, friendlyURLEntryLocalization.getParentClassPK());
		}
	}

	private void _setParentClassPK(
		AssetCategory assetCategory, long parentClassPK) {

		FriendlyURLEntry friendlyURLEntry =
			_friendlyURLEntryLocalService.fetchMainFriendlyURLEntry(
				_classNameLocalService.getClassNameId(AssetCategory.class),
				assetCategory.getCategoryId());

		Assert.assertNotNull(friendlyURLEntry);

		friendlyURLEntry.setParentClassPK(parentClassPK);

		friendlyURLEntry = _friendlyURLEntryLocalService.updateFriendlyURLEntry(
			friendlyURLEntry);

		for (FriendlyURLEntryLocalization friendlyURLEntryLocalization :
				_friendlyURLEntryLocalService.getFriendlyURLEntryLocalizations(
					friendlyURLEntry.getFriendlyURLEntryId())) {

			friendlyURLEntryLocalization.setParentClassPK(parentClassPK);

			_friendlyURLEntryLocalService.updateFriendlyURLLocalization(
				friendlyURLEntryLocalization);
		}
	}

	private void _triggerFeatureFlag(boolean enabled) throws Exception {
		_featureFlagListener.onValue(
			TestPropsValues.getCompanyId(), "LPD-70396", enabled);
	}

	@Inject
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Inject
	private ClassNameLocalService _classNameLocalService;

	@Inject(
		filter = "component.name=com.liferay.asset.categories.internal.feature.flag.AssetCategoryFriendlyURLEntryFeatureFlagListener"
	)
	private FeatureFlagListener _featureFlagListener;

	@Inject
	private FriendlyURLEntryLocalService _friendlyURLEntryLocalService;

	@DeleteAfterTestRun
	private Group _group;

}