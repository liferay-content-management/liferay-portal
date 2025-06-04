/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectDefinitionSettingLocalService;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.Portal;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author Jürgen Kappler
 */
public class ViewAllSectionDisplayContext extends BaseSectionDisplayContext {

	public ViewAllSectionDisplayContext(
		DepotEntryLocalService depotEntryLocalService,
		GroupLocalService groupLocalService,
		HttpServletRequest httpServletRequest, Language language,
		ObjectDefinitionService objectDefinitionService,
		ObjectDefinitionSettingLocalService objectDefinitionSettingLocalService,
		Portal portal) {

		super(
			depotEntryLocalService, groupLocalService, httpServletRequest,
			language, objectDefinitionService,
			objectDefinitionSettingLocalService, portal);
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems() {
		List<FDSActionDropdownItem> fdsActionDropdownItems =
			super.getFDSActionDropdownItems();

		fdsActionDropdownItems.add(
			1,
			new FDSActionDropdownItem(
				"{embedded.file.link.href}", "download", "download",
				LanguageUtil.get(httpServletRequest, "download"), "get", null,
				"link"));

		return fdsActionDropdownItems;
	}

	@Override
	protected String getCMSSectionFilterString() {
		return "(cmsSection eq 'contents' or cmsSection eq 'files') and " +
			"cmsKind eq 'object'";
	}

	@Override
	protected String[] getObjectFolderExternalReferenceCodes() {
		return new String[] {
			ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENT_STRUCTURES,
			ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_FILE_TYPES
		};
	}

	@Override
	protected String getRootObjectEntryFolderExternalReferenceCode() {
		return null;
	}

	@Override
	protected void initEmptyState() {
		emptyStateDescriptionKey = "click-new-to-create-your-first-asset";
		emptyStateImage = "/states/cms_empty_state.svg";
		emptyStateTitleKey = "no-assets-yet";
	}

}