/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.marketing.platform.web.internal.upgrade.v2_2_0;

import com.liferay.fragment.constants.FragmentConstants;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.util.UpgradeProcessUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.site.navigation.menu.item.layout.constants.SiteNavigationMenuItemTypeConstants;
import com.liferay.site.navigation.model.SiteNavigationMenuItem;
import com.liferay.site.navigation.service.SiteNavigationMenuItemLocalService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Jan Brychta
 */
public class CMPPlanningAndTasksPagesUpgradeProcess extends UpgradeProcess {

	public CMPPlanningAndTasksPagesUpgradeProcess(
		CompanyLocalService companyLocalService,
		FragmentEntryLinkLocalService fragmentEntryLinkLocalService,
		GroupLocalService groupLocalService,
		LayoutLocalService layoutLocalService,
		LayoutPageTemplateStructureLocalService
			layoutPageTemplateStructureLocalService,
		SegmentsExperienceLocalService segmentsExperienceLocalService,
		SiteNavigationMenuItemLocalService siteNavigationMenuItemLocalService,
		UserLocalService userLocalService) {

		_companyLocalService = companyLocalService;
		_fragmentEntryLinkLocalService = fragmentEntryLinkLocalService;
		_groupLocalService = groupLocalService;
		_layoutLocalService = layoutLocalService;
		_layoutPageTemplateStructureLocalService =
			layoutPageTemplateStructureLocalService;
		_segmentsExperienceLocalService = segmentsExperienceLocalService;
		_siteNavigationMenuItemLocalService =
			siteNavigationMenuItemLocalService;
		_userLocalService = userLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompanyId(this::_upgradeCompany);
	}

	private String _getLayoutTypeSettings(Layout layout) {
		return UnicodePropertiesBuilder.put(
			"displayIcon", "paste"
		).put(
			"externalReferenceCode", layout.getExternalReferenceCode()
		).put(
			"privateLayout", String.valueOf(layout.isPrivateLayout())
		).buildString();
	}

	private String _getNodeTypeSettings(long companyId) throws Exception {
		Map<Locale, String> nameMap = new HashMap<>();

		for (Locale locale : LanguageUtil.getAvailableLocales()) {
			nameMap.put(locale, LanguageUtil.get(locale, "planning"));
		}

		UnicodePropertiesBuilder.UnicodePropertiesWrapper
			unicodePropertiesWrapper = UnicodePropertiesBuilder.put(
				"defaultLanguageId",
				UpgradeProcessUtil.getDefaultLanguageId(companyId));

		for (Map.Entry<Locale, String> entry : nameMap.entrySet()) {
			unicodePropertiesWrapper.put(
				"name_" + LocaleUtil.toLanguageId(entry.getKey()),
				entry.getValue());
		}

		return unicodePropertiesWrapper.put(
			"title", StringPool.BLANK
		).buildString();
	}

	private void _mergePlanningNavigation(
			Group group,
			SiteNavigationMenuItem oldPlanningSiteNavigationMenuItem,
			ServiceContext serviceContext)
		throws Exception {

		SiteNavigationMenuItem planningSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				addOrUpdateSiteNavigationMenuItem(
					"CMSPRIMARYNAVIGATION11", serviceContext.getUserId(),
					group.getGroupId(),
					oldPlanningSiteNavigationMenuItem.getSiteNavigationMenuId(),
					0, SiteNavigationMenuItemTypeConstants.NODE,
					_getNodeTypeSettings(serviceContext.getCompanyId()),
					serviceContext);

		Layout tasksLayout = _layoutLocalService.fetchLayoutByFriendlyURL(
			group.getGroupId(), false, "/tasks");

		if (tasksLayout != null) {
			_siteNavigationMenuItemLocalService.
				addOrUpdateSiteNavigationMenuItem(
					"CMSPRIMARYNAVIGATION12", serviceContext.getUserId(),
					group.getGroupId(),
					planningSiteNavigationMenuItem.getSiteNavigationMenuId(),
					planningSiteNavigationMenuItem.
						getSiteNavigationMenuItemId(),
					SiteNavigationMenuItemTypeConstants.LAYOUT,
					_getLayoutTypeSettings(tasksLayout), serviceContext);
		}

		SiteNavigationMenuItem projectsSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMPPRIMARYNAVIGATION2", group.getGroupId());

		if (projectsSiteNavigationMenuItem != null) {
			List<SiteNavigationMenuItem> planningChildSiteNavigationMenuItems =
				_siteNavigationMenuItemLocalService.getSiteNavigationMenuItems(
					planningSiteNavigationMenuItem.getSiteNavigationMenuId(),
					planningSiteNavigationMenuItem.
						getSiteNavigationMenuItemId());

			_siteNavigationMenuItemLocalService.updateSiteNavigationMenuItem(
				projectsSiteNavigationMenuItem.getSiteNavigationMenuItemId(),
				planningSiteNavigationMenuItem.getSiteNavigationMenuItemId(),
				planningChildSiteNavigationMenuItems.size());
		}

		_siteNavigationMenuItemLocalService.deleteSiteNavigationMenuItem(
			"CMPPRIMARYNAVIGATION3", group.getGroupId());

		_siteNavigationMenuItemLocalService.deleteSiteNavigationMenuItem(
			"CMPPRIMARYNAVIGATION1", group.getGroupId());
	}

