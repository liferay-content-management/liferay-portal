/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.announcements.web.internal.display.context.test;

import com.liferay.announcements.kernel.exception.EntryDisplayDateException;
import com.liferay.announcements.kernel.exception.EntryExpirationDateException;
import com.liferay.announcements.kernel.model.AnnouncementsEntry;
import com.liferay.announcements.kernel.service.AnnouncementsEntryLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.util.BundleUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderResponse;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletURL;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.portlet.RenderRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Akhash R
 */
@RunWith(Arquillian.class)
public class AnnouncementsAdminViewDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(
			AnnouncementsAdminViewDisplayContextTest.class);

		bundle = BundleUtil.getBundle(
			bundle.getBundleContext(), "com.liferay.announcements.web");

		Class<?> clazz = bundle.loadClass(
			"com.liferay.announcements.web.internal.display.context." +
				"AnnouncementsAdminViewDisplayContext");

		_constructor = clazz.getConstructor(
			HttpServletRequest.class, LiferayPortletRequest.class,
			LiferayPortletResponse.class, RenderRequest.class);
	}

	@Before
	public void setUp() throws Exception {
		_user = UserTestUtil.getAdminUser(TestPropsValues.getCompanyId());
	}

	@Test
	public void testGetSearchContainer() throws Exception {
		int count =
			_announcementsEntryLocalService.getAnnouncementsEntriesCount();

		_addEntry("Beta");
		_addEntry("Alpha");
		_addEntry("Charlie");

		ThemeDisplay themeDisplay = _getThemeDisplay();

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);
		mockHttpServletRequest.setParameter("orderByCol", "title");
		mockHttpServletRequest.setParameter("orderByType", "asc");

		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			new MockLiferayPortletRenderRequest(mockHttpServletRequest);

		mockLiferayPortletRenderRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);
		mockLiferayPortletRenderRequest.setAttribute(
			StringBundler.concat(
				mockLiferayPortletRenderRequest.getPortletName(), "-",
				WebKeys.CURRENT_PORTLET_URL),
			new MockLiferayPortletURL());
		mockLiferayPortletRenderRequest.setParameter("orderByCol", "title");
		mockLiferayPortletRenderRequest.setParameter("orderByType", "asc");

		Object announcementsAdminViewDisplayContext = _constructor.newInstance(
			mockHttpServletRequest, mockLiferayPortletRenderRequest,
			new MockLiferayPortletRenderResponse(),
			mockLiferayPortletRenderRequest);

		SearchContainer<AnnouncementsEntry> searchContainer =
			ReflectionTestUtil.invoke(
				announcementsAdminViewDisplayContext, "getSearchContainer",
				new Class<?>[0]);

		List<AnnouncementsEntry> results = searchContainer.getResults();

		Assert.assertEquals(results.toString(), count + 3, results.size());

		Assert.assertEquals(
			"Alpha",
			results.get(
				0
			).getTitle());
		Assert.assertEquals(
			"Beta",
			results.get(
				1
			).getTitle());
		Assert.assertEquals(
			"Charlie",
			results.get(
				2
			).getTitle());
	}

	private void _addEntry(String title) throws Exception {
		AnnouncementsEntry entry = _announcementsEntryLocalService.addEntry(
			_user.getUserId(), 0, 0, title, "content", "http://localhost",
			"general",
			_portal.getDate(
				1, 1, 1990, 1, 1, _user.getTimeZone(),
				EntryDisplayDateException.class),
			_portal.getDate(
				1, 1, 3000, 1, 1, _user.getTimeZone(),
				EntryExpirationDateException.class),
			1, false);

		_announcementsEntries.add(entry);
	}

	private ThemeDisplay _getThemeDisplay() throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(_user.getCompanyId()));
		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());
		themeDisplay.setUser(TestPropsValues.getUser());

		return themeDisplay;
	}

	private static Constructor<?> _constructor;

	@DeleteAfterTestRun
	private final List<AnnouncementsEntry> _announcementsEntries =
		new ArrayList<>();

	@Inject
	private AnnouncementsEntryLocalService _announcementsEntryLocalService;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private Portal _portal;

	private User _user;

}