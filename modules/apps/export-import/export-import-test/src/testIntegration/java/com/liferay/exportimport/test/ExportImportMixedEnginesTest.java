/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.categories.admin.web.constants.AssetCategoriesAdminPortletKeys;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.asset.tags.constants.AssetTagsAdminPortletKeys;
import com.liferay.asset.test.util.AssetTestUtil;
import com.liferay.exportimport.kernel.configuration.ExportImportConfigurationSettingsMapFactoryUtil;
import com.liferay.exportimport.kernel.configuration.constants.ExportImportConfigurationConstants;
import com.liferay.exportimport.kernel.lar.PortletDataHandlerKeys;
import com.liferay.exportimport.kernel.model.ExportImportConfiguration;
import com.liferay.exportimport.kernel.service.ExportImportConfigurationLocalService;
import com.liferay.exportimport.kernel.service.ExportImportLocalService;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
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
public class ExportImportMixedEnginesTest {

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
	public void testExportImportAssetCategoriesPreservesCreationAndModificationDatesAndUuid()
		throws Exception {

		DateFormat simpleDateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd");

		AssetVocabulary assetVocabulary1 = AssetTestUtil.addVocabulary(
			_group.getGroupId());

		assetVocabulary1.setCreateDate(simpleDateFormat.parse("2020-01-15"));
		assetVocabulary1.setModifiedDate(simpleDateFormat.parse("2020-01-16"));

		assetVocabulary1 = _assetVocabularyLocalService.updateAssetVocabulary(
			assetVocabulary1);

		AssetCategory assetCategory1 = AssetTestUtil.addCategory(
			_group.getGroupId(), assetVocabulary1.getVocabularyId());

		assetCategory1.setCreateDate(simpleDateFormat.parse("2021-01-15"));
		assetCategory1.setModifiedDate(simpleDateFormat.parse("2021-01-16"));

		assetCategory1 = _assetCategoryLocalService.updateAssetCategory(
			assetCategory1);

		Map<String, String[]> parameterMap = HashMapBuilder.put(
			PortletDataHandlerKeys.PORTLET_DATA,
			new String[] {Boolean.TRUE.toString()}
		).put(
			PortletDataHandlerKeys.PORTLET_DATA + "_" +
				AssetCategoriesAdminPortletKeys.ASSET_CATEGORIES_ADMIN,
			new String[] {Boolean.TRUE.toString()}
		).build();

		File larFile = _exportImportLocalService.exportLayoutsAsFile(
			_addExportLayoutConfiguration(_group.getGroupId(), parameterMap));

		_assetCategoryLocalService.deleteCategory(assetCategory1);

		_assetVocabularyLocalService.deleteVocabulary(assetVocabulary1);

		_exportImportLocalService.importLayouts(
			_addImportLayoutConfiguration(_group.getGroupId(), parameterMap),
			larFile);

		AssetVocabulary assetVocabulary2 =
			_assetVocabularyLocalService.
				fetchAssetVocabularyByExternalReferenceCode(
					assetVocabulary1.getExternalReferenceCode(),
					_group.getGroupId());

		Assert.assertEquals(
			assetVocabulary1.getCreateDate(), assetVocabulary2.getCreateDate());
		Assert.assertEquals(
			assetVocabulary1.getModifiedDate(),
			assetVocabulary2.getModifiedDate());
		Assert.assertEquals(
			assetVocabulary1.getUuid(), assetVocabulary2.getUuid());

		AssetCategory assetCategory2 =
			_assetCategoryLocalService.
				fetchAssetCategoryByExternalReferenceCode(
					assetCategory1.getExternalReferenceCode(),
					_group.getGroupId());

		Assert.assertEquals(
			assetCategory1.getCreateDate(), assetCategory2.getCreateDate());
		Assert.assertEquals(
			assetCategory1.getModifiedDate(), assetCategory2.getModifiedDate());
		Assert.assertEquals(assetCategory1.getUuid(), assetCategory2.getUuid());
	}

