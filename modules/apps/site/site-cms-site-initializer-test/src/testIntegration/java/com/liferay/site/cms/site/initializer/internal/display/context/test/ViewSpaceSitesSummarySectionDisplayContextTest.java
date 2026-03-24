/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.info.constants.InfoDisplayWebKeys;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserGroupRoleLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Crescenzo Rega
 */
@FeatureFlag("LPD-17564")
@RunWith(Arquillian.class)
public class ViewSpaceSitesSummarySectionDisplayContextTest
	extends BaseDisplayContextTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		super.setUp();

		_depotEntry = _depotEntryLocalService.addDepotEntry(
			HashMapBuilder.put(
				LocaleUtil.getDefault(), StringUtil.randomString()
			).build(),
			HashMapBuilder.put(
				LocaleUtil.getDefault(), StringUtil.randomString()
			).build(),
			DepotConstants.TYPE_SPACE,
			ServiceContextTestUtil.getServiceContext(
				group.getGroupId(), TestPropsValues.getUserId()));

		_role = _roleLocalService.getRole(
			_depotEntry.getCompanyId(),
			DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR);
	}

	@Test
	public void testGetCreationMenu() throws Exception {
		User user1 = UserTestUtil.addUser();

		_userGroupRoleLocalService.addUserGroupRoles(
			user1.getUserId(), _depotEntry.getGroupId(),
			new long[] {_role.getRoleId()});

		_groupLocalService.addUserGroup(
			user1.getUserId(), _depotEntry.getGroup());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user1, PermissionCheckerFactoryUtil.create(user1))) {

			CreationMenu creationMenu = _getCreationMenu(
				_getMockHttpServletRequest(user1));

			List<DropdownItem> dropdownItems =
				(List<DropdownItem>)creationMenu.get("primaryItems");

			Assert.assertEquals(
				dropdownItems.toString(), 1, dropdownItems.size());

			DropdownItem dropdownItem = dropdownItems.get(0);

			Assert.assertEquals(
				language.get(LocaleUtil.getDefault(), "Connect Sites"),
				dropdownItem.get("label"));

			Map<String, Object> data = (Map<String, Object>)dropdownItem.get(
				"data");

			Group group = _groupLocalService.getGroup(_depotEntry.getGroupId());

			Assert.assertEquals("connectSites", data.get("action"));
			Assert.assertEquals(group.getGroupId(), data.get("groupId"));
			Assert.assertEquals(
				group.getExternalReferenceCode(),
				data.get("externalReferenceCode"));

			Assert.assertNotNull(creationMenu);
		}

		User user2 = UserTestUtil.addUser(group.getGroupId());

		_groupLocalService.addUserGroup(
			user2.getUserId(), _depotEntry.getGroup());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user2, PermissionCheckerFactoryUtil.create(user2))) {

			Assert.assertNull(
				_getCreationMenu(_getMockHttpServletRequest(user2)));
		}
	}

	@Test
	public void testGetFDSActionDropdownItems() throws Exception {
		User user1 = UserTestUtil.addUser();

		_userGroupRoleLocalService.addUserGroupRoles(
			user1.getUserId(), _depotEntry.getGroupId(),
			new long[] {_role.getRoleId()});

		_groupLocalService.addUserGroup(
			user1.getUserId(), _depotEntry.getGroup());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user1, PermissionCheckerFactoryUtil.create(user1))) {

			List<FDSActionDropdownItem> fdsActionDropdownItems =
				_getFDSActionDropdownItems(_getMockHttpServletRequest(user1));

			Assert.assertEquals(
				fdsActionDropdownItems.toString(), 3,
				fdsActionDropdownItems.size());

			_assertFDSActionDropdownItem(
				fdsActionDropdownItems.get(0), "make-searchable",
				"Make Searchable", "put", "item");

			_assertFDSActionDropdownItem(
				fdsActionDropdownItems.get(1), "make-unsearchable",
				"Make Unsearchable", "put", "item");

			_assertFDSActionDropdownItem(
				fdsActionDropdownItems.get(2), "delete", "Disconnect", "delete",
				"item");

			Assert.assertNotNull(fdsActionDropdownItems);
		}

		User user2 = UserTestUtil.addUser(group.getGroupId());

		_groupLocalService.addUserGroup(
			user2.getUserId(), _depotEntry.getGroup());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user2, PermissionCheckerFactoryUtil.create(user2))) {

			List<FDSActionDropdownItem> fdsActionDropdownItems =
				_getFDSActionDropdownItems(_getMockHttpServletRequest(user2));

			Assert.assertTrue(fdsActionDropdownItems.isEmpty());
		}
	}

	private void _assertFDSActionDropdownItem(
		FDSActionDropdownItem fdsActionDropdownItem, String id, String label,
		String method, String type) {

		Assert.assertNotNull(fdsActionDropdownItem);

		Map<String, Object> data =
			(Map<String, Object>)fdsActionDropdownItem.get("data");

		Assert.assertEquals(id, data.get("id"));
		Assert.assertEquals(method, data.get("method"));

		Assert.assertEquals(
			language.get(LocaleUtil.getDefault(), label),
			fdsActionDropdownItem.get("label"));
		Assert.assertEquals(type, fdsActionDropdownItem.get("type"));
	}

	private CreationMenu _getCreationMenu(HttpServletRequest httpServletRequest)
		throws Exception {

		return ReflectionTestUtil.invoke(
			_getSectionDisplayContext(httpServletRequest), "getCreationMenu",
			new Class<?>[0]);
	}

	private List<FDSActionDropdownItem> _getFDSActionDropdownItems(
			HttpServletRequest httpServletRequest)
		throws Exception {

		return ReflectionTestUtil.invoke(
			_getSectionDisplayContext(httpServletRequest),
			"getFDSActionDropdownItems", new Class<?>[0]);
	}

	private HttpServletRequest _getMockHttpServletRequest(User user)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			InfoDisplayWebKeys.INFO_ITEM, _depotEntry);

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY,
			_getThemeDisplay(mockHttpServletRequest, user));

		return mockHttpServletRequest;
	}

	private Object _getSectionDisplayContext(
			HttpServletRequest httpServletRequest)
		throws Exception {

		_fragmentRenderer.render(
			null, httpServletRequest, new MockHttpServletResponse());

		Object viewSpaceSitesSummarySectionDisplayContext =
			httpServletRequest.getAttribute(
				"com.liferay.site.cms.site.initializer.internal.display." +
					"context.ViewSpaceSitesSummarySectionDisplayContext");

		Assert.assertNotNull(viewSpaceSitesSummarySectionDisplayContext);

		return viewSpaceSitesSummarySectionDisplayContext;
	}

	private ThemeDisplay _getThemeDisplay(
			HttpServletRequest httpServletRequest, User user)
		throws Exception {

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			companyLocalService.getCompany(group.getCompanyId()));
		themeDisplay.setLanguageId(group.getDefaultLanguageId());

		Layout layout = LayoutTestUtil.addTypeContentLayout(group, "test");

		themeDisplay.setLayout(layout);
		themeDisplay.setLayoutSet(layout.getLayoutSet());

		themeDisplay.setLocale(LocaleUtil.getDefault());
		themeDisplay.setPathMain(portal.getPathMain());
		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());
		themeDisplay.setPortalURL("http://localhost:8080");
		themeDisplay.setRealUser(user);
		themeDisplay.setRequest(httpServletRequest);
		themeDisplay.setScopeGroupId(group.getGroupId());
		themeDisplay.setSiteGroupId(group.getGroupId());
		themeDisplay.setURLCurrent("http://localhost:8080/currentURL");
		themeDisplay.setUser(user);

		return themeDisplay;
	}

	private DepotEntry _depotEntry;

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject(
		filter = "component.name=com.liferay.site.cms.site.initializer.internal.fragment.renderer.ViewSpaceSitesSummaryJSPSectionFragmentRenderer"
	)
	private FragmentRenderer _fragmentRenderer;

	@Inject
	private GroupLocalService _groupLocalService;

	private Role _role;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject
	private UserGroupRoleLocalService _userGroupRoleLocalService;

}