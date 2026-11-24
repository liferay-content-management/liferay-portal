/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.fragment.renderer;

import com.liferay.info.constants.InfoDisplayWebKeys;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.subscription.service.SubscriptionLocalService;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Jürgen Kappler
 */
public class EditFolderComponentSectionFragmentRendererTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		PortalUtil portalUtil = new PortalUtil();

		portalUtil.setPortal(_portal);

		ReflectionTestUtil.setFieldValue(
			_editFolderComponentSectionFragmentRenderer,
			"_subscriptionLocalService", _subscriptionLocalService);
	}

	@Test
	public void testGetProps() throws Exception {
		_testGetPropsWithoutObjectEntryFolder();
		_testGetPropsSubscribed();
	}

	private void _testGetPropsSubscribed() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		long objectEntryFolderId = RandomTestUtil.randomLong();
		long userId = RandomTestUtil.randomLong();

		ObjectEntryFolder objectEntryFolder = Mockito.mock(
			ObjectEntryFolder.class);

		Mockito.when(
			objectEntryFolder.getObjectEntryFolderId()
		).thenReturn(
			objectEntryFolderId
		);

		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Mockito.when(
			themeDisplay.getCompanyId()
		).thenReturn(
			companyId
		);

		Mockito.when(
			themeDisplay.getPathMain()
		).thenReturn(
			StringPool.BLANK
		);

		Mockito.when(
			themeDisplay.getPortalURL()
		).thenReturn(
			StringPool.BLANK
		);

		Mockito.when(
			themeDisplay.getUserId()
		).thenReturn(
			userId
		);

		Mockito.when(
			_subscriptionLocalService.isSubscribed(
				companyId, userId, ObjectEntryFolder.class.getName(),
				objectEntryFolderId)
		).thenReturn(
			true
		);

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			InfoDisplayWebKeys.INFO_ITEM, objectEntryFolder);
		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		Map<String, Object> props =
			_editFolderComponentSectionFragmentRenderer.getProps(
				null, mockHttpServletRequest);

		Assert.assertEquals(Boolean.TRUE, props.get("isSubscribed"));
		Assert.assertEquals(
			GroupConstants.CMS_FRIENDLY_URL + "/subscribe_folder?classPK=" +
				objectEntryFolderId,
			props.get("subscribeURL"));
	}

	private void _testGetPropsWithoutObjectEntryFolder() throws Exception {
		String redirect = "javascript:alert(1)";

		String escapedRedirect = RandomTestUtil.randomString();

		Mockito.when(
			_portal.escapeRedirect(redirect)
		).thenReturn(
			escapedRedirect
		);

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setParameter("redirect", redirect);

		Map<String, Object> props =
			_editFolderComponentSectionFragmentRenderer.getProps(
				null, mockHttpServletRequest);

		Assert.assertEquals(escapedRedirect, props.get("backURL"));
		Assert.assertEquals(Boolean.FALSE, props.get("isSubscribed"));
		Assert.assertNull(props.get("subscribeURL"));
	}

	private final EditFolderComponentSectionFragmentRenderer
		_editFolderComponentSectionFragmentRenderer =
			new EditFolderComponentSectionFragmentRenderer();

	@Mock
	private Portal _portal;

	@Mock
	private SubscriptionLocalService _subscriptionLocalService;

}