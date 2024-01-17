/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.knowledge.base.internal.manager;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.DSLFunctionFactoryUtil;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.base.BaseTable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.LockedLayoutException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.lock.Lock;
import com.liferay.portal.kernel.lock.LockManager;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutTable;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.lock.model.LockTable;
import com.liferay.portal.lock.service.LockLocalService;
import com.liferay.portal.model.impl.LayoutModelImpl;

import java.sql.Types;

import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Galluzzi
 */
@Component(service = KBArticleLockManager.class)
public class KBArticleLockManagerImpl implements KBArticleLockManager {

	@Override
	public void getLock(KBArticle kbArticle, long userId) throws PortalException {
		if (!FeatureFlagManagerUtil.isEnabled("LPS-195016") ||
			(kbArticle == null)) {

			return;
		}

		Lock lock = _lockManager.fetchLock(
			KBArticleConstants.getClassName(), kbArticle.getResourcePrimKey());

		if (lock == null) {
			try {
				_lockManager.lock(
					userId, KBArticleConstants.getClassName(), kbArticle.getResourcePrimKey(),
					String.valueOf(userId), false,
					KBArticleConstants.LOCK_EXPIRATION_TIME);
			}
			catch (PortalException portalException) {
				throw new LockedKBArticleException(portalException);
			}
		}
		else if (lock.getUserId() == userId) {
			try {
				_lockManager.refresh(
					lock.getUuid(), lock.getCompanyId(),
					KBArticleConstants.LOCK_EXPIRATION_TIME);
			}
			catch (PortalException portalException) {
				throw new LockedKBArticleException(portalException);
			}
		}
		else {
			throw new LockedKBArticleException();
		}
	}

	@Override
	public boolean isUnlocked(long resourcePrimKey, long userId) {
		if (!FeatureFlagManagerUtil.isEnabled("LPS-195016")) {

			return true;
		}

		Lock lock = LockManagerUtil.fetchLock(
			KBArticleConstants.getClassName(), resourcePrimKey);

		if ((lock != null) && (lock.getUserId() != userId)) {
			return false;
		}

		return true;
	}

	@Override
	public void unlock(KBArticle kbArticle, long userId) {
		if (!FeatureFlagManagerUtil.isEnabled("LPS-195016") ||
			(kbArticle == null)) {

			return;
		}

		_lockManager.unlock(
			KBArticleConstants.getClassName(), String.valueOf(kbArticle.getResourcePrimKey()),
			String.valueOf(userId));
	}

	@Reference
	private KBArticleLocalService _KBArticleLocalService;

	@Reference
	private LockManager _lockManager;

}