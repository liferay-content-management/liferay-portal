/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.internal.rest.manager.v1_0;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.BaseObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.storage.sharepoint.exception.SharepointAuthenticationRequiredException;
import com.liferay.object.storage.sharepoint.exception.SharepointGraphException;
import com.liferay.object.storage.sharepoint.graph.SharepointGraphClient;
import com.liferay.object.storage.sharepoint.model.TokenEntry;
import com.liferay.object.storage.sharepoint.service.TokenEntryLocalService;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	property = "object.entry.manager.storage.type=sharepoint",
	service = ObjectEntryManager.class
)
public class SharepointObjectEntryManagerImpl
	extends BaseObjectEntryManager implements ObjectEntryManager {

	@Override
	public ObjectEntry addObjectEntry(
			DTOConverterContext dtoConverterContext,
			ObjectDefinition objectDefinition, ObjectEntry objectEntry,
			String scopeKey)
		throws Exception {

		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteObjectEntry(
			long companyId, DTOConverterContext dtoConverterContext,
			String externalReferenceCode, ObjectDefinition objectDefinition,
			String scopeKey)
		throws Exception {

		throw new UnsupportedOperationException();
	}

	@Override
	public Page<ObjectEntry> getObjectEntries(
			long companyId, ObjectDefinition objectDefinition, String scopeKey,
			Aggregation aggregation, DTOConverterContext dtoConverterContext,
			String filterString, Pagination pagination, String search,
			Sort[] sorts)
		throws Exception {

		long groupId = GetterUtil.getLong(scopeKey);

		if (groupId == 0) {
			groupId = ParamUtil.getLong(
				dtoConverterContext.getHttpServletRequest(), "spaceGroupId");
		}

		if (groupId == 0) {
			return Page.of(Collections.emptyList());
		}

		Group group = groupLocalService.fetchGroup(groupId);

		if (group == null) {
			return Page.of(Collections.emptyList());
		}

		String folderURL = group.getTypeSettingsProperty("sharepointFolderUrl");

		if (Validator.isNull(folderURL)) {
			return Page.of(Collections.emptyList());
		}

		TokenEntry tokenEntry = _tokenEntryLocalService.fetchTokenEntry(
			groupId, dtoConverterContext.getUserId());

		if (tokenEntry == null) {
			return Page.of(Collections.emptyList());
		}

		SharepointGraphClient sharepointGraphClient = new SharepointGraphClient(
			_http, _jsonFactory);

		try {
			List<JSONObject> driveItemJSONObjects =
				sharepointGraphClient.listChildren(
					tokenEntry.getAccessToken(), folderURL);

			List<ObjectEntry> objectEntries = new ArrayList<>();

			for (JSONObject driveItemJSONObject : driveItemJSONObjects) {
				objectEntries.add(_toObjectEntry(driveItemJSONObject));
			}

			return Page.of(objectEntries);
		}
		catch (SharepointAuthenticationRequiredException |
			   SharepointGraphException exception) {

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to list SharePoint entries for group " + groupId,
					exception);
			}

			return Page.of(Collections.emptyList());
		}
	}

	@Override
	public ObjectEntry getObjectEntry(
			long companyId, DTOConverterContext dtoConverterContext,
			String externalReferenceCode, ObjectDefinition objectDefinition,
			String scopeKey)
		throws Exception {

		throw new UnsupportedOperationException();
	}

	@Override
	public String getStorageLabel(Locale locale) {
		return "SharePoint";
	}

	@Override
	public String getStorageType() {
		return "sharepoint";
	}

	@Override
	public ObjectEntry updateObjectEntry(
			long companyId, DTOConverterContext dtoConverterContext,
			String externalReferenceCode, ObjectDefinition objectDefinition,
			ObjectEntry objectEntry, String scopeKey)
		throws Exception {

		throw new UnsupportedOperationException();
	}

	private ObjectEntry _toObjectEntry(JSONObject driveItemJSONObject) {
		JSONObject fileJSONObject = driveItemJSONObject.getJSONObject("file");
		JSONObject folderJSONObject = driveItemJSONObject.getJSONObject(
			"folder");

		String mimeType = null;

		if (fileJSONObject != null) {
			mimeType = fileJSONObject.getString("mimeType");
		}

		String type = "file";

		if (folderJSONObject != null) {
			type = "folder";
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

		Map<String, Object> properties = HashMapBuilder.<String, Object>put(
			"downloadUrl",
			driveItemJSONObject.getString("@microsoft.graph.downloadUrl")
		).put(
			"lastModifiedByDisplayName", lastModifiedByDisplayName
		).put(
			"lastModifiedDateTime",
			driveItemJSONObject.getString("lastModifiedDateTime")
		).put(
			"mimeType", mimeType
		).put(
			"name", driveItemJSONObject.getString("name")
		).put(
			"size", driveItemJSONObject.getLong("size")
		).put(
			"type", type
		).put(
			"webUrl", driveItemJSONObject.getString("webUrl")
		).build();

		return new ObjectEntry() {
			{
				setExternalReferenceCode(
					() -> driveItemJSONObject.getString("id"));
				setProperties(() -> properties);
			}
		};
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SharepointObjectEntryManagerImpl.class);

	@Reference
	private Http _http;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private TokenEntryLocalService _tokenEntryLocalService;

}