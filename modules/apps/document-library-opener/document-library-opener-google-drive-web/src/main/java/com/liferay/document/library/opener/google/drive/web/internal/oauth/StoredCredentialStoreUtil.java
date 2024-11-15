/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.opener.google.drive.web.internal.oauth;

import com.google.api.client.auth.oauth2.StoredCredential;

import com.liferay.portal.kernel.cluster.ClusterExecutorUtil;
import com.liferay.portal.kernel.cluster.ClusterRequest;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Marco Galluzzi
 */
public class StoredCredentialStoreUtil {

	public static void add(
		long companyId, long userId, StoredCredential storedCredential) {

		_add(companyId, userId, storedCredential);

		if (ClusterExecutorUtil.isEnabled()) {
			_executeOnCluster(
				new MethodHandler(
					_addMethodKey, companyId, userId, storedCredential));
		}
	}

	public static void clear() {
		_clear();

		if (ClusterExecutorUtil.isEnabled()) {
			_executeOnCluster(new MethodHandler(_clearMethodKey));
		}
	}

	public static void delete(long companyId, long userId) {
		_delete(companyId, userId);

		if (ClusterExecutorUtil.isEnabled()) {
			_executeOnCluster(
				new MethodHandler(_deleteMethodKey, companyId, userId));
		}
	}

	public static StoredCredential get(long companyId, long userId) {
		Map<Long, StoredCredential> storedCredentials =
			_storedCredentials.getOrDefault(companyId, new HashMap<>());

		return storedCredentials.get(userId);
	}

	private static void _add(
		long companyId, long userId, StoredCredential storedCredential) {

		Map<Long, StoredCredential> accessTokens =
			_storedCredentials.computeIfAbsent(
				companyId, key -> new ConcurrentHashMap<>());

		accessTokens.put(userId, storedCredential);
	}

	private static void _clear() {
		_storedCredentials.clear();
	}

	private static void _delete(long companyId, long userId) {
		Map<Long, StoredCredential> storedCredentials =
			_storedCredentials.computeIfAbsent(
				companyId, key -> new ConcurrentHashMap<>());

		storedCredentials.remove(userId);
	}

	private static void _executeOnCluster(MethodHandler methodHandler) {
		ClusterRequest clusterRequest = ClusterRequest.createMulticastRequest(
			methodHandler, true);

		clusterRequest.setFireAndForget(true);

		ClusterExecutorUtil.execute(clusterRequest);
	}

	private static final MethodKey _addMethodKey = new MethodKey(
		StoredCredentialStoreUtil.class, "_add", long.class, long.class,
		StoredCredential.class);
	private static final MethodKey _clearMethodKey = new MethodKey(
		StoredCredentialStoreUtil.class, "_clear");
	private static final MethodKey _deleteMethodKey = new MethodKey(
		StoredCredentialStoreUtil.class, "_delete", long.class, long.class);
	private static final Map<Long, Map<Long, StoredCredential>>
		_storedCredentials = new ConcurrentHashMap<>();

}