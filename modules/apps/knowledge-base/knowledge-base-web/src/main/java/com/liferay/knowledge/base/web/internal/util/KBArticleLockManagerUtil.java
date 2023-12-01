/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.knowledge.base.web.internal.util;

import com.liferay.knowledge.base.constants.KBArticleConstants;
import com.liferay.knowledge.base.model.KBArticle;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.lock.LockManagerUtil;

/**
 * @author Marco Galluzzi
 */
public class KBArticleLockManagerUtil {

	public static void lock(long userId, long resourcePrimKey)
		throws PortalException {

		if (!FeatureFlagManagerUtil.isEnabled("LPS-195016")) {
			return;
		}

		LockManagerUtil.lock(
			userId, KBArticleConstants.getClassName(), resourcePrimKey,
			String.valueOf(userId), false,
			KBArticleConstants.LOCK_EXPIRATION_TIME);
	}

	public static void unlock(long userId, long resourcePrimKey) {
		if (!FeatureFlagManagerUtil.isEnabled("LPS-195016")) {
			return;
		}

		LockManagerUtil.unlock(
			KBArticleConstants.getClassName(), String.valueOf(resourcePrimKey),
			String.valueOf(userId));
	}

	public static KBArticle withLock(
			long userId, long resourcePrimKey,
			UnsafeFunction<Long, KBArticle, PortalException> unsafeFunction)
		throws PortalException {

		lock(userId, resourcePrimKey);

		try {
			return unsafeFunction.apply(resourcePrimKey);
		}
		finally {
			_unlock(resourcePrimKey);
		}
	}

	private static void _unlock(long resourcePrimKey) {
		if (!FeatureFlagManagerUtil.isEnabled("LPS-195016")) {
			return;
		}

		LockManagerUtil.unlock(
			KBArticleConstants.getClassName(), String.valueOf(resourcePrimKey));
	}

}