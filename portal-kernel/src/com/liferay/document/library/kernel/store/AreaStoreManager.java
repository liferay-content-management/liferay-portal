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

package com.liferay.document.library.kernel.store;

import com.liferay.petra.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.exception.PortalException;

import java.io.InputStream;

/**
 * @author Marco Galluzzi
 */
public class AreaStoreManager {

	public static void move(
			AreaStore store, AreaStore.AreaType sourceAreaType,
			AreaStore.AreaType targetAreaType, long companyId,
			long repositoryId, String fileName, String versionLabel)
		throws PortalException {

		InputStream inputStream = store.getFileAsStream(
			sourceAreaType, companyId, repositoryId, fileName, versionLabel);

		if (inputStream == null) {
			inputStream = new UnsyncByteArrayInputStream(new byte[0]);
		}

		store.addFile(
			targetAreaType, companyId, repositoryId, fileName, versionLabel,
			inputStream);

		store.deleteFile(
			sourceAreaType, companyId, repositoryId, fileName, versionLabel);
	}

}