/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.knowledge.base.exception;

/**
 * @author Marco Galluzzi
 */
public class LockedKBArticleException extends PortalException {

	public LockedKBArticleException() {
	}

	public LockedKBArticleException(String msg) {
		super(msg);
	}

	public LockedKBArticleException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public LockedKBArticleException(Throwable throwable) {
		super(throwable);
	}

}