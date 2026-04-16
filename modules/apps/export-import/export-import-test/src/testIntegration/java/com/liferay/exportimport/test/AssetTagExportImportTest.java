/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.asset.tags.constants.AssetTagsAdminPortletKeys;
import com.liferay.exportimport.kernel.configuration.ExportImportConfigurationSettingsMapFactoryUtil;
import com.liferay.exportimport.kernel.configuration.constants.ExportImportConfigurationConstants;
import com.liferay.exportimport.kernel.lar.PortletDataHandlerKeys;
import com.liferay.exportimport.kernel.model.ExportImportConfiguration;
import com.liferay.exportimport.kernel.service.ExportImportConfigurationLocalService;
import com.liferay.exportimport.kernel.service.ExportImportLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.File;

import java.text.DateFormat;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@RunWith(Arquillian.class)
public class AssetTagExportImportTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule liferayIntegrationTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	@TestInfo("LPD-86116")
	public void testExportImportAssetTagsPreservesCreationAndModificationDatesAndUuidInBatch()
		throws Exception {

		DateFormat simpleDateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd");

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCreateDate(simpleDateFormat.parse("2020-01-15"));
		serviceContext.setModifiedDate(simpleDateFormat.parse("2020-01-16"));

		AssetTag assetTag1 = _assetTagLocalService.addTag(
			null, TestPropsValues.getUserId(), _group.getGroupId(),
			RandomTestUtil.randomString(), serviceContext);

		Map<String, String[]> parameterMap = HashMapBuilder.put(
			PortletDataHandlerKeys.PORTLET_DATA,
			new String[] {Boolean.TRUE.toString()}
		).put(
			PortletDataHandlerKeys.PORTLET_DATA + "_" +
				AssetTagsAdminPortletKeys.ASSET_TAGS_ADMIN,
			new String[] {Boolean.TRUE.toString()}
		).build();

		File larFile = _exportImportLocalService.exportLayoutsAsFile(
			_addExportLayoutConfiguration(_group.getGroupId(), parameterMap));

		_assetTagLocalService.deleteAssetTag(assetTag1.getTagId());

		_exportImportLocalService.importLayouts(
			_addImportLayoutConfiguration(_group.getGroupId(), parameterMap),
			larFile);

		AssetTag assetTag2 =
			_assetTagLocalService.getAssetTagByExternalReferenceCode(
				assetTag1.getExternalReferenceCode(), _group.getGroupId());

		Assert.assertEquals(
			assetTag1.getCreateDate(), assetTag2.getCreateDate());
		Assert.assertEquals(
			assetTag1.getModifiedDate(), assetTag2.getModifiedDate());
		Assert.assertEquals(assetTag1.getUuid(), assetTag2.getUuid());
	}

	private ExportImportConfiguration _addExportLayoutConfiguration(
			long groupId, Map<String, String[]> parameterMap)
		throws Exception {

		return _exportImportConfigurationLocalService.
			addDraftExportImportConfiguration(
				TestPropsValues.getUserId(),
				ExportImportConfigurationConstants.TYPE_EXPORT_LAYOUT,
				ExportImportConfigurationSettingsMapFactoryUtil.
					buildExportLayoutSettingsMap(
						TestPropsValues.getUser(), groupId, false, new long[0],
						parameterMap));
	}

	private ExportImportConfiguration _addImportLayoutConfiguration(
			long groupId, Map<String, String[]> parameterMap)
		throws Exception {

		return _exportImportConfigurationLocalService.
			addDraftExportImportConfiguration(
				TestPropsValues.getUserId(),
				ExportImportConfigurationConstants.TYPE_IMPORT_LAYOUT,
				ExportImportConfigurationSettingsMapFactoryUtil.
					buildImportLayoutSettingsMap(
						TestPropsValues.getUser(), groupId, false, new long[0],
						parameterMap));
	}

	@Inject
	private AssetTagLocalService _assetTagLocalService;

	@Inject
	private ExportImportConfigurationLocalService
		_exportImportConfigurationLocalService;

	@Inject
	private ExportImportLocalService _exportImportLocalService;

	@DeleteAfterTestRun
	private Group _group;

}