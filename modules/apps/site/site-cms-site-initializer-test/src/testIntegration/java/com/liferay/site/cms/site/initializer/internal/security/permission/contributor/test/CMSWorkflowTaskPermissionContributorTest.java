/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.security.permission.contributor.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserGroupRoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;

import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class CMSWorkflowTaskPermissionContributorTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testContains() throws Exception {
		DepotEntry depotEntry1 = _addSpaceDepotEntry();
		DepotEntry depotEntry2 = _addSpaceDepotEntry();

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_CMS_BASIC_WEB_CONTENT", TestPropsValues.getCompanyId());

		ObjectEntry objectEntry1 = _addObjectEntry(
			depotEntry1, objectDefinition);
		ObjectEntry objectEntry2 = _addObjectEntry(
			depotEntry2, objectDefinition);

		KaleoTaskInstanceToken kaleoTaskInstanceToken1 =
			_assignKaleoTaskInstanceToken(objectDefinition, objectEntry1);
		KaleoTaskInstanceToken kaleoTaskInstanceToken2 =
			_assignKaleoTaskInstanceToken(objectDefinition, objectEntry2);

		User user1 = UserTestUtil.addUser();

		_addUserGroupRole(
			depotEntry1, DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR,
			user1);

		User user2 = UserTestUtil.addUser();

		Role role = _roleLocalService.getRole(
			TestPropsValues.getCompanyId(), RoleConstants.CMS_ADMINISTRATOR);

		_userLocalService.addRoleUser(role.getRoleId(), user2.getUserId());

		User user3 = UserTestUtil.addUser();

		_userLocalService.addGroupUser(
			depotEntry1.getGroupId(), user3.getUserId());

		PermissionChecker permissionChecker2 = _permissionCheckerFactory.create(
			user2);

		Assert.assertTrue(
			_kaleoTaskInstanceTokenModelResourcePermission.contains(
				permissionChecker2, kaleoTaskInstanceToken1, ActionKeys.VIEW));
		Assert.assertTrue(
			_kaleoTaskInstanceTokenModelResourcePermission.contains(
				permissionChecker2, kaleoTaskInstanceToken2, ActionKeys.VIEW));

		PermissionChecker permissionChecker1 = _permissionCheckerFactory.create(
			user1);

		Assert.assertTrue(
			_kaleoTaskInstanceTokenModelResourcePermission.contains(
				permissionChecker1, kaleoTaskInstanceToken1, ActionKeys.VIEW));
		Assert.assertFalse(
			_kaleoTaskInstanceTokenModelResourcePermission.contains(
				permissionChecker1, kaleoTaskInstanceToken2, ActionKeys.VIEW));

		Assert.assertFalse(
			_kaleoTaskInstanceTokenModelResourcePermission.contains(
				_permissionCheckerFactory.create(user3),
				kaleoTaskInstanceToken1, ActionKeys.VIEW));

		PermissionThreadLocal.setPermissionChecker(permissionChecker1);

		PrincipalException.MustHavePermission mustHavePermission =
			Assert.assertThrows(
				PrincipalException.MustHavePermission.class,
				() -> _workflowTaskManager.completeWorkflowTask(
					TestPropsValues.getCompanyId(), user1.getUserId(),
					kaleoTaskInstanceToken1.getKaleoTaskInstanceTokenId(),
					"approve", RandomTestUtil.randomString(), new HashMap<>()));

		Assert.assertArrayEquals(
			new String[] {ActionKeys.VIEW}, mustHavePermission.actionId);
		Assert.assertEquals(
			kaleoTaskInstanceToken1.getKaleoTaskInstanceTokenId(),
			mustHavePermission.resourceId);
		Assert.assertEquals(
			WorkflowTask.class.getName(), mustHavePermission.resourceName);
		Assert.assertEquals(user1.getUserId(), mustHavePermission.userId);

		_workflowTaskManager.assignWorkflowTaskToUser(
			TestPropsValues.getCompanyId(), user1.getUserId(),
			kaleoTaskInstanceToken1.getKaleoTaskInstanceTokenId(),
			user1.getUserId(), RandomTestUtil.randomString(), null,
			new HashMap<>());
		_workflowTaskManager.completeWorkflowTask(
			TestPropsValues.getCompanyId(), user1.getUserId(),
			kaleoTaskInstanceToken1.getKaleoTaskInstanceTokenId(), "approve",
			RandomTestUtil.randomString(), new HashMap<>());

		objectEntry1 = _objectEntryLocalService.getObjectEntry(
			objectEntry1.getObjectEntryId());

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, objectEntry1.getStatus());
	}

	private ObjectEntry _addObjectEntry(
			DepotEntry depotEntry, ObjectDefinition objectDefinition)
		throws Exception {

		_workflowDefinitionLinkLocalService.updateWorkflowDefinitionLink(
			TestPropsValues.getUserId(), TestPropsValues.getCompanyId(),
			depotEntry.getGroupId(), objectDefinition.getClassName(), 0, 0,
			"Single Approver", 1);

		ObjectEntryFolder objectEntryFolder =
			_objectEntryFolderLocalService.
				getObjectEntryFolderByExternalReferenceCode(
					"L_CONTENTS", depotEntry.getGroupId(),
					depotEntry.getCompanyId());

		return _objectEntryLocalService.addObjectEntry(
			depotEntry.getGroupId(), TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			objectEntryFolder.getObjectEntryFolderId(), "en_US",
			HashMapBuilder.<String, Serializable>put(
				"content_i18n",
				HashMapBuilder.put(
					"en_US", RandomTestUtil.randomString()
				).build()
			).put(
				"title_i18n",
				HashMapBuilder.put(
					"en_US", RandomTestUtil.randomString()
				).build()
			).build(),
			ServiceContextTestUtil.getServiceContext(depotEntry.getGroupId()));
	}

	private DepotEntry _addSpaceDepotEntry() throws Exception {
		Group group = _groupLocalService.getGroup(
			TestPropsValues.getCompanyId(), GroupConstants.CMS);

		return _depotEntryLocalService.addDepotEntry(
			Collections.singletonMap(
				LocaleUtil.getDefault(), RandomTestUtil.randomString()),
			null, DepotConstants.TYPE_SPACE,
			ServiceContextTestUtil.getServiceContext(group.getGroupId()));
	}

	private void _addUserGroupRole(
			DepotEntry depotEntry, String roleName, User user)
		throws Exception {

		_userLocalService.addGroupUser(
			depotEntry.getGroupId(), user.getUserId());

		Role role = _roleLocalService.getRole(
			TestPropsValues.getCompanyId(), roleName);

		_userGroupRoleLocalService.addUserGroupRoles(
			user.getUserId(), depotEntry.getGroupId(),
			new long[] {role.getRoleId()});
	}

	private KaleoTaskInstanceToken _assignKaleoTaskInstanceToken(
			ObjectDefinition objectDefinition, ObjectEntry objectEntry)
		throws Exception {

		List<KaleoTaskInstanceToken> kaleoTaskInstanceTokens =
			_kaleoTaskInstanceTokenLocalService.getKaleoTaskInstanceTokens(
				objectDefinition.getClassName(),
				objectEntry.getObjectEntryId());

		KaleoTaskInstanceToken kaleoTaskInstanceToken =
			kaleoTaskInstanceTokens.get(0);

		_workflowTaskManager.assignWorkflowTaskToUser(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId(),
			TestPropsValues.getUserId(), RandomTestUtil.randomString(), null,
			new HashMap<>());

		return _kaleoTaskInstanceTokenLocalService.getKaleoTaskInstanceToken(
			kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());
	}

	@Inject
	private DepotEntryLocalService _depotEntryLocalService;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private KaleoTaskInstanceTokenLocalService
		_kaleoTaskInstanceTokenLocalService;

	@Inject(
		filter = "model.class.name=com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken"
	)
	private ModelResourcePermission<KaleoTaskInstanceToken>
		_kaleoTaskInstanceTokenModelResourcePermission;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject
	private UserGroupRoleLocalService _userGroupRoleLocalService;

	@Inject
	private UserLocalService _userLocalService;

	@Inject
	private WorkflowDefinitionLinkLocalService
		_workflowDefinitionLinkLocalService;

	@Inject
	private WorkflowTaskManager _workflowTaskManager;

}