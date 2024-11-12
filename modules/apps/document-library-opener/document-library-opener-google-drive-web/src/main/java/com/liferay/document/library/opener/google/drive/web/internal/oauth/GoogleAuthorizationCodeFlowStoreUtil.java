/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.opener.google.drive.web.internal.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;

import com.liferay.portal.kernel.cluster.ClusterExecutorUtil;
import com.liferay.portal.kernel.cluster.ClusterRequest;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Marco Galluzzi
 */
public class GoogleAuthorizationCodeFlowStoreUtil {

	public static void add(
		long companyId,
		GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow) {

		_add(companyId, googleAuthorizationCodeFlow);

		if (ClusterExecutorUtil.isEnabled()) {
			_executeOnCluster(
				new MethodHandler(
					_addMethodKey, companyId, googleAuthorizationCodeFlow));
		}
	}

	public static void clear() {
		_clear();

		if (ClusterExecutorUtil.isEnabled()) {
			_executeOnCluster(new MethodHandler(_clearMethodKey));
		}
	}

	public static GoogleAuthorizationCodeFlow get(long companyId) {
		return _googleAuthorizationCodeFlows.get(companyId);
	}

	private static void _add(
		long companyId,
		GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow) {

		_googleAuthorizationCodeFlows.put(
			companyId, googleAuthorizationCodeFlow);
	}

	private static void _clear() {
		_googleAuthorizationCodeFlows.clear();
	}

	private static void _executeOnCluster(MethodHandler methodHandler) {
		ClusterRequest clusterRequest = ClusterRequest.createMulticastRequest(
			methodHandler, true);

		clusterRequest.setFireAndForget(true);

		ClusterExecutorUtil.execute(clusterRequest);
	}

	private static final MethodKey _addMethodKey = new MethodKey(
		GoogleAuthorizationCodeFlowStoreUtil.class, "_add", long.class,
		GoogleAuthorizationCodeFlow.class);
	private static final MethodKey _clearMethodKey = new MethodKey(
		GoogleAuthorizationCodeFlowStoreUtil.class, "_clear");
	private static final Map<Long, GoogleAuthorizationCodeFlow>
		_googleAuthorizationCodeFlows = new ConcurrentHashMap<>();

}