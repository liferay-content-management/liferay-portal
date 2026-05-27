/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.graph;

import com.liferay.object.storage.sharepoint.exception.SharepointAuthenticationRequiredException;
import com.liferay.object.storage.sharepoint.exception.SharepointGraphException;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jürgen Kappler
 */
public class SharepointGraphClient {

	public SharepointGraphClient(Http http, JSONFactory jsonFactory) {
		_http = http;
		_jsonFactory = jsonFactory;
	}

	public JSONObject createDriveItem(
			String accessToken, String folderURL, String name)
		throws SharepointAuthenticationRequiredException,
			   SharepointGraphException {

		FolderURLParts folderURLParts = _parseFolderURL(folderURL);

		String driveId = _resolveDriveId(accessToken, folderURLParts);

		JSONObject folderJSONObject = _getFolderByPathJSONObject(
			accessToken, driveId, folderURLParts.getFolderRelativePath());

		String folderId = folderJSONObject.getString("id");

		return _put(
			accessToken,
			StringBundler.concat(
				"https://graph.microsoft.com/v1.0/drives/", driveId, "/items/",
				folderId, ":/", _encodePath(name), ":/content"));
	}

	public JSONObject getDriveItem(
			String accessToken, String folderURL, String itemId)
		throws SharepointAuthenticationRequiredException,
			   SharepointGraphException {

		FolderURLParts folderURLParts = _parseFolderURL(folderURL);

		String driveId = _resolveDriveId(accessToken, folderURLParts);

		return _get(
			accessToken,
			StringBundler.concat(
				"https://graph.microsoft.com/v1.0/drives/", driveId, "/items/",
				itemId));
	}

	public List<JSONObject> listChildren(String accessToken, String folderURL)
		throws SharepointAuthenticationRequiredException,
			   SharepointGraphException {

		FolderURLParts folderURLParts = _parseFolderURL(folderURL);

		String driveId = _resolveDriveId(accessToken, folderURLParts);

		JSONObject folderJSONObject = _getFolderByPathJSONObject(
			accessToken, driveId, folderURLParts.getFolderRelativePath());

		String folderId = folderJSONObject.getString("id");

		return _listFolderChildren(accessToken, driveId, folderId);
	}

	private String _encodePath(String path) {
		String[] segments = path.split("/", -1);

		StringBundler sb = new StringBundler(segments.length * 2);

		for (int i = 0; i < segments.length; i++) {
			if (i > 0) {
				sb.append("/");
			}

			String encoded = URLCodec.encodeURL(segments[i]);

			sb.append(StringUtil.replace(encoded, '+', "%20"));
		}

		return sb.toString();
	}

