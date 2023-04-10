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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.InputStream;

/**
 * @author Marco Galluzzi
 */
public interface AreaStore extends Store {

	public void addFile(
			AreaType areaType, long companyId, long repositoryId,
			String fileName, String versionLabel, InputStream inputStream)
		throws PortalException;

	public default void addFile(
			long companyId, long repositoryId, String fileName,
			String versionLabel, InputStream inputStream)
		throws PortalException {

		addFile(
			AreaType.LIVE, companyId, repositoryId, fileName, versionLabel,
			inputStream);
	}

	public void deleteDirectory(
		AreaType areaType, long companyId, long repositoryId, String dirName);

	public default void deleteDirectory(
		long companyId, long repositoryId, String dirName) {

		deleteDirectory(AreaType.LIVE, companyId, repositoryId, dirName);
	}

	public void deleteFile(
		AreaType areaType, long companyId, long repositoryId, String fileName,
		String versionLabel);

	public default void deleteFile(
		long companyId, long repositoryId, String fileName,
		String versionLabel) {

		deleteFile(
			AreaType.LIVE, companyId, repositoryId, fileName, versionLabel);
	}

	public InputStream getFileAsStream(
			AreaType areaType, long companyId, long repositoryId,
			String fileName, String versionLabel)
		throws PortalException;

	public default InputStream getFileAsStream(
			long companyId, long repositoryId, String fileName,
			String versionLabel)
		throws PortalException {

		return getFileAsStream(
			AreaType.LIVE, companyId, repositoryId, fileName, versionLabel);
	}

	public String[] getFileNames(
		AreaType areaType, long companyId, long repositoryId, String dirName);

	public default String[] getFileNames(
		long companyId, long repositoryId, String dirName) {

		return getFileNames(AreaType.LIVE, companyId, repositoryId, dirName);
	}

	public long getFileSize(
			AreaType areaType, long companyId, long repositoryId,
			String fileName, String versionLabel)
		throws PortalException;

	public default long getFileSize(
			long companyId, long repositoryId, String fileName,
			String versionLabel)
		throws PortalException {

		return getFileSize(
			AreaType.LIVE, companyId, repositoryId, fileName, versionLabel);
	}

	public String[] getFileVersions(
		AreaType areaType, long companyId, long repositoryId, String fileName);

	public default String[] getFileVersions(
		long companyId, long repositoryId, String fileName) {

		return getFileVersions(
			AreaType.LIVE, companyId, repositoryId, fileName);
	}

	public boolean hasFile(
		AreaType areaType, long companyId, long repositoryId, String fileName,
		String versionLabel);

	public default boolean hasFile(
		long companyId, long repositoryId, String fileName,
		String versionLabel) {

		return hasFile(
			AreaType.LIVE, companyId, repositoryId, fileName, versionLabel);
	}

	public enum AreaType {

		DELETED, EVICTED, LIVE;

		public String getDirectorySuffix() {
			if (this == LIVE) {
				return "";
			}

			return "_" + StringUtil.lowerCase(toString());
		}

	}

}