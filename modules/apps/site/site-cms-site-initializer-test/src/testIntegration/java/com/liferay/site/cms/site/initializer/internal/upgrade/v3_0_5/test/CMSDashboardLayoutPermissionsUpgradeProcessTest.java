/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.upgrade.v3_0_5.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Veronica Gonzalez
 */
@RunWith(Arquillian.class)
public class CMSDashboardLayoutPermissionsUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		Group group = _groupLocalService.getGroup(
			TestPropsValues.getCompanyId(), GroupConstants.CMS);

		_layout = _layoutLocalService.getFriendlyURLLayout(
			group.getGroupId(), false, "/dashboard");

		_role = _roleLocalService.getRole(
			TestPropsValues.getCompanyId(),
			DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR);
	}

	@Test
	public void testUpgrade() throws Exception {
		_resourcePermissionLocalService.setResourcePermissions(
			TestPropsValues.getCompanyId(), Layout.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_layout.getPlid()), _role.getRoleId(),
			new String[0]);

		Assert.assertFalse(_hasViewResourcePermission());

		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator,
			"com.liferay.site.cms.site.initializer.internal.upgrade.v3_0_5." +
				"CMSDashboardLayoutPermissionsUpgradeProcess");

		upgradeProcess.upgrade();

		Assert.assertTrue(_hasViewResourcePermission());
	}

	private boolean _hasViewResourcePermission() throws Exception {
		return _resourcePermissionLocalService.hasResourcePermission(
			TestPropsValues.getCompanyId(), Layout.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_layout.getPlid()), _role.getRoleId(),
			ActionKeys.VIEW);
	}

	@Inject
	private GroupLocalService _groupLocalService;

	private Layout _layout;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	private Role _role;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject(
		filter = "component.name=com.liferay.site.cms.site.initializer.internal.upgrade.registry.SiteCMSSiteInitializerUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}