	private JSONObject _get(String accessToken, String url)
		throws SharepointAuthenticationRequiredException,
			   SharepointGraphException {

		try {
			Http.Options options = new Http.Options();

			options.addHeader("Accept", "application/json");
			options.addHeader("Authorization", "Bearer " + accessToken);
			options.setLocation(url);
			options.setMethod(Http.Method.GET);

			String responseBody = _http.URLtoString(options);

			Http.Response response = options.getResponse();

			int responseCode = response.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new SharepointAuthenticationRequiredException();
			}

			if ((responseCode < 200) || (responseCode >= 300)) {
				throw new SharepointGraphException(
					StringBundler.concat(
						"Graph request failed (HTTP ", responseCode, "): ",
						url));
			}

			return _jsonFactory.createJSONObject(responseBody);
		}
		catch (IOException ioException) {
			throw new SharepointGraphException(
				"Graph request failed: " + url, ioException);
		}
		catch (JSONException jsonException) {
			throw new SharepointGraphException(
				"Failed to parse Graph response: " + url, jsonException);
		}
	}

	private JSONObject _getDefaultDriveJSONObject(
			String accessToken, String siteId)
		throws SharepointAuthenticationRequiredException,
			   SharepointGraphException {

		return _get(
			accessToken,
			"https://graph.microsoft.com/v1.0/sites/" + siteId + "/drive");
	}

	private JSONObject _getFolderByPathJSONObject(
			String accessToken, String driveId, String folderRelativePath)
		throws SharepointAuthenticationRequiredException,
			   SharepointGraphException {

		String url =
			"https://graph.microsoft.com/v1.0/drives/" + driveId + "/root";

		if (Validator.isNotNull(folderRelativePath)) {
			url += ":/" + _encodePath(folderRelativePath);
		}

		return _get(accessToken, url);
	}

	private JSONObject _getSiteByPathJSONObject(
			String accessToken, String host, String sitePath)
		throws SharepointAuthenticationRequiredException,
			   SharepointGraphException {

		String url = "https://graph.microsoft.com/v1.0/sites/" + host;

		if (Validator.isNotNull(sitePath)) {
			url += ":" + _encodePath(sitePath);
		}

		return _get(accessToken, url);
	}

	private List<JSONObject> _listFolderChildren(
			String accessToken, String driveId, String folderId)
		throws SharepointAuthenticationRequiredException,
			   SharepointGraphException {

		JSONObject jsonObject = _get(
			accessToken,
			StringBundler.concat(
				"https://graph.microsoft.com/v1.0/drives/", driveId, "/items/",
				folderId, "/children?$top=100"));

		JSONArray jsonArray = jsonObject.getJSONArray("value");

		List<JSONObject> driveItems = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			driveItems.add(jsonArray.getJSONObject(i));
		}

		return driveItems;
	}

	private FolderURLParts _parseFolderURL(String folderURL)
		throws SharepointGraphException {

		try {
			URI uri = new URI(StringUtil.replace(folderURL, ' ', "%20"));

			String host = uri.getHost();

			if (host == null) {
				throw new SharepointGraphException(
					"Folder URL has no host: " + folderURL);
			}

			String path = uri.getPath();

			int sitesIndex = path.indexOf("/sites/");

			String sitePath;
			String afterSite;

			if (sitesIndex < 0) {
				sitePath = "";

				if (path.startsWith("/")) {
					afterSite = path.substring(1);
				}
				else {
					afterSite = path;
				}
			}
			else {
				int afterSiteNameIndex = path.indexOf(
					"/", sitesIndex + "/sites/".length());

				if (afterSiteNameIndex < 0) {
					sitePath = path.substring(sitesIndex);
					afterSite = "";
				}
				else {
					sitePath = path.substring(sitesIndex, afterSiteNameIndex);
					afterSite = path.substring(afterSiteNameIndex + 1);
				}
			}

			int driveNameEndIndex = afterSite.indexOf("/");

			String folderRelativePath;

			if (driveNameEndIndex < 0) {
				folderRelativePath = "";
			}
			else {
				folderRelativePath = afterSite.substring(driveNameEndIndex + 1);
			}

			return new FolderURLParts(folderRelativePath, host, sitePath);
		}
		catch (URISyntaxException uriSyntaxException) {
			throw new SharepointGraphException(
				"Invalid folder URL: " + folderURL, uriSyntaxException);
		}
	}

	private JSONObject _put(String accessToken, String url)
		throws SharepointAuthenticationRequiredException,
			   SharepointGraphException {

		try {
			Http.Options options = new Http.Options();

			options.addHeader("Accept", "application/json");
			options.addHeader("Authorization", "Bearer " + accessToken);
			options.setBody("", "text/plain", "UTF-8");
			options.setLocation(url);
			options.setMethod(Http.Method.PUT);

			String responseBody = _http.URLtoString(options);

			Http.Response response = options.getResponse();

			int responseCode = response.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new SharepointAuthenticationRequiredException();
			}

			if ((responseCode < 200) || (responseCode >= 300)) {
				throw new SharepointGraphException(
					StringBundler.concat(
						"Graph request failed (HTTP ", responseCode, "): ",
						url));
			}

			return _jsonFactory.createJSONObject(responseBody);
		}
		catch (IOException ioException) {
			throw new SharepointGraphException(
				"Graph request failed: " + url, ioException);
		}
		catch (JSONException jsonException) {
			throw new SharepointGraphException(
				"Failed to parse Graph response: " + url, jsonException);
		}
	}

	private String _resolveDriveId(
			String accessToken, FolderURLParts folderURLParts)
		throws SharepointAuthenticationRequiredException,
			   SharepointGraphException {

		JSONObject siteJSONObject = _getSiteByPathJSONObject(
			accessToken, folderURLParts.getHost(),
			folderURLParts.getSitePath());

		String siteId = siteJSONObject.getString("id");

		JSONObject driveJSONObject = _getDefaultDriveJSONObject(
			accessToken, siteId);

		return driveJSONObject.getString("id");
	}

	private final Http _http;
	private final JSONFactory _jsonFactory;

	private static class FolderURLParts {

		public FolderURLParts(
			String folderRelativePath, String host, String sitePath) {

			_folderRelativePath = folderRelativePath;
			_host = host;
			_sitePath = sitePath;
		}

		public String getFolderRelativePath() {
			return _folderRelativePath;
		}

		public String getHost() {
			return _host;
		}

		public String getSitePath() {
			return _sitePath;
		}

		private final String _folderRelativePath;
		private final String _host;
		private final String _sitePath;

	}

}