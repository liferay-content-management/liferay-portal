/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.knowledge.base.trash.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.knowledge.base.model.KBFolder;
import com.liferay.knowledge.base.service.KBFolderLocalService;
import com.liferay.knowledge.base.test.util.KBTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderRequest;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.trash.constants.TrashPortletKeys;
import com.liferay.trash.model.TrashEntry;
import com.liferay.trash.service.TrashEntryLocalService;

import jakarta.portlet.Portlet;
import jakarta.portlet.RenderRequest;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian Wulbern
 */
@RunWith(Arquillian.class)
public class KBFolderTrashPortletTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	@TestInfo("LPD-107894")
	public void testCheckPermissions() throws Exception {
		Group group1 = GroupTestUtil.addGroup();

		ReflectionTestUtil.invoke(
			_portlet, "_checkPermissions", new Class<?>[] {RenderRequest.class},
			_getMockLiferayPortletRenderRequest(
				group1.getGroupId(), _addKBFolderTrashEntry(group1),
				TestPropsValues.getUser()));

		_groupLocalService.deleteGroup(group1);
	}

	@Test
	@TestInfo("LPD-107894")
	public void testCheckPermissionsWithAnotherScopeGroup() throws Exception {
		Group group1 = GroupTestUtil.addGroup();

		TrashEntry trashEntry = _addKBFolderTrashEntry(group1);

		Group group2 = GroupTestUtil.addGroup();

		_assertMustHavePermission(
			trashEntry,
			_getMockLiferayPortletRenderRequest(
				group2.getGroupId(), trashEntry, TestPropsValues.getUser()));

		_groupLocalService.deleteGroup(group1);
		_groupLocalService.deleteGroup(group2);
	}

	@Test
	@TestInfo("LPD-107894")
	public void testCheckPermissionsWithoutViewPermission() throws Exception {
		Group group1 = GroupTestUtil.addGroup();

		TrashEntry trashEntry = _addKBFolderTrashEntry(group1);

		for (String roleName :
				new String[] {RoleConstants.GUEST, RoleConstants.SITE_MEMBER}) {

			RoleTestUtil.removeResourcePermission(
				roleName, KBFolder.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(trashEntry.getClassPK()), ActionKeys.VIEW);
		}

		User user = UserTestUtil.addGroupUser(
			group1, RoleConstants.SITE_MEMBER);

		_assertMustHavePermission(
			trashEntry,
			_getMockLiferayPortletRenderRequest(
				group1.getGroupId(), trashEntry, user));

		_userLocalService.deleteUser(user);

		_groupLocalService.deleteGroup(group1);
	}

	private TrashEntry _addKBFolderTrashEntry(Group group) throws Exception {
		KBFolder kbFolder = KBTestUtil.addKBFolder(group.getGroupId());

		_kbFolderLocalService.moveKBFolderToTrash(
			TestPropsValues.getUserId(), kbFolder.getKbFolderId());

		return _trashEntryLocalService.getEntry(
			KBFolder.class.getName(), kbFolder.getKbFolderId());
	}

	private void _assertMustHavePermission(
		TrashEntry trashEntry,
		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest) {

		PrincipalException.MustHavePermission mustHavePermission =
			Assert.assertThrows(
				PrincipalException.MustHavePermission.class,
				() -> ReflectionTestUtil.invoke(
					_portlet, "_checkPermissions",
					new Class<?>[] {RenderRequest.class},
					mockLiferayPortletRenderRequest));

		Assert.assertArrayEquals(
			new String[] {ActionKeys.VIEW}, mustHavePermission.actionId);
		Assert.assertEquals(
			trashEntry.getClassPK(), mustHavePermission.resourceId);
		Assert.assertEquals(
			KBFolder.class.getName(), mustHavePermission.resourceName);
	}

	private MockLiferayPortletRenderRequest _getMockLiferayPortletRenderRequest(
			long scopeGroupId, TrashEntry trashEntry, User user)
		throws Exception {

		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			new MockLiferayPortletRenderRequest();

		mockLiferayPortletRenderRequest.setParameter(
			"trashEntryId", String.valueOf(trashEntry.getEntryId()));

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(user));
		themeDisplay.setScopeGroupId(scopeGroupId);
		themeDisplay.setUser(user);

		mockLiferayPortletRenderRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		return mockLiferayPortletRenderRequest;
	}

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private KBFolderLocalService _kbFolderLocalService;

	@Inject(filter = "jakarta.portlet.name=" + TrashPortletKeys.TRASH)
	private Portlet _portlet;

	@Inject
	private TrashEntryLocalService _trashEntryLocalService;

	@Inject
	private UserLocalService _userLocalService;

}