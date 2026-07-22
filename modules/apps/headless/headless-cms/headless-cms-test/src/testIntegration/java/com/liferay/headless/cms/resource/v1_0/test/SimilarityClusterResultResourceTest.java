/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.cms.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.headless.cms.client.dto.v1_0.SimilarityCluster;
import com.liferay.headless.cms.client.dto.v1_0.SimilarityClusterAsset;
import com.liferay.headless.cms.client.dto.v1_0.SimilarityClusterResult;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.cms.site.initializer.test.util.CMSTestUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@FeatureFlag("LPD-17564")
@RunWith(Arquillian.class)
public class SimilarityClusterResultResourceTest
	extends BaseSimilarityClusterResultResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Override
	@Test
	public void testGetSimilarityCluster() throws Exception {
		DepotEntry depotEntry = _addSpaceDepotEntry(
			ServiceContextTestUtil.getServiceContext());

		long groupId = depotEntry.getGroupId();

		ObjectDefinition objectDefinition =
			_getBasicWebContentObjectDefinition();

		// No content yet

		SimilarityClusterResult similarityClusterResult =
			similarityClusterResultResource.getSimilarityCluster(
				groupId, "TEXT");

		Assert.assertEquals(
			0, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		SimilarityCluster[] similarityClusters =
			similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 0, similarityClusters.length);

		// Two near-duplicate assets and one distinct asset

		ObjectEntry nearDuplicateObjectEntry1 = _addObjectEntry(
			depotEntry, objectDefinition, "Reset Your Password",
			_NEAR_DUPLICATE_CONTENT);
		ObjectEntry nearDuplicateObjectEntry2 = _addObjectEntry(
			depotEntry, objectDefinition, "Reset Your Password",
			_NEAR_DUPLICATE_CONTENT +
				" You can also contact support for help.");

		_addObjectEntry(
			depotEntry, objectDefinition, "Quarterly Sales Report",
			_DISTINCT_CONTENT);

		similarityClusterResult =
			similarityClusterResultResource.getSimilarityCluster(
				groupId, "TEXT");

		Assert.assertEquals(
			2, GetterUtil.getLong(similarityClusterResult.getTotalCount()));

		similarityClusters = similarityClusterResult.getSimilarityClusters();

		Assert.assertEquals(
			Arrays.toString(similarityClusters), 1, similarityClusters.length);

		SimilarityCluster similarityCluster = similarityClusters[0];

		Assert.assertEquals(
			2, GetterUtil.getInteger(similarityCluster.getSize()));

		SimilarityClusterAsset[] similarityClusterAssets =
			similarityCluster.getSimilarityClusterAssets();

		Assert.assertEquals(
			Arrays.toString(similarityClusterAssets), 2,
			similarityClusterAssets.length);

		List<Long> objectEntryIds = new ArrayList<>();

		int topAssetCount = 0;

		for (SimilarityClusterAsset similarityClusterAsset :
				similarityClusterAssets) {

			objectEntryIds.add(similarityClusterAsset.getId());

			Assert.assertEquals(
				"Reset Your Password", similarityClusterAsset.getTitle());
			Assert.assertNotNull(similarityClusterAsset.getContentType());
			Assert.assertNotNull(similarityClusterAsset.getDateModified());

			if (GetterUtil.getBoolean(similarityClusterAsset.getTopAsset())) {
				topAssetCount++;

				Assert.assertNull(
					similarityClusterAsset.getSimilarityPercent());
			}
			else {
				double similarityPercent = GetterUtil.getDouble(
					similarityClusterAsset.getSimilarityPercent());

				Assert.assertTrue(
					"Similarity percent must be in (0, 100], was " +
						similarityPercent,
					(similarityPercent > 0) && (similarityPercent <= 100));
			}
		}

		Assert.assertEquals(1, topAssetCount);
		Assert.assertTrue(
			objectEntryIds.contains(
				nearDuplicateObjectEntry1.getObjectEntryId()));
		Assert.assertTrue(
			objectEntryIds.contains(
				nearDuplicateObjectEntry2.getObjectEntryId()));

		_depotEntryLocalService.deleteDepotEntry(depotEntry.getDepotEntryId());
	}

	@Override
	@Test
	public void testGraphQLGetSimilarityCluster() throws Exception {
	}

	private ObjectEntry _addObjectEntry(
			DepotEntry depotEntry, ObjectDefinition objectDefinition,
			String title, String content)
		throws Exception {

		ObjectEntryFolder objectEntryFolder =
			_objectEntryFolderLocalService.
				getObjectEntryFolderByExternalReferenceCode(
					"L_CONTENTS", depotEntry.getGroupId(),
					depotEntry.getCompanyId());

		return _objectEntryLocalService.addObjectEntry(
			depotEntry.getGroupId(), depotEntry.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			objectEntryFolder.getObjectEntryFolderId(), "en_US",
			HashMapBuilder.<String, Serializable>put(
				"content_i18n",
				HashMapBuilder.put(
					"en_US", (Serializable)content
				).build()
			).put(
				"title_i18n",
				HashMapBuilder.put(
					"en_US", (Serializable)title
				).build()
			).build(),
			ServiceContextTestUtil.getServiceContext());
	}

	private DepotEntry _addSpaceDepotEntry(ServiceContext serviceContext)
		throws Exception {

		return _depotEntryLocalService.addDepotEntry(
			HashMapBuilder.put(
				LocaleUtil.getDefault(), StringUtil.randomString()
			).build(),
			HashMapBuilder.put(
				LocaleUtil.getDefault(), StringUtil.randomString()
			).build(),
			DepotConstants.TYPE_SPACE, serviceContext);
	}

	private ObjectDefinition _getBasicWebContentObjectDefinition()
		throws Exception {

		Group cmsGroup = CMSTestUtil.getOrAddGroup(
			SimilarityClusterResultResourceTest.class);

		return _objectDefinitionLocalService.
			getObjectDefinitionByExternalReferenceCode(
				"L_CMS_BASIC_WEB_CONTENT", cmsGroup.getCompanyId());
	}

	private static final String _DISTINCT_CONTENT =
		"The quarterly sales report shows strong revenue growth across the " +
			"European market with product categories in retail and wholesale " +
				"increasing during the last fiscal period.";

	private static final String _NEAR_DUPLICATE_CONTENT =
		"If you forgot your password go to the login page and click the " +
			"forgot password link enter your email address and you will " +
				"receive an email with instructions to create a new password.";

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}