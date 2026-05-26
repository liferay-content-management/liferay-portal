/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryLocalService;
import com.liferay.asset.list.test.util.AssetListTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.SearchEngineHelper;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.model.uid.UIDFactory;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.test.rule.SearchTestRule;
import com.liferay.portal.search.test.util.FieldValuesAssert;
import com.liferay.portal.search.test.util.IndexedFieldsFixture;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class AssetListEntryIndexerIndexedFieldsTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_indexedFieldsFixture = new IndexedFieldsFixture(
			_resourcePermissionLocalService, _searchEngineHelper, _uidFactory);
	}

	@Test
	public void testIndexedFields() throws Exception {
		String title = RandomTestUtil.randomString();

		_assetListEntry = AssetListTestUtil.addAssetListEntry(
			_group.getGroupId(), title);

		_assetListEntry.setAssetEntrySubtype(RandomTestUtil.randomString());
		_assetListEntry.setAssetEntryType(RandomTestUtil.randomString());

		_assetListEntry = _assetListEntryLocalService.updateAssetListEntry(
			_assetListEntry);

		_assertFieldValues(
			_expectedFieldValues(_assetListEntry), LocaleUtil.US, title);
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	private void _assertFieldValues(
		Map<String, String> map, Locale locale, String searchTerm) {

		FieldValuesAssert.assertFieldValues(
			map,
			name ->
				!name.contains(StringPool.PERIOD) && !name.equals("score") &&
				!name.equals("timestamp"),
			_searcher.search(
				_searchRequestBuilderFactory.builder(
				).companyId(
					_group.getCompanyId()
				).fields(
					StringPool.STAR
				).groupIds(
					_group.getGroupId()
				).locale(
					locale
				).modelIndexerClasses(
					AssetListEntry.class
				).queryString(
					searchTerm
				).build()));
	}

	private Map<String, String> _expectedFieldValues(
			AssetListEntry assetListEntry)
		throws Exception {

		Map<String, String> map = HashMapBuilder.put(
			Field.COMPANY_ID, String.valueOf(assetListEntry.getCompanyId())
		).put(
			Field.ENTRY_CLASS_NAME, AssetListEntry.class.getName()
		).put(
			Field.ENTRY_CLASS_PK,
			String.valueOf(assetListEntry.getAssetListEntryId())
		).put(
			Field.GROUP_ID, String.valueOf(assetListEntry.getGroupId())
		).put(
			Field.SCOPE_GROUP_ID, String.valueOf(assetListEntry.getGroupId())
		).put(
			Field.STAGING_GROUP, String.valueOf(_group.isStagingGroup())
		).put(
			Field.TITLE, assetListEntry.getTitle()
		).put(
			Field.USER_ID, String.valueOf(assetListEntry.getUserId())
		).put(
			Field.USER_NAME, StringUtil.lowerCase(assetListEntry.getUserName())
		).put(
			"assetEntrySubtype", assetListEntry.getAssetEntrySubtype()
		).put(
			"assetEntryType", assetListEntry.getAssetEntryType()
		).put(
			"externalReferenceCode", assetListEntry.getExternalReferenceCode()
		).put(
			"groupExternalReferenceCode", _group.getExternalReferenceCode()
		).put(
			"scopeGroupExternalReferenceCode", _group.getExternalReferenceCode()
		).put(
			"title_sortable", StringUtil.lowerCase(assetListEntry.getTitle())
		).put(
			"userExternalReferenceCode",
			() -> {
				User user = TestPropsValues.getUser();

				return user.getExternalReferenceCode();
			}
		).build();

		_indexedFieldsFixture.populateUID(assetListEntry, map);

		_indexedFieldsFixture.populateDate(
			Field.CREATE_DATE, assetListEntry.getCreateDate(), map);
		_indexedFieldsFixture.populateDate(
			Field.MODIFIED_DATE, assetListEntry.getModifiedDate(), map);

		_indexedFieldsFixture.populateRoleIdFields(
			assetListEntry.getCompanyId(), AssetListEntry.class.getName(),
			assetListEntry.getAssetListEntryId(), assetListEntry.getGroupId(),
			null, map);

		return map;
	}

	@DeleteAfterTestRun
	private AssetListEntry _assetListEntry;

	@Inject
	private AssetListEntryLocalService _assetListEntryLocalService;

	@DeleteAfterTestRun
	private Group _group;

	private IndexedFieldsFixture _indexedFieldsFixture;

	@Inject(
		filter = "indexer.class.name=com.liferay.asset.list.model.AssetListEntry"
	)
	private Indexer<AssetListEntry> _indexer;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private SearchEngineHelper _searchEngineHelper;

	@Inject
	private Searcher _searcher;

	@Inject
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Inject
	private UIDFactory _uidFactory;

}