/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.journal.internal.notifications;

import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.notifications.NotificationsHelper;

import org.osgi.service.component.annotations.Component;

/**
 * @author Alicia García
 */
@Component(service = NotificationsHelper.class)
public class JournalArticleNotificationsHelper implements NotificationsHelper {

	@Override
	public String getClassName() {
		return JournalArticle.class.getName();
	}

	@Override
	public String getPayload(String payload) {
		return "this is the payload for " + getClassName();
	}

}