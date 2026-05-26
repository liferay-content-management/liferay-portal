/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.storage.sharepoint.exception.SharepointAuthenticationRequiredException;
import com.liferay.object.storage.sharepoint.exception.SharepointGraphException;
import com.liferay.object.storage.sharepoint.graph.SharepointGraphClient;
import com.liferay.object.storage.sharepoint.model.TokenEntry;
import com.liferay.object.storage.sharepoint.service.TokenEntryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.util.ActionUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Jürgen Kappler
 */
public class ViewSharepointEntriesSectionDisplayContext {

	public ViewSharepointEntriesSectionDisplayContext(
		DepotEntryLocalService depotEntryLocalService, long groupId,
		GroupLocalService groupLocalService, Http http,
		HttpServletRequest httpServletRequest, JSONFactory jsonFactory,
		TokenEntryLocalService tokenEntryLocalService) {

		_depotEntryLocalService = depotEntryLocalService;
		_groupId = groupId;
		_groupLocalService = groupLocalService;
		_http = http;
		_jsonFactory = jsonFactory;
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

	public List<Map<String, String>> getEntries() {
		if (!isAuthenticated()) {
			return Collections.emptyList();
		}

		Group group = _groupLocalService.fetchGroup(_groupId);

		String folderURL = group.getTypeSettingsProperty("sharepointFolderUrl");

		TokenEntry tokenEntry = _tokenEntryLocalService.fetchTokenEntry(
			_groupId, _themeDisplay.getUserId());

		SharepointGraphClient sharepointGraphClient = new SharepointGraphClient(
			_http, _jsonFactory);

		try {
			List<JSONObject> driveItemJSONObjects =
				sharepointGraphClient.listChildren(
					tokenEntry.getAccessToken(), folderURL);

			List<Map<String, String>> entries = new ArrayList<>();

			for (JSONObject driveItemJSONObject : driveItemJSONObjects) {
				if (driveItemJSONObject.getJSONObject("file") == null) {
					continue;
				}

				entries.add(_toEntry(driveItemJSONObject));
			}

			return entries;
		}
		catch (SharepointAuthenticationRequiredException |
			   SharepointGraphException exception) {

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to list SharePoint entries for group " + _groupId,
					exception);
			}

			return Collections.emptyList();
		}
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

	private Map<String, String> _toEntry(JSONObject driveItemJSONObject) {
		JSONObject fileJSONObject = driveItemJSONObject.getJSONObject("file");

		String mimeType = "";

		if (fileJSONObject != null) {
			mimeType = fileJSONObject.getString("mimeType");
		}

		String lastModifiedByDisplayName = "";

		JSONObject lastModifiedByJSONObject = driveItemJSONObject.getJSONObject(
			"lastModifiedBy");

		if (lastModifiedByJSONObject != null) {
			JSONObject userJSONObject = lastModifiedByJSONObject.getJSONObject(
				"user");

			if (userJSONObject != null) {
				lastModifiedByDisplayName = userJSONObject.getString(
					"displayName");
			}
		}

		return HashMapBuilder.put(
			"lastModifiedByDisplayName", lastModifiedByDisplayName
		).put(
			"lastModifiedDateTime",
			driveItemJSONObject.getString("lastModifiedDateTime")
		).put(
			"mimeType", mimeType
		).put(
			"name", driveItemJSONObject.getString("name")
		).put(
			"size", String.valueOf(driveItemJSONObject.getLong("size"))
		).put(
			"webUrl", driveItemJSONObject.getString("webUrl")
		).build();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ViewSharepointEntriesSectionDisplayContext.class);

	private final DepotEntryLocalService _depotEntryLocalService;
	private final long _groupId;
	private final GroupLocalService _groupLocalService;
	private final Http _http;
	private final JSONFactory _jsonFactory;
	private final ThemeDisplay _themeDisplay;
	private final TokenEntryLocalService _tokenEntryLocalService;

}