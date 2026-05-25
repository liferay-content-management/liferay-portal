/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.fragment.renderer;

import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.object.storage.sharepoint.service.TokenEntryLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.site.cms.site.initializer.internal.display.context.ViewSharepointEntriesSectionDisplayContext;
import com.liferay.site.cms.site.initializer.internal.util.InfoItemUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(service = FragmentRenderer.class)
public class ViewSharepointEntriesJSPSectionFragmentRenderer
	extends BaseJSPSectionFragmentRenderer
		<ViewSharepointEntriesSectionDisplayContext> {

	@Override
	public String getCollectionKey() {
		return "sections";
	}

	@Override
	public String getLabelKey() {
		return "sharepoint-entries";
	}

	@Override
	protected ViewSharepointEntriesSectionDisplayContext getDisplayContext(
		HttpServletRequest httpServletRequest) {

		return new ViewSharepointEntriesSectionDisplayContext(
			_depotEntryLocalService,
			InfoItemUtil.getGroupId(httpServletRequest), _groupLocalService,
			httpServletRequest, _tokenEntryLocalService);
	}

	@Override
	protected String getJSPPath() {
		return "/view_sharepoint_entries.jsp";
	}

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private TokenEntryLocalService _tokenEntryLocalService;

}