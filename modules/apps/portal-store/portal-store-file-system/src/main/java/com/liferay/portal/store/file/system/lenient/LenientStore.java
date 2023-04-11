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

package com.liferay.portal.store.file.system.lenient;

import com.liferay.document.library.kernel.store.AreaStore;
import com.liferay.petra.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.exception.PortalException;

import java.io.InputStream;

/**
 * @author Adolfo Pérez
 */
public class LenientStore implements AreaStore {

	public LenientStore(AreaStore store) {
		_store = store;
	}

	@Override
	public void addFile(
			AreaType areaType, long companyId, long repositoryId,
			String fileName, String versionLabel, InputStream inputStream)
		throws PortalException {

		_store.addFile(
			areaType, companyId, repositoryId, fileName, versionLabel,
			inputStream);
	}

	@Override
	public void deleteDirectory(
		AreaType areaType, long companyId, long repositoryId, String dirName) {

		_store.deleteDirectory(areaType, companyId, repositoryId, dirName);
	}

	@Override
	public void deleteFile(
		AreaType areaType, long companyId, long repositoryId, String fileName,
		String versionLabel) {

		_store.deleteFile(
			areaType, companyId, repositoryId, fileName, versionLabel);
	}

	@Override
	public InputStream getFileAsStream(
			AreaType areaType, long companyId, long repositoryId,
			String fileName, String versionLabel)
		throws PortalException {

		if (!_store.hasFile(
				areaType, companyId, repositoryId, fileName, versionLabel)) {

			_store.addFile(
				areaType, companyId, repositoryId, fileName, versionLabel,
				new UnsyncByteArrayInputStream(_DUMMY_CONTENT));
		}

		return _store.getFileAsStream(
			areaType, companyId, repositoryId, fileName, versionLabel);
	}

	@Override
	public String[] getFileNames(
		AreaType areaType, long companyId, long repositoryId, String dirName) {

		return _store.getFileNames(areaType, companyId, repositoryId, dirName);
	}

	@Override
	public long getFileSize(
			AreaType areaType, long companyId, long repositoryId,
			String fileName, String versionLabel)
		throws PortalException {

		if (!_store.hasFile(
				areaType, companyId, repositoryId, fileName, versionLabel)) {

			_store.addFile(
				areaType, companyId, repositoryId, fileName, versionLabel,
				new UnsyncByteArrayInputStream(_DUMMY_CONTENT));
		}

		return _store.getFileSize(
			areaType, companyId, repositoryId, fileName, versionLabel);
	}

	@Override
	public String[] getFileVersions(
		AreaType areaType, long companyId, long repositoryId, String fileName) {

		return _store.getFileVersions(
			areaType, companyId, repositoryId, fileName);
	}

	@Override
	public boolean hasFile(
		AreaType areaType, long companyId, long repositoryId, String fileName,
		String versionLabel) {

		return true;
	}

	private static final byte[] _DUMMY_CONTENT =
		"This is a test file.".getBytes();

	private final AreaStore _store;

}