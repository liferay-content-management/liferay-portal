/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryMetadata;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.model.DLFileEntryTypeConstants;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.dynamic.data.mapping.constants.DDMStructureConstants;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.storage.StorageType;
import com.liferay.headless.delivery.client.dto.v1_0.Creator;
import com.liferay.headless.delivery.client.dto.v1_0.Document;
import com.liferay.headless.delivery.client.dto.v1_0.DocumentType;
import com.liferay.headless.delivery.client.http.HttpInvoker;
import com.liferay.headless.delivery.client.resource.v1_0.DocumentResource;
import com.liferay.headless.delivery.client.serdes.v1_0.DocumentSerDes;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.constants.TestDataConstants;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.util.PropsValues;
import com.liferay.ratings.kernel.service.RatingsEntryLocalService;

import java.io.File;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Javier Gamarra
 */
@RunWith(Arquillian.class)
public class DocumentResourceTest extends BaseDocumentResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Override
	@Test
	public void testDeleteDocumentMyRating() throws Exception {
		super.testDeleteDocumentMyRating();

		Document document = testDeleteDocumentMyRating_addDocument();

		assertHttpResponseStatusCode(
			204,
			documentResource.deleteDocumentMyRatingHttpResponse(
				document.getId()));
		assertHttpResponseStatusCode(
			404,
			documentResource.deleteDocumentMyRatingHttpResponse(
				document.getId()));

		Document irrelevantDocument = randomIrrelevantDocument();

		assertHttpResponseStatusCode(
			404,
			documentResource.deleteDocumentMyRatingHttpResponse(
				irrelevantDocument.getId()));
	}

	@FeatureFlags("LPS-10701")
	@Override
	@Test
	public void testGetDocument() throws Exception {
		super.testGetDocument();

		Document document1 = documentResource.postSiteDocument(
			testGroup.getGroupId(), randomDocument(), getMultipartFiles());

		Assert.assertTrue(Validator.isNotNull(document1.getContentUrl()));
		Assert.assertTrue(Validator.isNotNull(document1.getDateExpired()));
		Assert.assertTrue(Validator.isNotNull(document1.getDatePublished()));
		Assert.assertTrue(Validator.isNotNull(document1.getFriendlyUrlPath()));

		Document document2 = documentResource.postSiteDocument(
			testGroup.getGroupId(), randomDocument(),
			HashMapBuilder.put(
				"file", () -> FileUtil.createTempFile(new byte[0])
			).build());

		Assert.assertTrue(Validator.isNull(document2.getContentUrl()));
		Assert.assertTrue(Validator.isNotNull(document2.getDateExpired()));
		Assert.assertTrue(Validator.isNotNull(document2.getDatePublished()));
		Assert.assertTrue(Validator.isNotNull(document2.getFriendlyUrlPath()));

		Role guestRole = _roleLocalService.getRole(
			testCompany.getCompanyId(), RoleConstants.GUEST);

		_resourcePermissionLocalService.removeResourcePermission(
			testCompany.getCompanyId(), DLFileEntry.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(document1.getId()), guestRole.getRoleId(),
			ActionKeys.DOWNLOAD);

		DocumentResource.Builder builder = DocumentResource.builder();

		String password = StringUtil.randomString();

		User user = UserTestUtil.addUser(
			testCompany.getCompanyId(), testCompany.getUserId(), password,
			RandomTestUtil.randomString() + "@liferay.com",
			RandomTestUtil.randomString(), LocaleUtil.getDefault(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			ServiceContextTestUtil.getServiceContext());

		DocumentResource regularUserDocumentResource = builder.authentication(
			user.getLogin(), password
		).build();

		document1 = regularUserDocumentResource.getDocument(document1.getId());

		Assert.assertTrue(Validator.isNull(document1.getContentUrl()));
		Assert.assertTrue(Validator.isNotNull(document1.getDateExpired()));
		Assert.assertTrue(Validator.isNotNull(document1.getDatePublished()));
		Assert.assertTrue(Validator.isNotNull(document1.getFriendlyUrlPath()));
	}

	@Override
	@Test
	public void testGetDocumentRenderedContentByDisplayPageDisplayPageKey()
		throws Exception {
	}

	@Override
	@Test
	public void testGraphQLGetSiteDocumentsPage() throws Exception {
		Document document1 = testGraphQLDocument_addDocument();
		Document document2 = testGraphQLDocument_addDocument();

		JSONObject documentsJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(
				new GraphQLField(
					"documents",
					HashMapBuilder.<String, Object>put(
						"flatten", true
					).put(
						"page", 1
					).put(
						"pageSize", 2
					).put(
						"siteKey", "\"" + testGroup.getGroupId() + "\""
					).build(),
					new GraphQLField("items", getGraphQLFields()),
					new GraphQLField("page"), new GraphQLField("totalCount"))),
			"JSONObject/data", "JSONObject/documents");

		Assert.assertEquals(2, documentsJSONObject.get("totalCount"));

		assertEqualsIgnoringOrder(
			Arrays.asList(document1, document2),
			Arrays.asList(
				DocumentSerDes.toDTOs(documentsJSONObject.getString("items"))));
	}

	@Override
	@Test
	public void testPostDocumentFolderDocument() throws Exception {
		super.testPostDocumentFolderDocument();

		_testPostDocumentFolderDocumentWithDLFileEntryType();
	}

	@Override
	@Test
	public void testPutSiteDocumentByExternalReferenceCode() throws Exception {
		super.testPutSiteDocumentByExternalReferenceCode();

		DLFolder dlFolder = _dlFolderLocalService.addFolder(
			null, TestPropsValues.getUserId(), testGroup.getGroupId(),
			testGroup.getGroupId(), false,
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), false,
			ServiceContextTestUtil.getServiceContext(testGroup.getGroupId()));

		Document randomDocument = randomDocument();

		randomDocument.setDocumentFolderId(dlFolder.getFolderId());

		Document putDocument =
			documentResource.putSiteDocumentByExternalReferenceCode(
				randomDocument.getSiteId(),
				randomDocument.getExternalReferenceCode(), randomDocument,
				getMultipartFiles());

		Assert.assertEquals(
			(Long)dlFolder.getFolderId(), putDocument.getDocumentFolderId());
	}

	@Override
	protected void assertValid(
			Document document, Map<String, File> multipartFiles)
		throws Exception {

		Assert.assertEquals(
			new String(FileUtil.getBytes(multipartFiles.get("file"))),
			_read("http://localhost:8080" + document.getContentUrl()));
	}

	@Override
	protected boolean equals(Document document1, Document document2) {
		if (document1 == document2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals(additionalAssertFieldName, "actions")) {
				if (!equals(
						(Map)document1.getActions(),
						(Map)document2.getActions())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "adaptedImages")) {
				if (!Objects.deepEquals(
						document1.getAdaptedImages(),
						document2.getAdaptedImages())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "aggregateRating")) {
				if (!Objects.deepEquals(
						document1.getAggregateRating(),
						document2.getAggregateRating())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "contentUrl")) {
				if (!Objects.deepEquals(
						document1.getContentUrl(), document2.getContentUrl())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "contentValue")) {
				if (!Objects.deepEquals(
						document1.getContentValue(),
						document2.getContentValue())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "creator")) {
				if (!Objects.deepEquals(
						document1.getCreator(), document2.getCreator())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "customFields")) {
				if (!Objects.deepEquals(
						document1.getCustomFields(),
						document2.getCustomFields())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "dateCreated")) {
				if (!Objects.deepEquals(
						document1.getDateCreated(),
						document2.getDateCreated())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "dateExpired")) {
				if (!Objects.deepEquals(
						document1.getDateExpired(),
						document2.getDateExpired())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "dateModified")) {
				if (!Objects.deepEquals(
						document1.getDateModified(),
						document2.getDateModified())) {

					return false;
				}

				continue;
			}

			if (FeatureFlagManagerUtil.isEnabled(
					testCompany.getCompanyId(), "LPD-10701") &&
				Objects.equals(additionalAssertFieldName, "datePublished")) {

				if (!Objects.deepEquals(
						document1.getDatePublished(),
						document2.getDatePublished())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "description")) {
				if (!Objects.deepEquals(
						document1.getDescription(),
						document2.getDescription())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "documentFolderId")) {
				if (!Objects.deepEquals(
						document1.getDocumentFolderId(),
						document2.getDocumentFolderId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "documentType")) {
				if (!Objects.deepEquals(
						document1.getDocumentType(),
						document2.getDocumentType())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "encodingFormat")) {
				if (!Objects.deepEquals(
						document1.getEncodingFormat(),
						document2.getEncodingFormat())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					additionalAssertFieldName, "externalReferenceCode")) {

				if (!Objects.deepEquals(
						document1.getExternalReferenceCode(),
						document2.getExternalReferenceCode())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "fileExtension")) {
				if (!Objects.deepEquals(
						document1.getFileExtension(),
						document2.getFileExtension())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "fileName")) {
				if (!Objects.deepEquals(
						document1.getFileName(), document2.getFileName())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "friendlyUrlPath")) {
				if (!Objects.deepEquals(
						document1.getFriendlyUrlPath(),
						document2.getFriendlyUrlPath())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "id")) {
				if (!Objects.deepEquals(document1.getId(), document2.getId())) {
					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "keywords")) {
				if (!Objects.deepEquals(
						document1.getKeywords(), document2.getKeywords())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "numberOfComments")) {
				if (!Objects.deepEquals(
						document1.getNumberOfComments(),
						document2.getNumberOfComments())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "relatedContents")) {
				if (!Objects.deepEquals(
						document1.getRelatedContents(),
						document2.getRelatedContents())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "renderedContents")) {
				if (!Objects.deepEquals(
						document1.getRenderedContents(),
						document2.getRenderedContents())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "sizeInBytes")) {
				if (!Objects.deepEquals(
						document1.getSizeInBytes(),
						document2.getSizeInBytes())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					additionalAssertFieldName, "taxonomyCategoryBriefs")) {

				if (!Objects.deepEquals(
						document1.getTaxonomyCategoryBriefs(),
						document2.getTaxonomyCategoryBriefs())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					additionalAssertFieldName, "taxonomyCategoryIds")) {

				if (!Objects.deepEquals(
						document1.getTaxonomyCategoryIds(),
						document2.getTaxonomyCategoryIds())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "title")) {
				if (!Objects.deepEquals(
						document1.getTitle(), document2.getTitle())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(additionalAssertFieldName, "viewableBy")) {
				if (!Objects.deepEquals(
						document1.getViewableBy(), document2.getViewableBy())) {

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

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"description", "fileName", "title"};
	}

	@Override
	protected String getFilterString(
		EntityField entityField, String operator, Document document) {

		StringBundler sb = new StringBundler(4);

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		if (entityFieldName.equals("actions") ||
			entityFieldName.equals("adaptedImages") ||
			entityFieldName.equals("aggregateRating")) {

			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("assetLibraryKey")) {
			Object object = document.getAssetLibraryKey();

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

		if (entityFieldName.equals("contentUrl")) {
			Object object = document.getContentUrl();

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

		if (entityFieldName.equals("contentValue")) {
			Object object = document.getContentValue();

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

		if (entityFieldName.equals("creator") ||
			entityFieldName.equals("customFields")) {

			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("dateCreated")) {
			if (operator.equals("between")) {
				Date date = document.getDateCreated();

				sb = new StringBundler();

				sb.append("(");
				sb.append(entityFieldName);
				sb.append(" gt ");
				sb.append(
					_dateFormat.format(date.getTime() - (2 * Time.SECOND)));
				sb.append(" and ");
				sb.append(entityFieldName);
				sb.append(" lt ");
				sb.append(
					_dateFormat.format(date.getTime() + (2 * Time.SECOND)));
				sb.append(")");
			}
			else {
				sb.append(entityFieldName);

				sb.append(" ");
				sb.append(operator);
				sb.append(" ");

				sb.append(_dateFormat.format(document.getDateCreated()));
			}

			return sb.toString();
		}

		if (entityFieldName.equals("dateExpired")) {
			if (operator.equals("between")) {
				Date date = document.getDateExpired();

				sb = new StringBundler();

				sb.append("(");
				sb.append(entityFieldName);
				sb.append(" gt ");
				sb.append(
					_dateFormat.format(date.getTime() - (2 * Time.SECOND)));
				sb.append(" and ");
				sb.append(entityFieldName);
				sb.append(" lt ");
				sb.append(
					_dateFormat.format(date.getTime() + (2 * Time.SECOND)));
				sb.append(")");
			}
			else {
				sb.append(entityFieldName);

				sb.append(" ");
				sb.append(operator);
				sb.append(" ");

				sb.append(_dateFormat.format(document.getDateExpired()));
			}

			return sb.toString();
		}

		if (entityFieldName.equals("dateModified")) {
			if (operator.equals("between")) {
				Date date = document.getDateModified();

				sb = new StringBundler();

				sb.append("(");
				sb.append(entityFieldName);
				sb.append(" gt ");
				sb.append(
					_dateFormat.format(date.getTime() - (2 * Time.SECOND)));
				sb.append(" and ");
				sb.append(entityFieldName);
				sb.append(" lt ");
				sb.append(
					_dateFormat.format(date.getTime() + (2 * Time.SECOND)));
				sb.append(")");
			}
			else {
				sb.append(entityFieldName);

				sb.append(" ");
				sb.append(operator);
				sb.append(" ");

				sb.append(_dateFormat.format(document.getDateModified()));
			}

			return sb.toString();
		}

		if (FeatureFlagManagerUtil.isEnabled(
				testCompany.getCompanyId(), "LPD-10701") &&
			entityFieldName.equals("datePublished")) {

			if (operator.equals("between")) {
				Date date = document.getDatePublished();

				sb = new StringBundler();

				sb.append("(");
				sb.append(entityFieldName);
				sb.append(" gt ");
				sb.append(
					_dateFormat.format(date.getTime() - (2 * Time.SECOND)));
				sb.append(" and ");
				sb.append(entityFieldName);
				sb.append(" lt ");
				sb.append(
					_dateFormat.format(date.getTime() + (2 * Time.SECOND)));
				sb.append(")");
			}
			else {
				sb.append(entityFieldName);

				sb.append(" ");
				sb.append(operator);
				sb.append(" ");

				sb.append(_dateFormat.format(document.getDatePublished()));
			}

			return sb.toString();
		}

		if (entityFieldName.equals("description")) {
			Object object = document.getDescription();

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

		if (entityFieldName.equals("documentFolderId") ||
			entityFieldName.equals("documentType")) {

			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("encodingFormat")) {
			Object object = document.getEncodingFormat();

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

		if (entityFieldName.equals("externalReferenceCode")) {
			Object object = document.getExternalReferenceCode();

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

		if (entityFieldName.equals("fileExtension")) {
			Object object = document.getFileExtension();

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

		if (entityFieldName.equals("fileName")) {
			Object object = document.getFileName();

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

		if (entityFieldName.equals("friendlyUrlPath")) {
			Object object = document.getFriendlyUrlPath();

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

		if (entityFieldName.equals("id") ||
			entityFieldName.equals("keywords")) {

			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("numberOfComments")) {
			sb.append(String.valueOf(document.getNumberOfComments()));

			return sb.toString();
		}

		if (entityFieldName.equals("relatedContents") ||
			entityFieldName.equals("renderedContents") ||
			entityFieldName.equals("siteId") ||
			entityFieldName.equals("sizeInBytes") ||
			entityFieldName.equals("taxonomyCategoryBriefs") ||
			entityFieldName.equals("taxonomyCategoryIds")) {

			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("title")) {
			Object object = document.getTitle();

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

		if (entityFieldName.equals("viewableBy")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		throw new IllegalArgumentException(
			"Invalid entity field " + entityFieldName);
	}

	@Override
	protected String[] getIgnoredEntityFieldNames() {
		return new String[] {"creatorId", "fileExtension", "sizeInBytes"};
	}

	@Override
	protected Map<String, File> getMultipartFiles() {
		return HashMapBuilder.<String, File>put(
			"file",
			() -> FileUtil.createTempFile(TestDataConstants.TEST_BYTE_ARRAY)
		).build();
	}

	@Override
	protected Document randomDocument() throws Exception {
		Document document = super.randomDocument();

		document.setDocumentFolderId(0L);
		document.setViewableBy(Document.ViewableBy.ANYONE);

		return document;
	}

	@Override
	protected Document
			testDeleteAssetLibraryDocumentByExternalReferenceCode_addDocument()
		throws Exception {

		return documentResource.postAssetLibraryDocument(
			testDepotEntry.getDepotEntryId(), randomDocument(),
			getMultipartFiles());
	}

	@Override
	protected Long
			testDeleteAssetLibraryDocumentByExternalReferenceCode_getAssetLibraryId()
		throws Exception {

		return testDepotEntry.getDepotEntryId();
	}

	@Override
	protected Document testDeleteDocumentMyRating_addDocument()
		throws Exception {

		Document document = super.testDeleteDocumentMyRating_addDocument();

		documentResource.putDocumentMyRating(document.getId(), randomRating());

		return document;
	}

	@Override
	protected Document
			testGetAssetLibraryDocumentByExternalReferenceCode_addDocument()
		throws Exception {

		return testPostAssetLibraryDocument_addDocument(
			randomDocument(), getMultipartFiles());
	}

	@Override
	protected Long
			testGetAssetLibraryDocumentByExternalReferenceCode_getAssetLibraryId()
		throws Exception {

		return testDepotEntry.getDepotEntryId();
	}

	@Override
	protected Document testGetAssetLibraryDocumentsRatedByMePage_addDocument(
			Long assetLibraryId, Document document)
		throws Exception {

		Document addedDocument =
			super.testGetAssetLibraryDocumentsRatedByMePage_addDocument(
				assetLibraryId, document);

		_addDocumentRatingsEntry(addedDocument);

		return addedDocument;
	}

	@Override
	protected Long testGetDocumentFolderDocumentsPage_getDocumentFolderId()
		throws Exception {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGuestPermissions(true);

		Folder folder = DLAppLocalServiceUtil.addFolder(
			null, UserLocalServiceUtil.getGuestUserId(testGroup.getCompanyId()),
			testGroup.getGroupId(), 0, RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), serviceContext);

		return folder.getFolderId();
	}

	@Override
	protected Document testGetSiteDocumentsRatedByMePage_addDocument(
			Long siteId, Document document)
		throws Exception {

		Document addedDocument =
			super.testGetSiteDocumentsRatedByMePage_addDocument(
				siteId, document);

		_addDocumentRatingsEntry(addedDocument);

		return addedDocument;
	}

	@Override
	protected Document testGraphQLDocument_addDocument() throws Exception {
		return testPostDocumentFolderDocument_addDocument(
			randomDocument(), getMultipartFiles());
	}

	@Override
	protected Document
			testGraphQLGetAssetLibraryDocumentByExternalReferenceCode_addDocument()
		throws Exception {

		return testGetAssetLibraryDocumentByExternalReferenceCode_addDocument();
	}

	@Override
	protected Long
			testGraphQLGetAssetLibraryDocumentByExternalReferenceCode_getAssetLibraryId()
		throws Exception {

		return testDepotEntry.getDepotEntryId();
	}

	@Override
	protected Long
			testPutAssetLibraryDocumentByExternalReferenceCode_getAssetLibraryId()
		throws Exception {

		return testDepotEntry.getDepotEntryId();
	}

	private void _addDocumentRatingsEntry(Document document) throws Exception {
		Creator creator = document.getCreator();

		_ratingsEntryLocalService.updateEntry(
			creator.getId(), DLFileEntry.class.getName(), document.getId(), 1.0,
			ServiceContextTestUtil.getServiceContext(testGroup.getGroupId()));
	}

	private DLFileEntryType _addFileEntryType(Group group) throws Exception {
		DDMStructure ddmStructure = _ddmStructureLocalService.addStructure(
			group.getCreatorUserId(), group.getGroupId(),
			DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID,
			PortalUtil.getClassNameId(DLFileEntryMetadata.class),
			StringPool.BLANK,
			HashMapBuilder.put(
				LocaleUtil.getDefault(),
				DLFileEntryMetadata.class.getSimpleName()
			).build(),
			new HashMap<>(), StringPool.BLANK, StorageType.DEFAULT.toString(),
			ServiceContextTestUtil.getServiceContext(group.getGroupId()));

		return _dlFileEntryTypeLocalService.addFileEntryType(
			group.getCreatorUserId(), group.getGroupId(),
			ddmStructure.getStructureId(), null,
			HashMapBuilder.put(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()
			).build(),
			new HashMap<>(),
			DLFileEntryTypeConstants.FILE_ENTRY_TYPE_SCOPE_DEFAULT,
			ServiceContextTestUtil.getServiceContext(group.getGroupId()));
	}

	private void _assertDLFileEntryType(
			DLFileEntryType dlFileEntryType, Group group)
		throws Exception {

		DLFolder dlFolder = _dlFolderLocalService.addFolder(
			null, TestPropsValues.getUserId(), group.getGroupId(),
			group.getGroupId(), false,
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), false,
			ServiceContextTestUtil.getServiceContext(group.getGroupId()));

		Document randomDocument = randomDocument();

		randomDocument.setDocumentType(
			new DocumentType() {
				{
					name = dlFileEntryType.getName(LocaleUtil.getDefault());
				}
			});
		randomDocument.setSiteId(group.getGroupId());

		Document postDocument = documentResource.postDocumentFolderDocument(
			dlFolder.getFolderId(), randomDocument, getMultipartFiles());

		DocumentType documentType = postDocument.getDocumentType();

		Assert.assertNotNull(documentType);
		Assert.assertEquals(
			dlFileEntryType.getName(LocaleUtil.getDefault()),
			documentType.getName());
	}

	private String _read(String url) throws Exception {
		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.httpMethod(HttpInvoker.HttpMethod.GET);
		httpInvoker.path(url);
		httpInvoker.userNameAndPassword(
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD);

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		return httpResponse.getContent();
	}

	private void _testPostDocumentFolderDocumentWithDLFileEntryType()
		throws Exception {

		DLFileEntryType dlFileEntryType = _addFileEntryType(testGroup);

		_assertDLFileEntryType(dlFileEntryType, testGroup);

		Group childGroup = GroupTestUtil.addGroup(testGroup.getGroupId());

		_assertDLFileEntryType(dlFileEntryType, childGroup);

		GroupTestUtil.deleteGroup(childGroup);
	}

	@Inject
	private DDMStructureLocalService _ddmStructureLocalService;

	@Inject
	private DLFileEntryTypeLocalService _dlFileEntryTypeLocalService;

	@Inject
	private DLFolderLocalService _dlFolderLocalService;

	@Inject
	private RatingsEntryLocalService _ratingsEntryLocalService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

}