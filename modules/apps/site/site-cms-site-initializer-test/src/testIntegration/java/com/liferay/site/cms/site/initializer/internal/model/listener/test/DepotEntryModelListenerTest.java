/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.model.AssetVocabularyGroupRel;
import com.liferay.asset.kernel.service.AssetVocabularyGroupRelLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.cms.site.initializer.test.util.CMSDefaultValueTestUtil;
import com.liferay.site.cms.site.initializer.util.CMSDefaultValueUtil;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class DepotEntryModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@FeatureFlag("LPD-112761")
	@Test
	public void testOnBeforeRemoveDeleteCMSDefaultValueObjectEntries()
		throws Exception {

		CMSDefaultValueTestUtil.processBatchEngineUnits(
			DepotEntryModelListenerTest.class);

		DepotEntry depotEntry = _depotEntryLocalService.addDepotEntry(
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			null, DepotConstants.TYPE_SPACE,
			ServiceContextTestUtil.getServiceContext());

		CMSDefaultValueUtil.addOrUpdateObjectEntry(
			JSONFactoryUtil.createJSONArray(), depotEntry, _filterFactory,
			TestPropsValues.getUserId());

		ObjectEntryFolder rootObjectEntryFolder =
			_objectEntryFolderLocalService.
				getObjectEntryFolderByExternalReferenceCode(
					ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS,
					depotEntry.getGroupId(), depotEntry.getCompanyId());

		CMSDefaultValueUtil.addOrUpdateObjectEntry(
			JSONFactoryUtil.createJSONArray(), _filterFactory,
			_objectEntryFolderLocalService.addObjectEntryFolder(
				RandomTestUtil.randomString(), depotEntry.getGroupId(),
				TestPropsValues.getUserId(),
				rootObjectEntryFolder.getObjectEntryFolderId(),
				StringPool.BLANK,
				Collections.singletonMap(
					LocaleUtil.getDefault(), RandomTestUtil.randomString()),
				RandomTestUtil.randomString(),
				ServiceContextTestUtil.getServiceContext()),
			TestPropsValues.getUserId());

		ObjectDefinition objectDefinition =
			CMSDefaultValueUtil.fetchObjectDefinition(
				depotEntry.getCompanyId());

		List<ObjectEntry> objectEntries =
			_objectEntryLocalService.getObjectEntries(
				depotEntry.getGroupId(),
				objectDefinition.getObjectDefinitionId(), QueryUtil.ALL_POS,
				QueryUtil.ALL_POS);

		Assert.assertEquals(objectEntries.toString(), 2, objectEntries.size());

		_depotEntryLocalService.deleteDepotEntry(depotEntry);

		objectEntries = _objectEntryLocalService.getObjectEntries(
			depotEntry.getGroupId(), objectDefinition.getObjectDefinitionId(),
			QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertEquals(objectEntries.toString(), 0, objectEntries.size());
	}

	@Test
	@TestInfo("LPD-83676")
	public void testOnBeforeRemoveUpdateAssetVocabularyGroupRel()
		throws Exception {

		_testOnBeforeRemoveUpdateAssetVocabularyGroupRel(
			DepotConstants.TYPE_PROJECT);
		_testOnBeforeRemoveUpdateAssetVocabularyGroupRel(
			DepotConstants.TYPE_SPACE);
	}

	private void _testOnBeforeRemoveUpdateAssetVocabularyGroupRel(
			int depotEntryType)
		throws Exception {

		Group cmsGroup = _groupLocalService.getGroup(
			TestPropsValues.getCompanyId(), GroupConstants.CMS);

		AssetVocabulary assetVocabulary =
			_assetVocabularyLocalService.addVocabulary(
				TestPropsValues.getUserId(), cmsGroup.getGroupId(),
				RandomTestUtil.randomString(),
				ServiceContextTestUtil.getServiceContext(
					cmsGroup.getGroupId(), TestPropsValues.getUserId()));

		List<AssetVocabularyGroupRel> assetVocabularyGroupRels =
			_assetVocabularyGroupRelLocalService.
				getAssetVocabularyGroupRelsByVocabularyIdAndDepotEntryType(
					assetVocabulary.getVocabularyId(), depotEntryType);

		Assert.assertEquals(
			assetVocabularyGroupRels.toString(), 1,
			assetVocabularyGroupRels.size());

		AssetVocabularyGroupRel assetVocabularyGroupRel =
			assetVocabularyGroupRels.get(0);

		Assert.assertEquals(
			GroupConstants.ANY_PARENT_GROUP_ID,
			assetVocabularyGroupRel.getGroupId());

		DepotEntry depotEntry1 = _depotEntryLocalService.addDepotEntry(
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			null, depotEntryType,
			ServiceContextTestUtil.getServiceContext(
				cmsGroup.getGroupId(), TestPropsValues.getUserId()));

		DepotEntry depotEntry2 = _depotEntryLocalService.addDepotEntry(
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			null, depotEntryType,
			ServiceContextTestUtil.getServiceContext(
				cmsGroup.getGroupId(), TestPropsValues.getUserId()));

		long[] groupIds = {depotEntry1.getGroupId(), depotEntry2.getGroupId()};

		_assetVocabularyGroupRelLocalService.setAssetVocabularyGroupRels(
			assetVocabulary.getVocabularyId(), groupIds, depotEntryType);

		assetVocabularyGroupRels =
			_assetVocabularyGroupRelLocalService.
				getAssetVocabularyGroupRelsByVocabularyIdAndDepotEntryType(
					assetVocabulary.getVocabularyId(), depotEntryType);

		Assert.assertEquals(
			assetVocabularyGroupRels.toString(), 2,
			assetVocabularyGroupRels.size());

		assetVocabularyGroupRel = assetVocabularyGroupRels.get(0);

		Assert.assertEquals(
			depotEntry1.getGroupId(), assetVocabularyGroupRel.getGroupId());

		assetVocabularyGroupRel = assetVocabularyGroupRels.get(1);

		Assert.assertEquals(
			depotEntry2.getGroupId(), assetVocabularyGroupRel.getGroupId());

		_depotEntryLocalService.deleteDepotEntry(depotEntry1);

		assetVocabularyGroupRels =
			_assetVocabularyGroupRelLocalService.
				getAssetVocabularyGroupRelsByVocabularyIdAndDepotEntryType(
					assetVocabulary.getVocabularyId(), depotEntryType);

		Assert.assertEquals(
			assetVocabularyGroupRels.toString(), 1,
			assetVocabularyGroupRels.size());

		assetVocabularyGroupRel = assetVocabularyGroupRels.get(0);

		Assert.assertEquals(
			depotEntry2.getGroupId(), assetVocabularyGroupRel.getGroupId());

		_depotEntryLocalService.deleteDepotEntry(depotEntry2);

		assetVocabularyGroupRels =
			_assetVocabularyGroupRelLocalService.
				getAssetVocabularyGroupRelsByVocabularyIdAndDepotEntryType(
					assetVocabulary.getVocabularyId(), depotEntryType);

		Assert.assertEquals(
			assetVocabularyGroupRels.toString(), 1,
			assetVocabularyGroupRels.size());

		assetVocabularyGroupRel = assetVocabularyGroupRels.get(0);

		Assert.assertEquals(
			GroupConstants.ANY_PARENT_GROUP_ID,
			assetVocabularyGroupRel.getGroupId());

		assetVocabularyGroupRels =
			_assetVocabularyGroupRelLocalService.
				getAssetVocabularyGroupRelsByGroupId(depotEntry1.getGroupId());

		Assert.assertEquals(
			assetVocabularyGroupRels.toString(), 0,
			assetVocabularyGroupRels.size());

		assetVocabularyGroupRels =
			_assetVocabularyGroupRelLocalService.
				getAssetVocabularyGroupRelsByGroupId(depotEntry2.getGroupId());

		Assert.assertEquals(
			assetVocabularyGroupRels.toString(), 0,
			assetVocabularyGroupRels.size());
	}

	@Inject
	private AssetVocabularyGroupRelLocalService
		_assetVocabularyGroupRelLocalService;

	@Inject
	private AssetVocabularyLocalService _assetVocabularyLocalService;

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject(
		filter = "filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT
	)
	private FilterFactory<Predicate> _filterFactory;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}