/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.cms.site.initializer.test.util.CMSDefaultValueTestUtil;
import com.liferay.site.cms.site.initializer.util.CMSDefaultValueUtil;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stefano Motta
 */
@FeatureFlag("LPD-112761")
@RunWith(Arquillian.class)
public class CMSDefaultValueObjectEntryFolderLocalServiceWrapperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		CMSDefaultValueTestUtil.processBatchEngineUnits(
			CMSDefaultValueObjectEntryFolderLocalServiceWrapperTest.class);

		DepotEntry depotEntry = _depotEntryLocalService.addDepotEntry(
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			DepotConstants.TYPE_SPACE,
			ServiceContextTestUtil.getServiceContext());

		_group = depotEntry.getGroup();

		_rootObjectEntryFolder =
			_objectEntryFolderLocalService.
				getObjectEntryFolderByExternalReferenceCode(
					ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS,
					_group.getGroupId(), _group.getCompanyId());
	}

	@Test
	public void testCopyObjectEntryFolder() throws Exception {
		ObjectEntryFolder objectEntryFolder = _addObjectEntryFolder(
			_rootObjectEntryFolder.getObjectEntryFolderId());

		CMSDefaultValueUtil.addOrUpdateObjectEntry(
			_createJSONArray(), _filterFactory, objectEntryFolder,
			TestPropsValues.getUserId());

		ObjectEntryFolder childObjectEntryFolder = _addObjectEntryFolder(
			objectEntryFolder.getObjectEntryFolderId());

		CMSDefaultValueUtil.addOrUpdateObjectEntry(
			_createJSONArray(), _filterFactory, childObjectEntryFolder,
			TestPropsValues.getUserId());

		ObjectEntryFolder targetObjectEntryFolder = _addObjectEntryFolder(
			_rootObjectEntryFolder.getObjectEntryFolderId());

		ObjectEntryFolder copyObjectEntryFolder =
			_objectEntryFolderLocalService.copyObjectEntryFolder(
				TestPropsValues.getUserId(),
				objectEntryFolder.getObjectEntryFolderId(),
				targetObjectEntryFolder.getObjectEntryFolderId(), false,
				ServiceContextTestUtil.getServiceContext());

		_assertCMSDefaultValueObjectEntry(copyObjectEntryFolder);

		List<ObjectEntryFolder> childObjectEntryFolders =
			_objectEntryFolderLocalService.getObjectEntryFolders(
				_group.getGroupId(), _group.getCompanyId(),
				copyObjectEntryFolder.getObjectEntryFolderId(),
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		_assertCMSDefaultValueObjectEntry(childObjectEntryFolders.get(0));

		Assert.assertEquals(
			childObjectEntryFolders.toString(), 1,
			childObjectEntryFolders.size());

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-112761")) {

			copyObjectEntryFolder =
				_objectEntryFolderLocalService.copyObjectEntryFolder(
					TestPropsValues.getUserId(),
					objectEntryFolder.getObjectEntryFolderId(),
					_rootObjectEntryFolder.getObjectEntryFolderId(), false,
					ServiceContextTestUtil.getServiceContext());
		}

		Assert.assertNull(
			CMSDefaultValueUtil.fetchObjectEntry(
				copyObjectEntryFolder.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(),
				copyObjectEntryFolder.getCompanyId(), _filterFactory,
				copyObjectEntryFolder.getGroupId()));
	}

	private ObjectEntryFolder _addObjectEntryFolder(
			long parentObjectEntryFolderId)
		throws Exception {

		return _objectEntryFolderLocalService.addObjectEntryFolder(
			RandomTestUtil.randomString(), _group.getGroupId(),
			_group.getCreatorUserId(), parentObjectEntryFolderId,
			StringPool.BLANK,
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext());
	}

	private void _assertCMSDefaultValueObjectEntry(
			ObjectEntryFolder objectEntryFolder)
		throws Exception {

		Assert.assertNotNull(
			CMSDefaultValueUtil.fetchObjectEntry(
				objectEntryFolder.getExternalReferenceCode(),
				ObjectEntryFolder.class.getName(),
				objectEntryFolder.getCompanyId(), _filterFactory,
				objectEntryFolder.getGroupId()));
	}

	private JSONArray _createJSONArray() {
		return JSONUtil.put(
			JSONUtil.put(
				"name", "title"
			).put(
				"type", "Text"
			).put(
				"value", RandomTestUtil.randomString()
			));
	}

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject(
		filter = "filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT
	)
	private FilterFactory<Predicate> _filterFactory;

	private Group _group;

	@Inject
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	private ObjectEntryFolder _rootObjectEntryFolder;

}