	@Test
	@TestInfo("LPD-67728")
	public void testExportImportAssetCategoriesReferencedByJournalArticle()
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(_group.getGroupId());

		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			_group.getGroupId());

		AssetCategory assetCategory1 = AssetTestUtil.addCategory(
			_group.getGroupId(), assetVocabulary.getVocabularyId());

		serviceContext.setAssetCategoryIds(
			new long[] {assetCategory1.getCategoryId()});

		JournalArticle journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), serviceContext);

		Map<String, String[]> parameterMap = HashMapBuilder.put(
			PortletDataHandlerKeys.PORTLET_DATA,
			new String[] {Boolean.TRUE.toString()}
		).put(
			PortletDataHandlerKeys.PORTLET_DATA + "_" +
				AssetCategoriesAdminPortletKeys.ASSET_CATEGORIES_ADMIN,
			new String[] {Boolean.TRUE.toString()}
		).put(
			PortletDataHandlerKeys.PORTLET_DATA + "_" +
				JournalPortletKeys.JOURNAL,
			new String[] {Boolean.TRUE.toString()}
		).build();

		File larFile = _exportImportLocalService.exportLayoutsAsFile(
			_addExportLayoutConfiguration(_group.getGroupId(), parameterMap));

		_journalArticleLocalService.deleteArticle(journalArticle);

		_assetCategoryLocalService.deleteCategory(assetCategory1);

		_assetVocabularyLocalService.deleteVocabulary(assetVocabulary);

		_exportImportLocalService.importLayouts(
			_addImportLayoutConfiguration(_group.getGroupId(), parameterMap),
			larFile);

		Assert.assertNotNull(
			_assetVocabularyLocalService.
				fetchAssetVocabularyByExternalReferenceCode(
					assetVocabulary.getExternalReferenceCode(),
					_group.getGroupId()));

		AssetCategory assetCategory2 =
			_assetCategoryLocalService.
				fetchAssetCategoryByExternalReferenceCode(
					assetCategory1.getExternalReferenceCode(),
					_group.getGroupId());

		Assert.assertEquals(assetCategory1.getName(), assetCategory2.getName());

		Assert.assertNotNull(
			_journalArticleLocalService.
				fetchLatestArticleByExternalReferenceCode(
					_group.getGroupId(),
					journalArticle.getExternalReferenceCode()));
	}

	@Test
	@TestInfo("LPD-86116")
	public void testExportImportAssetTagsPreservesCreationAndModificationDatesAndUuid()
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

	@Test
	public void testExportImportAssetTagsReferencedByJournalArticle()
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(_group.getGroupId());

		AssetTag assetTag1 = AssetTestUtil.addTag(_group.getGroupId());

		serviceContext.setAssetTagNames(new String[] {assetTag1.getName()});

		JournalArticle journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), serviceContext);

		Map<String, String[]> parameterMap = HashMapBuilder.put(
			PortletDataHandlerKeys.PORTLET_DATA,
			new String[] {Boolean.TRUE.toString()}
		).put(
			PortletDataHandlerKeys.PORTLET_DATA + "_" +
				AssetTagsAdminPortletKeys.ASSET_TAGS_ADMIN,
			new String[] {Boolean.TRUE.toString()}
		).put(
			PortletDataHandlerKeys.PORTLET_DATA + "_" +
				JournalPortletKeys.JOURNAL,
			new String[] {Boolean.TRUE.toString()}
		).build();

		File larFile = _exportImportLocalService.exportLayoutsAsFile(
			_addExportLayoutConfiguration(_group.getGroupId(), parameterMap));

		_journalArticleLocalService.deleteArticle(journalArticle);

		_assetTagLocalService.deleteAssetTag(assetTag1.getTagId());

		_exportImportLocalService.importLayouts(
			_addImportLayoutConfiguration(_group.getGroupId(), parameterMap),
			larFile);

		AssetTag assetTag2 =
			_assetTagLocalService.getAssetTagByExternalReferenceCode(
				assetTag1.getExternalReferenceCode(), _group.getGroupId());

		Assert.assertEquals(assetTag1.getName(), assetTag2.getName());

		Assert.assertNotNull(
			_journalArticleLocalService.
				fetchLatestArticleByExternalReferenceCode(
					_group.getGroupId(),
					journalArticle.getExternalReferenceCode()));
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
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Inject
	private AssetTagLocalService _assetTagLocalService;

	@Inject
	private AssetVocabularyLocalService _assetVocabularyLocalService;

	@Inject
	private ExportImportConfigurationLocalService
		_exportImportConfigurationLocalService;

	@Inject
	private ExportImportLocalService _exportImportLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private JournalArticleLocalService _journalArticleLocalService;

}