	private void _replaceTasksFragmentEntryLink(Layout layout)
		throws Exception {

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			_layoutPageTemplateStructureLocalService.
				fetchLayoutPageTemplateStructure(
					layout.getGroupId(), layout.getPlid());

		if (layoutPageTemplateStructure == null) {
			return;
		}

		long segmentsExperienceId =
			_segmentsExperienceLocalService.fetchDefaultSegmentsExperienceId(
				layout.getPlid());

		LayoutStructure layoutStructure = LayoutStructure.of(
			layoutPageTemplateStructure.getData(segmentsExperienceId));

		LayoutStructureItem mainLayoutStructureItem =
			layoutStructure.getMainLayoutStructureItem();

		for (String childItemId :
				new ArrayList<>(mainLayoutStructureItem.getChildrenItemIds())) {

			layoutStructure.deleteLayoutStructureItem(childItemId);
		}

		_fragmentEntryLinkLocalService.deleteFragmentEntryLinks(
			layout.getGroupId(), layout.getPlid(), false);

		FragmentEntryLink fragmentEntryLink =
			_fragmentEntryLinkLocalService.addFragmentEntryLink(
				null, layout.getUserId(), layout.getGroupId(), null, null, null,
				segmentsExperienceId, layout.getPlid(), StringPool.BLANK,
				StringPool.BLANK, StringPool.BLANK, StringPool.BLANK,
				JSONFactoryUtil.getNullJSON(), StringPool.BLANK, 0,
				_VIEW_TASKS_FRAGMENT_RENDERER_KEY,
				FragmentConstants.TYPE_COMPONENT, new ServiceContext());

		layoutStructure.addFragmentStyledLayoutStructureItem(
			fragmentEntryLink.getFragmentEntryLinkId(),
			layoutStructure.getMainItemId(), 0);

		_layoutPageTemplateStructureLocalService.
			updateLayoutPageTemplateStructureData(
				layout.getUserId(), layout.getGroupId(), layout.getPlid(),
				segmentsExperienceId, layoutStructure.toString());
	}

	private void _replaceTasksFragments(Group group) throws Exception {
		Layout layout = _layoutLocalService.fetchLayoutByFriendlyURL(
			group.getGroupId(), false, "/tasks");

		if (layout == null) {
			return;
		}

		_replaceTasksFragmentEntryLink(layout);

		Layout draftLayout = layout.fetchDraftLayout();

		if (draftLayout != null) {
			_replaceTasksFragmentEntryLink(draftLayout);
		}
	}

	private void _upgradeCompany(long companyId) throws Exception {
		Group group = _groupLocalService.fetchGroup(
			companyId, GroupConstants.CMS);

		if (group == null) {
			return;
		}

		SiteNavigationMenuItem oldPlanningSiteNavigationMenuItem =
			_siteNavigationMenuItemLocalService.
				fetchSiteNavigationMenuItemByExternalReferenceCode(
					"CMPPRIMARYNAVIGATION1", group.getGroupId());

		if (oldPlanningSiteNavigationMenuItem == null) {
			return;
		}

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(companyId);
		serviceContext.setScopeGroupId(group.getGroupId());
		serviceContext.setUserId(_userLocalService.getGuestUserId(companyId));

		ServiceContextThreadLocal.pushServiceContext(serviceContext);

		try {
			_mergePlanningNavigation(
				group, oldPlanningSiteNavigationMenuItem, serviceContext);

			_replaceTasksFragments(group);
		}
		finally {
			ServiceContextThreadLocal.popServiceContext();
		}
	}

	private static final String _VIEW_TASKS_FRAGMENT_RENDERER_KEY =
		"com.liferay.site.cms.site.initializer.internal.fragment.renderer." +
			"ViewTasksJSPSectionFragmentRenderer";

	private final CompanyLocalService _companyLocalService;
	private final FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;
	private final GroupLocalService _groupLocalService;
	private final LayoutLocalService _layoutLocalService;
	private final LayoutPageTemplateStructureLocalService
		_layoutPageTemplateStructureLocalService;
	private final SegmentsExperienceLocalService
		_segmentsExperienceLocalService;
	private final SiteNavigationMenuItemLocalService
		_siteNavigationMenuItemLocalService;
	private final UserLocalService _userLocalService;

}