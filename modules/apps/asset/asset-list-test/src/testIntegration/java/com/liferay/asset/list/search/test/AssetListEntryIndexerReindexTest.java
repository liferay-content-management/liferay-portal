/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.test.util.AssetListTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.IndexWriterHelper;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.search.model.uid.UIDFactory;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.test.rule.SearchTestRule;
import com.liferay.portal.search.test.util.FieldValuesAssert;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Collections;
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
public class AssetListEntryIndexerReindexTest {

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
	}

	@Test
	public void testReindex() throws Exception {
		Locale locale = LocaleUtil.US;

		String title = RandomTestUtil.randomString();

		_assetListEntry = AssetListTestUtil.addAssetListEntry(
			_group.getGroupId(), title);

		Map<String, String> map = Collections.singletonMap(Field.TITLE, title);

		_assertFieldValues(Field.TITLE, map, locale, title);

		_indexWriterHelper.deleteDocument(
			_assetListEntry.getCompanyId(), _uidFactory.getUID(_assetListEntry),
			true);

		_assertFieldValues(Field.TITLE, Collections.emptyMap(), locale, title);

		_indexer.reindexCompany(_group.getCompanyId());

		_assertFieldValues(Field.TITLE, map, locale, title);
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	private void _assertFieldValues(
		String fieldName, Map<String, String> map, Locale locale,
		String searchTerm) {

		FieldValuesAssert.assertFieldValues(
			map, fieldName::equals,
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

	@DeleteAfterTestRun
	private AssetListEntry _assetListEntry;

	@DeleteAfterTestRun
	private Group _group;

	@Inject(
		filter = "indexer.class.name=com.liferay.asset.list.model.AssetListEntry"
	)
	private Indexer<AssetListEntry> _indexer;

	@Inject
	private IndexWriterHelper _indexWriterHelper;

	@Inject
	private Searcher _searcher;

	@Inject
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

	@Inject
	private UIDFactory _uidFactory;

}