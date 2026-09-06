/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.category.facet.portlet;

import com.liferay.asset.kernel.exception.NoSuchVocabularyException;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.portlet.PortletPreferences;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Shrilakshmi Reddy
 */
public class CategoryFacetPortletPreferencesImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetVocabularyIdsWhenExternalReferenceCodeIsPairedWithOwningGroup()
		throws Exception {

		String groupExternalReferenceCode = RandomTestUtil.randomString();
		String vocabularyExternalReferenceCode = RandomTestUtil.randomString();

		long groupId = RandomTestUtil.randomLong();
		long vocabularyId = RandomTestUtil.randomLong();

		Group group = Mockito.mock(Group.class);

		Mockito.when(
			group.getGroupId()
		).thenReturn(
			groupId
		);

		GroupLocalService groupLocalService = Mockito.mock(
			GroupLocalService.class);

		Mockito.when(
			groupLocalService.getGroupByExternalReferenceCode(
				Mockito.eq(groupExternalReferenceCode), Mockito.anyLong())
		).thenReturn(
			group
		);

		AssetVocabulary assetVocabulary = Mockito.mock(AssetVocabulary.class);

		Mockito.when(
			assetVocabulary.getVocabularyId()
		).thenReturn(
			vocabularyId
		);

		AssetVocabularyLocalService assetVocabularyLocalService = Mockito.mock(
			AssetVocabularyLocalService.class);

		Mockito.when(
			assetVocabularyLocalService.
				getAssetVocabularyByExternalReferenceCode(
					vocabularyExternalReferenceCode, groupId)
		).thenReturn(
			assetVocabulary
		);

		CategoryFacetPortletPreferencesImpl
			categoryFacetPortletPreferencesImpl =
				_createCategoryFacetPortletPreferencesImpl(
					assetVocabularyLocalService, groupLocalService,
					groupExternalReferenceCode + "&&" +
						vocabularyExternalReferenceCode);

		Assert.assertArrayEquals(
			new String[] {String.valueOf(vocabularyId)},
			categoryFacetPortletPreferencesImpl.getVocabularyIds());
	}

	@Test
	public void testGetVocabularyIdsWhenExternalReferenceCodeIsPairedWithUnrelatedGroup()
		throws Exception {

		String groupExternalReferenceCode = RandomTestUtil.randomString();
		String vocabularyExternalReferenceCode = RandomTestUtil.randomString();

		Group group = Mockito.mock(Group.class);

		Mockito.when(
			group.getGroupId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		GroupLocalService groupLocalService = Mockito.mock(
			GroupLocalService.class);

		Mockito.when(
			groupLocalService.getGroupByExternalReferenceCode(
				Mockito.eq(groupExternalReferenceCode), Mockito.anyLong())
		).thenReturn(
			group
		);

		AssetVocabularyLocalService assetVocabularyLocalService = Mockito.mock(
			AssetVocabularyLocalService.class);

		Mockito.when(
			assetVocabularyLocalService.
				getAssetVocabularyByExternalReferenceCode(
					Mockito.eq(vocabularyExternalReferenceCode),
					Mockito.anyLong())
		).thenThrow(
			new NoSuchVocabularyException()
		);

		CategoryFacetPortletPreferencesImpl
			categoryFacetPortletPreferencesImpl =
				_createCategoryFacetPortletPreferencesImpl(
					assetVocabularyLocalService, groupLocalService,
					groupExternalReferenceCode + "&&" +
						vocabularyExternalReferenceCode);

		Assert.assertArrayEquals(
			new String[0],
			categoryFacetPortletPreferencesImpl.getVocabularyIds());
	}

	private CategoryFacetPortletPreferencesImpl
		_createCategoryFacetPortletPreferencesImpl(
			AssetVocabularyLocalService assetVocabularyLocalService,
			GroupLocalService groupLocalService,
			String groupVocabularyExternalReferenceCodes) {

		PortletPreferences portletPreferences = Mockito.mock(
			PortletPreferences.class);

		Mockito.when(
			portletPreferences.getValue(
				CategoryFacetPortletPreferences.
					PREFERENCE_GROUP_VOCABULARY_EXTERNAL_REFERENCE_CODES,
				StringPool.BLANK)
		).thenReturn(
			groupVocabularyExternalReferenceCodes
		);

		return new CategoryFacetPortletPreferencesImpl(
			assetVocabularyLocalService, groupLocalService, portletPreferences);
	}

}