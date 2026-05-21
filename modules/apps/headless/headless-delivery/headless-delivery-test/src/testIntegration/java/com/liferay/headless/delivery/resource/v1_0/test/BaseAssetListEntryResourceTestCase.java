/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.resource.v1_0.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalServiceUtil;
import com.liferay.headless.delivery.client.dto.v1_0.AssetListEntry;
import com.liferay.headless.delivery.client.dto.v1_0.Field;
import com.liferay.headless.delivery.client.http.HttpInvoker;
import com.liferay.headless.delivery.client.pagination.Page;
import com.liferay.headless.delivery.client.pagination.Pagination;
import com.liferay.headless.delivery.client.resource.v1_0.AssetListEntryResource;
import com.liferay.headless.delivery.client.serdes.v1_0.AssetListEntrySerDes;
import com.liferay.petra.function.UnsafeTriConsumer;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.search.test.rule.SearchTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import jakarta.annotation.Generated;

import jakarta.ws.rs.core.MultivaluedHashMap;

import java.lang.reflect.Method;

import java.text.Format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public abstract class BaseAssetListEntryResourceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_format = FastDateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		irrelevantDepotEntry = DepotEntryLocalServiceUtil.addDepotEntry(
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			null, DepotConstants.TYPE_ASSET_LIBRARY,
			new ServiceContext() {
				{
					setCompanyId(testCompany.getCompanyId());
					setUserId(TestPropsValues.getUserId());
				}
			});
		irrelevantDepotEntryGroup = irrelevantDepotEntry.getGroup();
		testDepotEntry = DepotEntryLocalServiceUtil.addDepotEntry(
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			null, DepotConstants.TYPE_ASSET_LIBRARY,
			new ServiceContext() {
				{
					setCompanyId(testCompany.getCompanyId());
					setUserId(TestPropsValues.getUserId());
				}
			});
		testDepotEntryGroup = testDepotEntry.getGroup();

		_assetListEntryResource.setContextCompany(testCompany);

		_testCompanyAdminUser = UserTestUtil.getAdminUser(
			testCompany.getCompanyId());

		assetListEntryResource = AssetListEntryResource.builder(
		).authentication(
			_testCompanyAdminUser.getEmailAddress(),
			PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(),
			PortalUtil.getPortalServerPort(false), "http"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	@After
	public void tearDown() throws Exception {
		DepotEntryLocalServiceUtil.deleteDepotEntry(irrelevantDepotEntry);
		DepotEntryLocalServiceUtil.deleteDepotEntry(testDepotEntry);

		GroupTestUtil.deleteGroup(irrelevantGroup);
		GroupTestUtil.deleteGroup(testGroup);
	}

	@Test
	public void testClientSerDesToDTO() throws Exception {
		ObjectMapper objectMapper = getClientSerDesObjectMapper();

		AssetListEntry assetListEntry1 = randomAssetListEntry();

		String json = objectMapper.writeValueAsString(assetListEntry1);

		AssetListEntry assetListEntry2 = AssetListEntrySerDes.toDTO(json);

		Assert.assertTrue(equals(assetListEntry1, assetListEntry2));
	}

	@Test
	public void testClientSerDesToJSON() throws Exception {
		ObjectMapper objectMapper = getClientSerDesObjectMapper();

		AssetListEntry assetListEntry = randomAssetListEntry();

		String json1 = objectMapper.writeValueAsString(assetListEntry);
		String json2 = AssetListEntrySerDes.toJSON(assetListEntry);

		Assert.assertEquals(
			objectMapper.readTree(json1), objectMapper.readTree(json2));
	}

	protected ObjectMapper getClientSerDesObjectMapper() {
		return new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				enable(SerializationFeature.INDENT_OUTPUT);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};
	}

	@Test
	public void testEscapeRegexInStringFields() throws Exception {
		String regex = "^[0-9]+(\\.[0-9]{1,2})\"?";

		AssetListEntry assetListEntry = randomAssetListEntry();

		assetListEntry.setExternalReferenceCode(regex);
		assetListEntry.setItemSubtype(regex);
		assetListEntry.setItemType(regex);
		assetListEntry.setTitle(regex);

		String json = AssetListEntrySerDes.toJSON(assetListEntry);

		Assert.assertFalse(json.contains(regex));

		assetListEntry = AssetListEntrySerDes.toDTO(json);

		Assert.assertEquals(regex, assetListEntry.getExternalReferenceCode());
		Assert.assertEquals(regex, assetListEntry.getItemSubtype());
		Assert.assertEquals(regex, assetListEntry.getItemType());
		Assert.assertEquals(regex, assetListEntry.getTitle());
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPage() throws Exception {
		Long assetLibraryId =
			testGetAssetLibraryAssetListEntriesPage_getAssetLibraryId();
		Long irrelevantAssetLibraryId =
			testGetAssetLibraryAssetListEntriesPage_getIrrelevantAssetLibraryId();

		Page<AssetListEntry> page =
			assetListEntryResource.getAssetLibraryAssetListEntriesPage(
				assetLibraryId, null, null, Pagination.of(1, 10), null);

		long totalCount = page.getTotalCount();

		if (irrelevantAssetLibraryId != null) {
			AssetListEntry irrelevantAssetListEntry =
				testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
					irrelevantAssetLibraryId, randomIrrelevantAssetListEntry());

			page = assetListEntryResource.getAssetLibraryAssetListEntriesPage(
				irrelevantAssetLibraryId, null, null,
				Pagination.of(1, (int)totalCount + 1), null);

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantAssetListEntry,
				(List<AssetListEntry>)page.getItems());
			assertValid(
				page,
				testGetAssetLibraryAssetListEntriesPage_getExpectedActions(
					irrelevantAssetLibraryId));
		}

		AssetListEntry assetListEntry1 =
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				assetLibraryId, randomAssetListEntry());

		AssetListEntry assetListEntry2 =
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				assetLibraryId, randomAssetListEntry());

		page = assetListEntryResource.getAssetLibraryAssetListEntriesPage(
			assetLibraryId, null, null, Pagination.of(1, 10), null);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(assetListEntry1, (List<AssetListEntry>)page.getItems());
		assertContains(assetListEntry2, (List<AssetListEntry>)page.getItems());
		assertValid(
			page,
			testGetAssetLibraryAssetListEntriesPage_getExpectedActions(
				assetLibraryId));
	}

	protected Map<String, Map<String, String>>
			testGetAssetLibraryAssetListEntriesPage_getExpectedActions(
				Long assetLibraryId)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPageWithFilterDateTimeEquals()
		throws Exception {

		List<EntityField> entityFields = getEntityFields(
			EntityField.Type.DATE_TIME);

		if (entityFields.isEmpty()) {
			return;
		}

		Long assetLibraryId =
			testGetAssetLibraryAssetListEntriesPage_getAssetLibraryId();

		AssetListEntry assetListEntry1 = randomAssetListEntry();

		assetListEntry1 =
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				assetLibraryId, assetListEntry1);

		for (EntityField entityField : entityFields) {
			Page<AssetListEntry> page =
				assetListEntryResource.getAssetLibraryAssetListEntriesPage(
					assetLibraryId, null,
					getFilterString(entityField, "between", assetListEntry1),
					Pagination.of(1, 2), null);

			assertEquals(
				Collections.singletonList(assetListEntry1),
				(List<AssetListEntry>)page.getItems());
		}
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPageWithFilterDoubleEquals()
		throws Exception {

		testGetAssetLibraryAssetListEntriesPageWithFilter(
			"eq", EntityField.Type.DOUBLE);
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPageWithFilterStringContains()
		throws Exception {

		testGetAssetLibraryAssetListEntriesPageWithFilter(
			"contains", EntityField.Type.STRING);
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPageWithFilterStringEquals()
		throws Exception {

		testGetAssetLibraryAssetListEntriesPageWithFilter(
			"eq", EntityField.Type.STRING);
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPageWithFilterStringStartsWith()
		throws Exception {

		testGetAssetLibraryAssetListEntriesPageWithFilter(
			"startswith", EntityField.Type.STRING);
	}

	protected void testGetAssetLibraryAssetListEntriesPageWithFilter(
			String operator, EntityField.Type type)
		throws Exception {

		List<EntityField> entityFields = getEntityFields(type);

		if (entityFields.isEmpty()) {
			return;
		}

		Long assetLibraryId =
			testGetAssetLibraryAssetListEntriesPage_getAssetLibraryId();

		AssetListEntry assetListEntry1 =
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				assetLibraryId, randomAssetListEntry());

		@SuppressWarnings("PMD.UnusedLocalVariable")
		AssetListEntry assetListEntry2 =
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				assetLibraryId, randomAssetListEntry());

		for (EntityField entityField : entityFields) {
			Page<AssetListEntry> page =
				assetListEntryResource.getAssetLibraryAssetListEntriesPage(
					assetLibraryId, null,
					getFilterString(entityField, operator, assetListEntry1),
					Pagination.of(1, 2), null);

			assertEquals(
				Collections.singletonList(assetListEntry1),
				(List<AssetListEntry>)page.getItems());
		}
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPageWithPagination()
		throws Exception {

		Long assetLibraryId =
			testGetAssetLibraryAssetListEntriesPage_getAssetLibraryId();

		Page<AssetListEntry> assetListEntriesPage =
			assetListEntryResource.getAssetLibraryAssetListEntriesPage(
				assetLibraryId, null, null, null, null);

		int totalCount = GetterUtil.getInteger(
			assetListEntriesPage.getTotalCount());

		AssetListEntry assetListEntry1 =
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				assetLibraryId, randomAssetListEntry());

		AssetListEntry assetListEntry2 =
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				assetLibraryId, randomAssetListEntry());

		AssetListEntry assetListEntry3 =
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				assetLibraryId, randomAssetListEntry());

		// See com.liferay.portal.vulcan.internal.configuration.HeadlessAPICompanyConfiguration#pageSizeLimit

		int pageSizeLimit = 500;

		if (totalCount >= (pageSizeLimit - 2)) {
			Page<AssetListEntry> page1 =
				assetListEntryResource.getAssetLibraryAssetListEntriesPage(
					assetLibraryId, null, null,
					Pagination.of(
						(int)Math.ceil((totalCount + 1.0) / pageSizeLimit),
						pageSizeLimit),
					null);

			Assert.assertEquals(totalCount + 3, page1.getTotalCount());

			assertContains(
				assetListEntry1, (List<AssetListEntry>)page1.getItems());

			Page<AssetListEntry> page2 =
				assetListEntryResource.getAssetLibraryAssetListEntriesPage(
					assetLibraryId, null, null,
					Pagination.of(
						(int)Math.ceil((totalCount + 2.0) / pageSizeLimit),
						pageSizeLimit),
					null);

			assertContains(
				assetListEntry2, (List<AssetListEntry>)page2.getItems());

			Page<AssetListEntry> page3 =
				assetListEntryResource.getAssetLibraryAssetListEntriesPage(
					assetLibraryId, null, null,
					Pagination.of(
						(int)Math.ceil((totalCount + 3.0) / pageSizeLimit),
						pageSizeLimit),
					null);

			assertContains(
				assetListEntry3, (List<AssetListEntry>)page3.getItems());
		}
		else {
			Page<AssetListEntry> page1 =
				assetListEntryResource.getAssetLibraryAssetListEntriesPage(
					assetLibraryId, null, null,
					Pagination.of(1, totalCount + 2), null);

			List<AssetListEntry> assetListEntries1 =
				(List<AssetListEntry>)page1.getItems();

			Assert.assertEquals(
				assetListEntries1.toString(), totalCount + 2,
				assetListEntries1.size());

			Page<AssetListEntry> page2 =
				assetListEntryResource.getAssetLibraryAssetListEntriesPage(
					assetLibraryId, null, null,
					Pagination.of(2, totalCount + 2), null);

			Assert.assertEquals(totalCount + 3, page2.getTotalCount());

			List<AssetListEntry> assetListEntries2 =
				(List<AssetListEntry>)page2.getItems();

			Assert.assertEquals(
				assetListEntries2.toString(), 1, assetListEntries2.size());

			Page<AssetListEntry> page3 =
				assetListEntryResource.getAssetLibraryAssetListEntriesPage(
					assetLibraryId, null, null,
					Pagination.of(1, (int)totalCount + 3), null);

			assertContains(
				assetListEntry1, (List<AssetListEntry>)page3.getItems());
			assertContains(
				assetListEntry2, (List<AssetListEntry>)page3.getItems());
			assertContains(
				assetListEntry3, (List<AssetListEntry>)page3.getItems());
		}
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPageWithSortDateTime()
		throws Exception {

		testGetAssetLibraryAssetListEntriesPageWithSort(
			EntityField.Type.DATE_TIME,
			(entityField, assetListEntry1, assetListEntry2) -> {
				BeanTestUtil.setProperty(
					assetListEntry1, entityField.getName(),
					new Date(System.currentTimeMillis() - (2 * Time.MINUTE)));
			});
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPageWithSortDouble()
		throws Exception {

		testGetAssetLibraryAssetListEntriesPageWithSort(
			EntityField.Type.DOUBLE,
			(entityField, assetListEntry1, assetListEntry2) -> {
				BeanTestUtil.setProperty(
					assetListEntry1, entityField.getName(), 0.1);
				BeanTestUtil.setProperty(
					assetListEntry2, entityField.getName(), 0.5);
			});
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPageWithSortInteger()
		throws Exception {

		testGetAssetLibraryAssetListEntriesPageWithSort(
			EntityField.Type.INTEGER,
			(entityField, assetListEntry1, assetListEntry2) -> {
				BeanTestUtil.setProperty(
					assetListEntry1, entityField.getName(), 0);
				BeanTestUtil.setProperty(
					assetListEntry2, entityField.getName(), 1);
			});
	}

	@Test
	public void testGetAssetLibraryAssetListEntriesPageWithSortString()
		throws Exception {

		testGetAssetLibraryAssetListEntriesPageWithSort(
			EntityField.Type.STRING,
			(entityField, assetListEntry1, assetListEntry2) -> {
				Class<?> clazz = assetListEntry1.getClass();

				String entityFieldName = entityField.getName();

				Method method = clazz.getMethod(
					"get" + StringUtil.upperCaseFirstLetter(entityFieldName));

				Class<?> returnType = method.getReturnType();

				if (returnType.isAssignableFrom(Map.class)) {
					BeanTestUtil.setProperty(
						assetListEntry1, entityFieldName,
						Collections.singletonMap("Aaa", "Aaa"));
					BeanTestUtil.setProperty(
						assetListEntry2, entityFieldName,
						Collections.singletonMap("Bbb", "Bbb"));
				}
				else if (entityFieldName.contains("email")) {
					BeanTestUtil.setProperty(
						assetListEntry1, entityFieldName,
						"aaa" +
							StringUtil.toLowerCase(
								RandomTestUtil.randomString()) +
									"@liferay.com");
					BeanTestUtil.setProperty(
						assetListEntry2, entityFieldName,
						"bbb" +
							StringUtil.toLowerCase(
								RandomTestUtil.randomString()) +
									"@liferay.com");
				}
				else {
					BeanTestUtil.setProperty(
						assetListEntry1, entityFieldName,
						"aaa" +
							StringUtil.toLowerCase(
								RandomTestUtil.randomString()));
					BeanTestUtil.setProperty(
						assetListEntry2, entityFieldName,
						"bbb" +
							StringUtil.toLowerCase(
								RandomTestUtil.randomString()));
				}
			});
	}

	protected void testGetAssetLibraryAssetListEntriesPageWithSort(
			EntityField.Type type,
			UnsafeTriConsumer
				<EntityField, AssetListEntry, AssetListEntry, Exception>
					unsafeTriConsumer)
		throws Exception {

		List<EntityField> entityFields = getEntityFields(type);

		if (entityFields.isEmpty()) {
			return;
		}

		Long assetLibraryId =
			testGetAssetLibraryAssetListEntriesPage_getAssetLibraryId();

		AssetListEntry assetListEntry1 = randomAssetListEntry();
		AssetListEntry assetListEntry2 = randomAssetListEntry();

		for (EntityField entityField : entityFields) {
			unsafeTriConsumer.accept(
				entityField, assetListEntry1, assetListEntry2);
		}

		assetListEntry1 =
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				assetLibraryId, assetListEntry1);

		assetListEntry2 =
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				assetLibraryId, assetListEntry2);

		Page<AssetListEntry> page =
			assetListEntryResource.getAssetLibraryAssetListEntriesPage(
				assetLibraryId, null, null, null, null);

		for (EntityField entityField : entityFields) {
			Page<AssetListEntry> ascPage =
				assetListEntryResource.getAssetLibraryAssetListEntriesPage(
					assetLibraryId, null, null,
					Pagination.of(1, (int)page.getTotalCount() + 1),
					entityField.getName() + ":asc");

			assertContains(
				assetListEntry1, (List<AssetListEntry>)ascPage.getItems());
			assertContains(
				assetListEntry2, (List<AssetListEntry>)ascPage.getItems());

			Page<AssetListEntry> descPage =
				assetListEntryResource.getAssetLibraryAssetListEntriesPage(
					assetLibraryId, null, null,
					Pagination.of(1, (int)page.getTotalCount() + 1),
					entityField.getName() + ":desc");

			assertContains(
				assetListEntry2, (List<AssetListEntry>)descPage.getItems());
			assertContains(
				assetListEntry1, (List<AssetListEntry>)descPage.getItems());
		}
	}

	protected AssetListEntry
			testGetAssetLibraryAssetListEntriesPage_addAssetListEntry(
				Long assetLibraryId, AssetListEntry assetListEntry)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long testGetAssetLibraryAssetListEntriesPage_getAssetLibraryId()
		throws Exception {

		return testDepotEntry.getDepotEntryId();
	}

	protected Long
			testGetAssetLibraryAssetListEntriesPage_getIrrelevantAssetLibraryId()
		throws Exception {

		return irrelevantDepotEntry.getDepotEntryId();
	}

	@Test
	public void testGetSiteAssetListEntriesPage() throws Exception {
		Long siteId = testGetSiteAssetListEntriesPage_getSiteId();
		Long irrelevantSiteId =
			testGetSiteAssetListEntriesPage_getIrrelevantSiteId();

		Page<AssetListEntry> page =
			assetListEntryResource.getSiteAssetListEntriesPage(
				siteId, null, null, Pagination.of(1, 10), null);

		long totalCount = page.getTotalCount();

		if (irrelevantSiteId != null) {
			AssetListEntry irrelevantAssetListEntry =
				testGetSiteAssetListEntriesPage_addAssetListEntry(
					irrelevantSiteId, randomIrrelevantAssetListEntry());

			page = assetListEntryResource.getSiteAssetListEntriesPage(
				irrelevantSiteId, null, null,
				Pagination.of(1, (int)totalCount + 1), null);

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantAssetListEntry,
				(List<AssetListEntry>)page.getItems());
			assertValid(
				page,
				testGetSiteAssetListEntriesPage_getExpectedActions(
					irrelevantSiteId));
		}

		AssetListEntry assetListEntry1 =
			testGetSiteAssetListEntriesPage_addAssetListEntry(
				siteId, randomAssetListEntry());

		AssetListEntry assetListEntry2 =
			testGetSiteAssetListEntriesPage_addAssetListEntry(
				siteId, randomAssetListEntry());

		page = assetListEntryResource.getSiteAssetListEntriesPage(
			siteId, null, null, Pagination.of(1, 10), null);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(assetListEntry1, (List<AssetListEntry>)page.getItems());
		assertContains(assetListEntry2, (List<AssetListEntry>)page.getItems());
		assertValid(
			page, testGetSiteAssetListEntriesPage_getExpectedActions(siteId));
	}

	protected Map<String, Map<String, String>>
			testGetSiteAssetListEntriesPage_getExpectedActions(Long siteId)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithFilterDateTimeEquals()
		throws Exception {

		List<EntityField> entityFields = getEntityFields(
			EntityField.Type.DATE_TIME);

		if (entityFields.isEmpty()) {
			return;
		}

		Long siteId = testGetSiteAssetListEntriesPage_getSiteId();

		AssetListEntry assetListEntry1 = randomAssetListEntry();

		assetListEntry1 = testGetSiteAssetListEntriesPage_addAssetListEntry(
			siteId, assetListEntry1);

		for (EntityField entityField : entityFields) {
			Page<AssetListEntry> page =
				assetListEntryResource.getSiteAssetListEntriesPage(
					siteId, null,
					getFilterString(entityField, "between", assetListEntry1),
					Pagination.of(1, 2), null);

			assertEquals(
				Collections.singletonList(assetListEntry1),
				(List<AssetListEntry>)page.getItems());
		}
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithFilterDoubleEquals()
		throws Exception {

		testGetSiteAssetListEntriesPageWithFilter(
			"eq", EntityField.Type.DOUBLE);
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithFilterStringContains()
		throws Exception {

		testGetSiteAssetListEntriesPageWithFilter(
			"contains", EntityField.Type.STRING);
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithFilterStringEquals()
		throws Exception {

		testGetSiteAssetListEntriesPageWithFilter(
			"eq", EntityField.Type.STRING);
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithFilterStringStartsWith()
		throws Exception {

		testGetSiteAssetListEntriesPageWithFilter(
			"startswith", EntityField.Type.STRING);
	}

	protected void testGetSiteAssetListEntriesPageWithFilter(
			String operator, EntityField.Type type)
		throws Exception {

		List<EntityField> entityFields = getEntityFields(type);

		if (entityFields.isEmpty()) {
			return;
		}

		Long siteId = testGetSiteAssetListEntriesPage_getSiteId();

		AssetListEntry assetListEntry1 =
			testGetSiteAssetListEntriesPage_addAssetListEntry(
				siteId, randomAssetListEntry());

		@SuppressWarnings("PMD.UnusedLocalVariable")
		AssetListEntry assetListEntry2 =
			testGetSiteAssetListEntriesPage_addAssetListEntry(
				siteId, randomAssetListEntry());

		for (EntityField entityField : entityFields) {
			Page<AssetListEntry> page =
				assetListEntryResource.getSiteAssetListEntriesPage(
					siteId, null,
					getFilterString(entityField, operator, assetListEntry1),
					Pagination.of(1, 2), null);

			assertEquals(
				Collections.singletonList(assetListEntry1),
				(List<AssetListEntry>)page.getItems());
		}
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithPagination()
		throws Exception {

		Long siteId = testGetSiteAssetListEntriesPage_getSiteId();

		Page<AssetListEntry> assetListEntriesPage =
			assetListEntryResource.getSiteAssetListEntriesPage(
				siteId, null, null, null, null);

		int totalCount = GetterUtil.getInteger(
			assetListEntriesPage.getTotalCount());

		AssetListEntry assetListEntry1 =
			testGetSiteAssetListEntriesPage_addAssetListEntry(
				siteId, randomAssetListEntry());

		AssetListEntry assetListEntry2 =
			testGetSiteAssetListEntriesPage_addAssetListEntry(
				siteId, randomAssetListEntry());

		AssetListEntry assetListEntry3 =
			testGetSiteAssetListEntriesPage_addAssetListEntry(
				siteId, randomAssetListEntry());

		// See com.liferay.portal.vulcan.internal.configuration.HeadlessAPICompanyConfiguration#pageSizeLimit

		int pageSizeLimit = 500;

		if (totalCount >= (pageSizeLimit - 2)) {
			Page<AssetListEntry> page1 =
				assetListEntryResource.getSiteAssetListEntriesPage(
					siteId, null, null,
					Pagination.of(
						(int)Math.ceil((totalCount + 1.0) / pageSizeLimit),
						pageSizeLimit),
					null);

			Assert.assertEquals(totalCount + 3, page1.getTotalCount());

			assertContains(
				assetListEntry1, (List<AssetListEntry>)page1.getItems());

			Page<AssetListEntry> page2 =
				assetListEntryResource.getSiteAssetListEntriesPage(
					siteId, null, null,
					Pagination.of(
						(int)Math.ceil((totalCount + 2.0) / pageSizeLimit),
						pageSizeLimit),
					null);

			assertContains(
				assetListEntry2, (List<AssetListEntry>)page2.getItems());

			Page<AssetListEntry> page3 =
				assetListEntryResource.getSiteAssetListEntriesPage(
					siteId, null, null,
					Pagination.of(
						(int)Math.ceil((totalCount + 3.0) / pageSizeLimit),
						pageSizeLimit),
					null);

			assertContains(
				assetListEntry3, (List<AssetListEntry>)page3.getItems());
		}
		else {
			Page<AssetListEntry> page1 =
				assetListEntryResource.getSiteAssetListEntriesPage(
					siteId, null, null, Pagination.of(1, totalCount + 2), null);

			List<AssetListEntry> assetListEntries1 =
				(List<AssetListEntry>)page1.getItems();

			Assert.assertEquals(
				assetListEntries1.toString(), totalCount + 2,
				assetListEntries1.size());

			Page<AssetListEntry> page2 =
				assetListEntryResource.getSiteAssetListEntriesPage(
					siteId, null, null, Pagination.of(2, totalCount + 2), null);

			Assert.assertEquals(totalCount + 3, page2.getTotalCount());

			List<AssetListEntry> assetListEntries2 =
				(List<AssetListEntry>)page2.getItems();

			Assert.assertEquals(
				assetListEntries2.toString(), 1, assetListEntries2.size());

			Page<AssetListEntry> page3 =
				assetListEntryResource.getSiteAssetListEntriesPage(
					siteId, null, null, Pagination.of(1, (int)totalCount + 3),
					null);

			assertContains(
				assetListEntry1, (List<AssetListEntry>)page3.getItems());
			assertContains(
				assetListEntry2, (List<AssetListEntry>)page3.getItems());
			assertContains(
				assetListEntry3, (List<AssetListEntry>)page3.getItems());
		}
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithSortDateTime()
		throws Exception {

		testGetSiteAssetListEntriesPageWithSort(
			EntityField.Type.DATE_TIME,
			(entityField, assetListEntry1, assetListEntry2) -> {
				BeanTestUtil.setProperty(
					assetListEntry1, entityField.getName(),
					new Date(System.currentTimeMillis() - (2 * Time.MINUTE)));
			});
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithSortDouble()
		throws Exception {

		testGetSiteAssetListEntriesPageWithSort(
			EntityField.Type.DOUBLE,
			(entityField, assetListEntry1, assetListEntry2) -> {
				BeanTestUtil.setProperty(
					assetListEntry1, entityField.getName(), 0.1);
				BeanTestUtil.setProperty(
					assetListEntry2, entityField.getName(), 0.5);
			});
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithSortInteger()
		throws Exception {

		testGetSiteAssetListEntriesPageWithSort(
			EntityField.Type.INTEGER,
			(entityField, assetListEntry1, assetListEntry2) -> {
				BeanTestUtil.setProperty(
					assetListEntry1, entityField.getName(), 0);
				BeanTestUtil.setProperty(
					assetListEntry2, entityField.getName(), 1);
			});
	}

	@Test
	public void testGetSiteAssetListEntriesPageWithSortString()
		throws Exception {

		testGetSiteAssetListEntriesPageWithSort(
			EntityField.Type.STRING,
			(entityField, assetListEntry1, assetListEntry2) -> {
				Class<?> clazz = assetListEntry1.getClass();

				String entityFieldName = entityField.getName();

				Method method = clazz.getMethod(
					"get" + StringUtil.upperCaseFirstLetter(entityFieldName));

				Class<?> returnType = method.getReturnType();

				if (returnType.isAssignableFrom(Map.class)) {
					BeanTestUtil.setProperty(
						assetListEntry1, entityFieldName,
						Collections.singletonMap("Aaa", "Aaa"));
					BeanTestUtil.setProperty(
						assetListEntry2, entityFieldName,
						Collections.singletonMap("Bbb", "Bbb"));
				}
				else if (entityFieldName.contains("email")) {
					BeanTestUtil.setProperty(
						assetListEntry1, entityFieldName,
						"aaa" +
							StringUtil.toLowerCase(
								RandomTestUtil.randomString()) +
									"@liferay.com");
					BeanTestUtil.setProperty(
						assetListEntry2, entityFieldName,
						"bbb" +
							StringUtil.toLowerCase(
								RandomTestUtil.randomString()) +
									"@liferay.com");
				}
				else {
					BeanTestUtil.setProperty(
						assetListEntry1, entityFieldName,
						"aaa" +
							StringUtil.toLowerCase(
								RandomTestUtil.randomString()));
					BeanTestUtil.setProperty(
						assetListEntry2, entityFieldName,
						"bbb" +
							StringUtil.toLowerCase(
								RandomTestUtil.randomString()));
				}
			});
	}

	protected void testGetSiteAssetListEntriesPageWithSort(
			EntityField.Type type,
			UnsafeTriConsumer
				<EntityField, AssetListEntry, AssetListEntry, Exception>
					unsafeTriConsumer)
		throws Exception {

		List<EntityField> entityFields = getEntityFields(type);

		if (entityFields.isEmpty()) {
			return;
		}

		Long siteId = testGetSiteAssetListEntriesPage_getSiteId();

		AssetListEntry assetListEntry1 = randomAssetListEntry();
		AssetListEntry assetListEntry2 = randomAssetListEntry();

		for (EntityField entityField : entityFields) {
			unsafeTriConsumer.accept(
				entityField, assetListEntry1, assetListEntry2);
		}

		assetListEntry1 = testGetSiteAssetListEntriesPage_addAssetListEntry(
			siteId, assetListEntry1);

		assetListEntry2 = testGetSiteAssetListEntriesPage_addAssetListEntry(
			siteId, assetListEntry2);

		Page<AssetListEntry> page =
			assetListEntryResource.getSiteAssetListEntriesPage(
				siteId, null, null, null, null);

		for (EntityField entityField : entityFields) {
			Page<AssetListEntry> ascPage =
				assetListEntryResource.getSiteAssetListEntriesPage(
					siteId, null, null,
					Pagination.of(1, (int)page.getTotalCount() + 1),
					entityField.getName() + ":asc");

			assertContains(
				assetListEntry1, (List<AssetListEntry>)ascPage.getItems());
			assertContains(
				assetListEntry2, (List<AssetListEntry>)ascPage.getItems());

			Page<AssetListEntry> descPage =
				assetListEntryResource.getSiteAssetListEntriesPage(
					siteId, null, null,
					Pagination.of(1, (int)page.getTotalCount() + 1),
					entityField.getName() + ":desc");

			assertContains(
				assetListEntry2, (List<AssetListEntry>)descPage.getItems());
			assertContains(
				assetListEntry1, (List<AssetListEntry>)descPage.getItems());
		}
	}

	protected AssetListEntry testGetSiteAssetListEntriesPage_addAssetListEntry(
			Long siteId, AssetListEntry assetListEntry)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long testGetSiteAssetListEntriesPage_getSiteId()
		throws Exception {

		return testGroup.getGroupId();
	}

	protected Long testGetSiteAssetListEntriesPage_getIrrelevantSiteId()
		throws Exception {

		return irrelevantGroup.getGroupId();
	}

	@Test
	public void testBatchEngineDeleteImportTask() throws Exception {
		Assert.assertTrue(true);
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	protected void assertContains(
		AssetListEntry assetListEntry, List<AssetListEntry> assetListEntries) {

		boolean contains = false;

		for (AssetListEntry item : assetListEntries) {
			if (equals(assetListEntry, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			assetListEntries + " does not contain " + assetListEntry, contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(
		AssetListEntry assetListEntry1, AssetListEntry assetListEntry2) {

		Assert.assertTrue(
			assetListEntry1 + " does not equal " + assetListEntry2,
			equals(assetListEntry1, assetListEntry2));
	}

	protected void assertEquals(
		List<AssetListEntry> assetListEntries1,
		List<AssetListEntry> assetListEntries2) {

		Assert.assertEquals(assetListEntries1.size(), assetListEntries2.size());

		for (int i = 0; i < assetListEntries1.size(); i++) {
			AssetListEntry assetListEntry1 = assetListEntries1.get(i);
			AssetListEntry assetListEntry2 = assetListEntries2.get(i);

			assertEquals(assetListEntry1, assetListEntry2);
		}
	}

	protected void assertEqualsIgnoringOrder(
		List<AssetListEntry> assetListEntries1,
		List<AssetListEntry> assetListEntries2) {

		Assert.assertEquals(assetListEntries1.size(), assetListEntries2.size());

		for (AssetListEntry assetListEntry1 : assetListEntries1) {
			boolean contains = false;

			for (AssetListEntry assetListEntry2 : assetListEntries2) {
				if (equals(assetListEntry1, assetListEntry2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				assetListEntries2 + " does not contain " + assetListEntry1,
				contains);
		}
	}

	protected void assertValid(AssetListEntry assetListEntry) throws Exception {
		boolean valid = true;

		if (assetListEntry.getDateCreated() == null) {
			valid = false;
		}

		if (assetListEntry.getDateModified() == null) {
			valid = false;
		}

		if (assetListEntry.getAssetListEntryId() == null) {
			valid = false;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals("assetListEntryId", additionalAssertFieldName)) {
				if (assetListEntry.getAssetListEntryId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("classNameId", additionalAssertFieldName)) {
				if (assetListEntry.getClassNameId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("classPK", additionalAssertFieldName)) {
				if (assetListEntry.getClassPK() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (assetListEntry.getExternalReferenceCode() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("itemSubtype", additionalAssertFieldName)) {
				if (assetListEntry.getItemSubtype() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("itemType", additionalAssertFieldName)) {
				if (assetListEntry.getItemType() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("title", additionalAssertFieldName)) {
				if (assetListEntry.getTitle() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(Page<AssetListEntry> page) {
		assertValid(page, Collections.emptyMap());
	}

	protected void assertValid(
		Page<AssetListEntry> page,
		Map<String, Map<String, String>> expectedActions) {

		boolean valid = false;

		java.util.Collection<AssetListEntry> assetListEntries = page.getItems();

		int size = assetListEntries.size();

		if ((page.getLastPage() > 0) && (page.getPage() > 0) &&
			(page.getPageSize() > 0) && (page.getTotalCount() > 0) &&
			(size > 0)) {

			valid = true;
		}

		Assert.assertTrue(valid);

		assertValid(page.getActions(), expectedActions);
	}

	protected void assertValid(
		Map<String, Map<String, String>> actions1,
		Map<String, Map<String, String>> actions2) {

		for (String key : actions2.keySet()) {
			Map action = actions1.get(key);

			Assert.assertNotNull(key + " does not contain an action", action);

			Map<String, String> expectedAction = actions2.get(key);

			Assert.assertEquals(
				expectedAction.get("method"), action.get("method"));
			Assert.assertEquals(expectedAction.get("href"), action.get("href"));
		}
	}

	protected String[] getAdditionalAssertFieldNames() {
		return new String[0];
	}

	protected List<GraphQLField> getGraphQLFields() throws Exception {
		List<GraphQLField> graphQLFields = new ArrayList<>();

		graphQLFields.add(new GraphQLField("externalReferenceCode"));

		graphQLFields.add(new GraphQLField("assetListEntryId"));

		for (java.lang.reflect.Field field :
				getDeclaredFields(
					com.liferay.headless.delivery.dto.v1_0.AssetListEntry.
						class)) {

			if (!ArrayUtil.contains(
					getAdditionalAssertFieldNames(), field.getName())) {

				continue;
			}

			graphQLFields.addAll(getGraphQLFields(field));
		}

		return graphQLFields;
	}

	protected List<GraphQLField> getGraphQLFields(
			java.lang.reflect.Field... fields)
		throws Exception {

		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field : fields) {
			com.liferay.portal.vulcan.graphql.annotation.GraphQLField
				vulcanGraphQLField = field.getAnnotation(
					com.liferay.portal.vulcan.graphql.annotation.GraphQLField.
						class);

			if (vulcanGraphQLField != null) {
				Class<?> clazz = field.getType();

				if (clazz.isArray()) {
					clazz = clazz.getComponentType();
				}

				List<GraphQLField> childrenGraphQLFields = getGraphQLFields(
					getDeclaredFields(clazz));

				graphQLFields.add(
					new GraphQLField(field.getName(), childrenGraphQLFields));
			}
		}

		return graphQLFields;
	}

	protected String[] getIgnoredEntityFieldNames() {
		return new String[0];
	}

	protected boolean equals(
		AssetListEntry assetListEntry1, AssetListEntry assetListEntry2) {

		if (assetListEntry1 == assetListEntry2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals("assetListEntryId", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						assetListEntry1.getAssetListEntryId(),
						assetListEntry2.getAssetListEntryId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("classNameId", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						assetListEntry1.getClassNameId(),
						assetListEntry2.getClassNameId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("classPK", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						assetListEntry1.getClassPK(),
						assetListEntry2.getClassPK())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("dateCreated", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						assetListEntry1.getDateCreated(),
						assetListEntry2.getDateCreated())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("dateModified", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						assetListEntry1.getDateModified(),
						assetListEntry2.getDateModified())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						assetListEntry1.getExternalReferenceCode(),
						assetListEntry2.getExternalReferenceCode())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("itemSubtype", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						assetListEntry1.getItemSubtype(),
						assetListEntry2.getItemSubtype())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("itemType", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						assetListEntry1.getItemType(),
						assetListEntry2.getItemType())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("title", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						assetListEntry1.getTitle(),
						assetListEntry2.getTitle())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		Map<String, Object> map1, Map<String, Object> map2) {

		if (Objects.equals(map1.keySet(), map2.keySet())) {
			for (Map.Entry<String, Object> entry : map1.entrySet()) {
				if (entry.getValue() instanceof Map) {
					if (!equals(
							(Map)entry.getValue(),
							(Map)map2.get(entry.getKey()))) {

						return false;
					}
				}
				else if (!Objects.deepEquals(
							entry.getValue(), map2.get(entry.getKey()))) {

					return false;
				}
			}

			return true;
		}

		return false;
	}

	protected java.lang.reflect.Field[] getDeclaredFields(Class clazz)
		throws Exception {

		if (clazz.getClassLoader() == null) {
			return new java.lang.reflect.Field[0];
		}

		return TransformUtil.transform(
			ReflectionUtil.getDeclaredFields(clazz),
			field -> {
				if (field.isSynthetic()) {
					return null;
				}

				return field;
			},
			java.lang.reflect.Field.class);
	}

	protected java.util.Collection<EntityField> getEntityFields()
		throws Exception {

		if (!(_assetListEntryResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_assetListEntryResource;

		EntityModel entityModel = entityModelResource.getEntityModel(
			new MultivaluedHashMap());

		if (entityModel == null) {
			return Collections.emptyList();
		}

		Map<String, EntityField> entityFieldsMap =
			entityModel.getEntityFieldsMap();

		return entityFieldsMap.values();
	}

	protected List<EntityField> getEntityFields(EntityField.Type type)
		throws Exception {

		return TransformUtil.transform(
			getEntityFields(),
			entityField -> {
				if (!Objects.equals(entityField.getType(), type) ||
					ArrayUtil.contains(
						getIgnoredEntityFieldNames(), entityField.getName())) {

					return null;
				}

				return entityField;
			});
	}

	protected String getFilterString(
		EntityField entityField, String operator,
		AssetListEntry assetListEntry) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		if (entityFieldName.equals("assetListEntryId")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("classNameId")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("classPK")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("dateCreated")) {
			if (operator.equals("between")) {
				Date date = assetListEntry.getDateCreated();

				sb = new StringBundler();

				sb.append("(");
				sb.append(entityFieldName);
				sb.append(" gt ");
				sb.append(_format.format(date.getTime() - (2 * Time.SECOND)));
				sb.append(" and ");
				sb.append(entityFieldName);
				sb.append(" lt ");
				sb.append(_format.format(date.getTime() + (2 * Time.SECOND)));
				sb.append(")");
			}
			else {
				sb.append(entityFieldName);

				sb.append(" ");
				sb.append(operator);
				sb.append(" ");

				sb.append(_format.format(assetListEntry.getDateCreated()));
			}

			return sb.toString();
		}

		if (entityFieldName.equals("dateModified")) {
			if (operator.equals("between")) {
				Date date = assetListEntry.getDateModified();

				sb = new StringBundler();

				sb.append("(");
				sb.append(entityFieldName);
				sb.append(" gt ");
				sb.append(_format.format(date.getTime() - (2 * Time.SECOND)));
				sb.append(" and ");
				sb.append(entityFieldName);
				sb.append(" lt ");
				sb.append(_format.format(date.getTime() + (2 * Time.SECOND)));
				sb.append(")");
			}
			else {
				sb.append(entityFieldName);

				sb.append(" ");
				sb.append(operator);
				sb.append(" ");

				sb.append(_format.format(assetListEntry.getDateModified()));
			}

			return sb.toString();
		}

		if (entityFieldName.equals("externalReferenceCode")) {
			Object object = assetListEntry.getExternalReferenceCode();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("itemSubtype")) {
			Object object = assetListEntry.getItemSubtype();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("itemType")) {
			Object object = assetListEntry.getItemType();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("title")) {
			Object object = assetListEntry.getTitle();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		throw new IllegalArgumentException(
			"Invalid entity field " + entityFieldName);
	}

	protected String invoke(String query) throws Exception {
		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.body(
			JSONUtil.put(
				"query", query
			).toString(),
			"application/json");
		httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);
		httpInvoker.path(
			"http://localhost:" + PortalUtil.getPortalServerPort(false) +
				"/o/graphql");
		httpInvoker.userNameAndPassword(
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD);

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		return httpResponse.getContent();
	}

	protected JSONObject invokeGraphQLMutation(GraphQLField graphQLField)
		throws Exception {

		GraphQLField mutationGraphQLField = new GraphQLField(
			"mutation", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(mutationGraphQLField.toString()));
	}

	protected JSONObject invokeGraphQLQuery(GraphQLField graphQLField)
		throws Exception {

		GraphQLField queryGraphQLField = new GraphQLField(
			"query", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(queryGraphQLField.toString()));
	}

	protected AssetListEntry randomAssetListEntry() throws Exception {
		return new AssetListEntry() {
			{
				assetListEntryId = RandomTestUtil.randomLong();
				classNameId = RandomTestUtil.randomLong();
				classPK = RandomTestUtil.randomLong();
				dateCreated = RandomTestUtil.nextDate();
				dateModified = RandomTestUtil.nextDate();
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				itemSubtype = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				itemType = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				title = StringUtil.toLowerCase(RandomTestUtil.randomString());
			}
		};
	}

	protected AssetListEntry randomIrrelevantAssetListEntry() throws Exception {
		AssetListEntry randomIrrelevantAssetListEntry = randomAssetListEntry();

		return randomIrrelevantAssetListEntry;
	}

	protected AssetListEntry randomPatchAssetListEntry() throws Exception {
		return randomAssetListEntry();
	}

	protected AssetListEntryResource assetListEntryResource;
	protected com.liferay.portal.kernel.model.Group irrelevantGroup;
	protected com.liferay.portal.kernel.model.Company testCompany;
	protected DepotEntry irrelevantDepotEntry;
	protected com.liferay.portal.kernel.model.Group irrelevantDepotEntryGroup;
	protected DepotEntry testDepotEntry;
	protected com.liferay.portal.kernel.model.Group testDepotEntryGroup;
	protected com.liferay.portal.kernel.model.Group testGroup;

	protected static class BeanTestUtil {

		public static void copyProperties(Object source, Object target)
			throws Exception {

			Class<?> sourceClass = source.getClass();

			Class<?> targetClass = target.getClass();

			for (java.lang.reflect.Field field :
					_getAllDeclaredFields(sourceClass)) {

				if (field.isSynthetic()) {
					continue;
				}

				Method getMethod = _getMethod(
					sourceClass, field.getName(), "get");

				try {
					Method setMethod = _getMethod(
						targetClass, field.getName(), "set",
						getMethod.getReturnType());

					setMethod.invoke(target, getMethod.invoke(source));
				}
				catch (Exception e) {
					continue;
				}
			}
		}

		public static boolean hasProperty(Object bean, String name) {
			Method setMethod = _getMethod(
				bean.getClass(), "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod != null) {
				return true;
			}

			return false;
		}

		public static void setProperty(Object bean, String name, Object value)
			throws Exception {

			Class<?> clazz = bean.getClass();

			Method setMethod = _getMethod(
				clazz, "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod == null) {
				throw new NoSuchMethodException();
			}

			Class<?>[] parameterTypes = setMethod.getParameterTypes();

			setMethod.invoke(bean, _translateValue(parameterTypes[0], value));
		}

		private static List<java.lang.reflect.Field> _getAllDeclaredFields(
			Class<?> clazz) {

			List<java.lang.reflect.Field> fields = new ArrayList<>();

			while ((clazz != null) && (clazz != Object.class)) {
				for (java.lang.reflect.Field field :
						clazz.getDeclaredFields()) {

					fields.add(field);
				}

				clazz = clazz.getSuperclass();
			}

			return fields;
		}

		private static Method _getMethod(Class<?> clazz, String name) {
			for (Method method : clazz.getMethods()) {
				if (name.equals(method.getName()) &&
					(method.getParameterCount() == 1) &&
					_parameterTypes.contains(method.getParameterTypes()[0])) {

					return method;
				}
			}

			return null;
		}

		private static Method _getMethod(
				Class<?> clazz, String fieldName, String prefix,
				Class<?>... parameterTypes)
			throws Exception {

			return clazz.getMethod(
				prefix + StringUtil.upperCaseFirstLetter(fieldName),
				parameterTypes);
		}

		private static Object _translateValue(
			Class<?> parameterType, Object value) {

			if ((value instanceof Integer) &&
				parameterType.equals(Long.class)) {

				Integer intValue = (Integer)value;

				return intValue.longValue();
			}

			return value;
		}

		private static final Set<Class<?>> _parameterTypes = new HashSet<>(
			Arrays.asList(
				Boolean.class, Date.class, Double.class, Integer.class,
				Long.class, Map.class, String.class));

	}

	protected class GraphQLField {

		public GraphQLField(String key, GraphQLField... graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(String key, List<GraphQLField> graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			GraphQLField... graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = Arrays.asList(graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			List<GraphQLField> graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = graphQLFields;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(_key);

			if (!_parameterMap.isEmpty()) {
				sb.append("(");

				for (Map.Entry<String, Object> entry :
						_parameterMap.entrySet()) {

					sb.append(entry.getKey());
					sb.append(": ");
					sb.append(entry.getValue());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append(")");
			}

			if (!_graphQLFields.isEmpty()) {
				sb.append("{");

				for (GraphQLField graphQLField : _graphQLFields) {
					sb.append(graphQLField.toString());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append("}");
			}

			return sb.toString();
		}

		private final List<GraphQLField> _graphQLFields;
		private final String _key;
		private final Map<String, Object> _parameterMap;

	}

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(BaseAssetListEntryResourceTestCase.class);

	private static Format _format;

	private com.liferay.portal.kernel.model.User _testCompanyAdminUser;

	@Inject
	private com.liferay.headless.delivery.resource.v1_0.AssetListEntryResource
		_assetListEntryResource;

}
// LIFERAY-REST-BUILDER-HASH:-686239852