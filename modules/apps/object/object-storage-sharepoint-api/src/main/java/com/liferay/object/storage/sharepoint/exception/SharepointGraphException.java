/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.exception;

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Jürgen Kappler
 */
public class SharepointGraphException extends PortalException {

	public SharepointGraphException(String msg) {
		super(msg);
	}

	public SharepointGraphException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public SharepointGraphException(Throwable throwable) {
		super(throwable);
	}

}