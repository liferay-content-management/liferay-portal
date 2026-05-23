/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.exception;

import com.liferay.portal.kernel.exception.NoSuchModelException;

/**
 * @author Jürgen Kappler
 */
public class NoSuchTokenEntryException extends NoSuchModelException {

	public NoSuchTokenEntryException() {
	}

	public NoSuchTokenEntryException(String msg) {
		super(msg);
	}

	public NoSuchTokenEntryException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public NoSuchTokenEntryException(Throwable throwable) {
		super(throwable);
	}

}