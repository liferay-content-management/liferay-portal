/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectDefinitionSettingLocalService;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.Portal;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Sam Ziemer
 */
public class ViewContentsSectionDisplayContext
	extends BaseSectionDisplayContext {

	public ViewContentsSectionDisplayContext(
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
	protected String getCMSSectionFilterString() {
		return "cmsSection eq 'contents' and cmsRoot eq true";
	}

	@Override
	protected String[] getObjectFolderExternalReferenceCodes() {
		return new String[] {
			ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENT_STRUCTURES
		};
	}

	@Override
	protected String getRootObjectEntryFolderExternalReferenceCode() {
		return ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS;
	}

	@Override
	protected void initEmptyState() {
		emptyStateDescriptionKey =
			"click-new-to-create-your-first-piece-of-content";
		emptyStateImage = "/states/cms_empty_state_content.svg";
		emptyStateTitleKey = "no-content-yet";
	}

}