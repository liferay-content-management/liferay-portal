/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.marketing.platform.web.internal.upgrade.v2_2_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.layout.test.util.ContentLayoutTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.site.navigation.menu.item.layout.constants.SiteNavigationMenuItemTypeConstants;
import com.liferay.site.navigation.model.SiteNavigationMenuItem;
import com.liferay.site.navigation.service.SiteNavigationMenuItemLocalService;

import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jan Brychta
 */
@RunWith(Arquillian.class)
public class CMPPlanningAndTasksPagesUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	@TestInfo("LPD-112720")
	public void testUpgradeCompanyWithNewPlanningStructure() throws Exception {
		Company company = CompanyTestUtil.addCompany(false);

		_portalInstanceLifecycleListener.portalInstanceRegistered(company);

		Group group = _groupLocalService.fetchGroup(
			company.getCompanyId(), GroupConstants.CMS);

		Assert.assertNull(
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMPPRIMARYNAVIGATION1", group.getGroupId()));

		SiteNavigationMenuItem planningSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMSPRIMARYNAVIGATION11", group.getGroupId());

		SiteNavigationMenuItem projectsSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMPPRIMARYNAVIGATION2", group.getGroupId());

		Assert.assertEquals(
			planningSiteNavigationMenuItem.getSiteNavigationMenuItemId(),
			projectsSiteNavigationMenuItem.getParentSiteNavigationMenuItemId());

		_upgrade();

		Assert.assertNull(
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMPPRIMARYNAVIGATION1", group.getGroupId()));

		planningSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMSPRIMARYNAVIGATION11", group.getGroupId());

		projectsSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMPPRIMARYNAVIGATION2", group.getGroupId());

		Assert.assertEquals(
			planningSiteNavigationMenuItem.getSiteNavigationMenuItemId(),
			projectsSiteNavigationMenuItem.getParentSiteNavigationMenuItemId());
	}

	@Test
	@TestInfo("LPD-112720")
	public void testUpgradeCompanyWithOldPlanningStructure() throws Exception {
		Company company = CompanyTestUtil.addCompany(false);

		_portalInstanceLifecycleListener.portalInstanceRegistered(company);

		Group group = _groupLocalService.fetchGroup(
			company.getCompanyId(), GroupConstants.CMS);

		_revertToOldPlanningNavigation(group);

		Layout layout = _layoutLocalService.fetchLayoutByFriendlyURL(
			group.getGroupId(), false, "/tasks");

		FragmentEntryLink oldFragmentEntryLink = _addOldTasksFragment(layout);
		FragmentEntryLink oldDraftFragmentEntryLink = _addOldTasksFragment(
			layout.fetchDraftLayout());

		_upgrade();

		Assert.assertNull(
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMPPRIMARYNAVIGATION1", group.getGroupId()));
		Assert.assertNull(
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMPPRIMARYNAVIGATION3", group.getGroupId()));

		SiteNavigationMenuItem planningSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMSPRIMARYNAVIGATION11", group.getGroupId());

		Assert.assertNotNull(planningSiteNavigationMenuItem);

		SiteNavigationMenuItem tasksSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMSPRIMARYNAVIGATION12", group.getGroupId());

		Assert.assertEquals(
			planningSiteNavigationMenuItem.getSiteNavigationMenuItemId(),
			tasksSiteNavigationMenuItem.getParentSiteNavigationMenuItemId());
		Assert.assertEquals(0, tasksSiteNavigationMenuItem.getOrder());

		SiteNavigationMenuItem projectsSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMPPRIMARYNAVIGATION2", group.getGroupId());

		Assert.assertEquals(
			planningSiteNavigationMenuItem.getSiteNavigationMenuItemId(),
			projectsSiteNavigationMenuItem.getParentSiteNavigationMenuItemId());
		Assert.assertEquals(1, projectsSiteNavigationMenuItem.getOrder());

		_assertTasksFragmentReplaced(layout, oldFragmentEntryLink);
		_assertTasksFragmentReplaced(
			layout.fetchDraftLayout(), oldDraftFragmentEntryLink);
	}

	private FragmentEntryLink _addOldTasksFragment(Layout layout)
		throws Exception {

		return ContentLayoutTestUtil.addFragmentEntryLinkToLayout(
			StringPool.BLANK, layout,
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				layout.getPlid()));
	}

	private void _assertTasksFragmentReplaced(
		Layout layout, FragmentEntryLink oldFragmentEntryLink) {

		Assert.assertNull(
			_fragmentEntryLinkLocalService.fetchFragmentEntryLink(
				oldFragmentEntryLink.getFragmentEntryLinkId()));

		List<FragmentEntryLink> fragmentEntryLinks =
			_fragmentEntryLinkLocalService.getFragmentEntryLinksByPlid(
				layout.getGroupId(), layout.getPlid());

		Assert.assertEquals(
			fragmentEntryLinks.toString(), 1, fragmentEntryLinks.size());

		FragmentEntryLink fragmentEntryLink = fragmentEntryLinks.get(0);

		Assert.assertEquals(
			"com.liferay.site.cms.site.initializer.internal.fragment." +
				"renderer.ViewTasksJSPSectionFragmentRenderer",
			fragmentEntryLink.getRendererKey());
	}

	private void _revertToOldPlanningNavigation(Group group) throws Exception {
		SiteNavigationMenuItem planningSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMSPRIMARYNAVIGATION11", group.getGroupId());

		SiteNavigationMenuItem projectsSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMPPRIMARYNAVIGATION2", group.getGroupId());

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(group.getGroupId());

		SiteNavigationMenuItem oldPlanningSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.addSiteNavigationMenuItem(
				"CMPPRIMARYNAVIGATION1", serviceContext.getUserId(),
				group.getGroupId(),
				planningSiteNavigationMenuItem.getSiteNavigationMenuId(), 0,
				SiteNavigationMenuItemTypeConstants.NODE, StringPool.BLANK,
				serviceContext);

		_siteNavigationMenuItemLocalService.updateSiteNavigationMenuItem(
			serviceContext.getUserId(),
			projectsSiteNavigationMenuItem.getSiteNavigationMenuItemId(),
			group.getGroupId(),
			projectsSiteNavigationMenuItem.getSiteNavigationMenuId(),
			oldPlanningSiteNavigationMenuItem.getSiteNavigationMenuItemId(),
			projectsSiteNavigationMenuItem.getType(), 0,
			projectsSiteNavigationMenuItem.getTypeSettings());

		_siteNavigationMenuItemLocalService.addSiteNavigationMenuItem(
			"CMPPRIMARYNAVIGATION3", serviceContext.getUserId(),
			group.getGroupId(),
			oldPlanningSiteNavigationMenuItem.getSiteNavigationMenuId(),
			oldPlanningSiteNavigationMenuItem.getSiteNavigationMenuItemId(),
			SiteNavigationMenuItemTypeConstants.LAYOUT, StringPool.BLANK,
			serviceContext);

		_siteNavigationMenuItemLocalService.deleteSiteNavigationMenuItem(
			"CMSPRIMARYNAVIGATION12", group.getGroupId());

		_siteNavigationMenuItemLocalService.deleteSiteNavigationMenuItem(
			"CMSPRIMARYNAVIGATION11", group.getGroupId());
	}

	private void _upgrade() throws Exception {
		UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator,
			"com.liferay.content.marketing.platform.web.internal.upgrade." +
				"v2_2_0.CMPPlanningAndTasksPagesUpgradeProcess"
		).upgrade();
	}

	@Inject
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject(
		filter = "component.name=com.liferay.content.marketing.platform.web.internal.instance.lifecycle.AddDefaultLayoutInitialRequestPortalInstanceLifecycleListener"
	)
	private PortalInstanceLifecycleListener _portalInstanceLifecycleListener;

	@Inject
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	@Inject
	private SiteNavigationMenuItemLocalService
		_siteNavigationMenuItemLocalService;

	@Inject(
		filter = "component.name=com.liferay.content.marketing.platform.web.internal.upgrade.registry.CMPSiteInitializerUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}