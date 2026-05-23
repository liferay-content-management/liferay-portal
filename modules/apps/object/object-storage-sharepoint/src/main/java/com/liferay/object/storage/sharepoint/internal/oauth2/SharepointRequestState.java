/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.internal.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;

import java.util.UUID;

/**
 * @author Jürgen Kappler
 */
public final class SharepointRequestState implements Serializable {

	public static SharepointRequestState pop(
		HttpServletRequest httpServletRequest, String state) {

		HttpSession httpSession = httpServletRequest.getSession();

		String sessionKey = _getSessionKey(state);

		SharepointRequestState sharepointRequestState =
			(SharepointRequestState)httpSession.getAttribute(sessionKey);

		httpSession.removeAttribute(sessionKey);

		return sharepointRequestState;
	}

	public static String save(
		long groupId, HttpServletRequest httpServletRequest, String returnURL) {

		String state = UUID.randomUUID(
		).toString();

		HttpSession httpSession = httpServletRequest.getSession();

		httpSession.setAttribute(
			_getSessionKey(state),
			new SharepointRequestState(groupId, returnURL));

		return state;
	}

	public long getGroupId() {
		return _groupId;
	}

	public String getReturnURL() {
		return _returnURL;
	}

	private static String _getSessionKey(String state) {
		return SharepointRequestState.class.getName() + ":" + state;
	}

	private SharepointRequestState(long groupId, String returnURL) {
		_groupId = groupId;
		_returnURL = returnURL;
	}

	private static final long serialVersionUID = 1L;

	private final long _groupId;
	private final String _returnURL;

}