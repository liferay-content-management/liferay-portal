/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.storage.sharepoint.model.TokenEntry;
import com.liferay.object.storage.sharepoint.service.TokenEntryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.util.ActionUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;

/**
 * @author Jürgen Kappler
 */
public class ViewSharepointEntriesSectionDisplayContext {

	public ViewSharepointEntriesSectionDisplayContext(
		DepotEntryLocalService depotEntryLocalService, long groupId,
		GroupLocalService groupLocalService,
		HttpServletRequest httpServletRequest,
		TokenEntryLocalService tokenEntryLocalService) {

		_depotEntryLocalService = depotEntryLocalService;
		_groupId = groupId;
		_groupLocalService = groupLocalService;
		_tokenEntryLocalService = tokenEntryLocalService;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getConnectURL() {
		return StringBundler.concat(
			"/c/portal/object_storage_sharepoint_oauth2_initiate?groupId=",
			_groupId, "&returnURL=",
			URLCodec.encodeURL(_themeDisplay.getURLCurrent()));
	}

	public String getSpaceSettingsURL() throws PortalException {
		DepotEntry depotEntry = _depotEntryLocalService.getGroupDepotEntry(
			_groupId);

		return ActionUtil.getSpaceSettingsURL(
			depotEntry.getDepotEntryId(), _themeDisplay.getURLCurrent(),
			_themeDisplay);
	}

	public boolean isAuthenticated() {
		if (!_isConfigured()) {
			return false;
		}

		TokenEntry tokenEntry = _tokenEntryLocalService.fetchTokenEntry(
			_groupId, _themeDisplay.getUserId());

		if (tokenEntry == null) {
			return false;
		}

		Date expirationDate = tokenEntry.getExpirationDate();

		if ((expirationDate == null) || expirationDate.before(new Date())) {
			return false;
		}

		return true;
	}

	public boolean isNotAuthenticated() {
		if (!_isConfigured()) {
			return false;
		}

		return !isAuthenticated();
	}

	public boolean isNotConfigured() {
		return !_isConfigured();
	}

	private boolean _isConfigured() {
		Group group = _groupLocalService.fetchGroup(_groupId);

		if (group == null) {
			return false;
		}

		String folderUrl = group.getTypeSettingsProperty("sharepointFolderUrl");

		return Validator.isNotNull(folderUrl);
	}

	private final DepotEntryLocalService _depotEntryLocalService;
	private final long _groupId;
	private final GroupLocalService _groupLocalService;
	private final ThemeDisplay _themeDisplay;
	private final TokenEntryLocalService _tokenEntryLocalService;

}