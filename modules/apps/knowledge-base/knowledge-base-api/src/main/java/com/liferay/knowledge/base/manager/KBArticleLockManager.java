/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.knowledge.base.manager;

import com.liferay.layout.model.LockedLayout;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;

import java.util.List;
import java.util.Locale;

import javax.portlet.ActionRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Marco Galluzzi
 */
public interface KBArticleLockManager {

	public void getLock(KBArticle kbArticle, long userId) throws PortalException;

	public boolean isUnlocked(long resourcePrimKey, long userId);

	public void unlock(KBArticle kbArticle, long userId);
}