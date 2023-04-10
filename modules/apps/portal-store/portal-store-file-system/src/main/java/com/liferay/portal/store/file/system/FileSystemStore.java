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

package com.liferay.portal.store.file.system;

import com.liferay.document.library.kernel.exception.NoSuchFileException;
import com.liferay.document.library.kernel.store.AreaStore;
import com.liferay.document.library.kernel.util.DLUtil;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.store.file.system.configuration.FileSystemStoreConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Brian Wing Shun Chan
 * @author Sten Martinez
 * @author Alexander Chow
 * @author Edward Han
 * @author Manuel de la Peña
 */
public class FileSystemStore implements AreaStore {

	public FileSystemStore(
		FileSystemStoreConfiguration fileSystemStoreConfiguration) {

		String path = fileSystemStoreConfiguration.rootDir();

		if (StringUtil.endsWith(path, CharPool.SLASH)) {
			path = path.substring(0, path.length() - 1);
		}

		for (AreaType areaType : AreaType.values()) {
			String areaPath = path + areaType.getDirectorySuffix();

			File rootDir = new File(areaPath);

			if (!rootDir.isAbsolute()) {
				rootDir = new File(
					PropsUtil.get(PropsKeys.LIFERAY_HOME), areaPath);
			}

			_rootDirs.put(areaType, rootDir);

			rootDir.mkdirs();
		}

		try {
			FileUtil.write(
				new File(_rootDirs.get(AreaType.LIVE), "README.txt"),
				StringUtil.read(
					FileSystemStore.class, "dependencies/README.txt"));
		}
		catch (IOException ioException) {
			ReflectionUtil.throwException(ioException);
		}
	}

	@Override
	public void addFile(
		AreaType areaType, long companyId, long repositoryId, String fileName,
		String versionLabel, InputStream inputStream) {

		if (Validator.isNull(versionLabel)) {
			versionLabel = getHeadVersionLabel(
				areaType, companyId, repositoryId, fileName);
		}

		try {
			FileUtil.write(
				getFileNameVersionFile(
					areaType, companyId, repositoryId, fileName, versionLabel),
				inputStream);
		}
		catch (IOException ioException) {
			throw new SystemException(ioException);
		}
	}

	@Override
	public void deleteDirectory(
		AreaType areaType, long companyId, long repositoryId, String dirName) {

		File dirNameDir = getDirNameDir(
			areaType, companyId, repositoryId, dirName);

		if (!dirNameDir.exists()) {
			return;
		}

		File parentFile = dirNameDir.getParentFile();

		FileUtil.deltree(dirNameDir);

		_deleteEmptyAncestors(parentFile);
	}

	@Override
	public void deleteFile(
		AreaType areaType, long companyId, long repositoryId, String fileName,
		String versionLabel) {

		if (Validator.isNull(versionLabel)) {
			versionLabel = getHeadVersionLabel(
				areaType, companyId, repositoryId, fileName);
		}

		File fileNameVersionFile = getFileNameVersionFile(
			areaType, companyId, repositoryId, fileName, versionLabel);

		if (!fileNameVersionFile.exists()) {
			return;
		}

		File parentFile = fileNameVersionFile.getParentFile();

		fileNameVersionFile.delete();

		_deleteEmptyAncestors(parentFile);
	}

	@Override
	public InputStream getFileAsStream(
			AreaType areaType, long companyId, long repositoryId,
			String fileName, String versionLabel)
		throws NoSuchFileException {

		if (Validator.isNull(versionLabel)) {
			versionLabel = getHeadVersionLabel(
				areaType, companyId, repositoryId, fileName);
		}

		File fileNameVersionFile = getFileNameVersionFile(
			areaType, companyId, repositoryId, fileName, versionLabel);

		try {
			return new FileInputStream(fileNameVersionFile);
		}
		catch (FileNotFoundException fileNotFoundException) {
			throw new NoSuchFileException(
				areaType, companyId, repositoryId, fileName, versionLabel,
				fileNotFoundException);
		}
	}

	@Override
	public String[] getFileNames(
		AreaType areaType, long companyId, long repositoryId, String dirName) {

		File dirNameDir = getDirNameDir(
			areaType, companyId, repositoryId, dirName);

		if (!dirNameDir.exists()) {
			return new String[0];
		}

		List<String> fileNames = new ArrayList<>();

		getFileNames(fileNames, dirName, dirNameDir.getPath());

		Collections.sort(fileNames);

		return fileNames.toArray(new String[0]);
	}

	@Override
	public long getFileSize(
			AreaType areaType, long companyId, long repositoryId,
			String fileName, String versionLabel)
		throws NoSuchFileException {

		if (Validator.isNull(versionLabel)) {
			versionLabel = getHeadVersionLabel(
				areaType, companyId, repositoryId, fileName);
		}

		File fileNameVersionFile = getFileNameVersionFile(
			areaType, companyId, repositoryId, fileName, versionLabel);

		if (!fileNameVersionFile.exists()) {
			throw new NoSuchFileException(
				areaType, companyId, repositoryId, fileName, versionLabel);
		}

		return fileNameVersionFile.length();
	}

	@Override
	public String[] getFileVersions(
		AreaType areaType, long companyId, long repositoryId, String fileName) {

		File fileNameDir = getFileNameDir(
			areaType, companyId, repositoryId, fileName);

		if (!fileNameDir.exists()) {
			return StringPool.EMPTY_ARRAY;
		}

		String[] versions = FileUtil.listFiles(fileNameDir);

		Arrays.sort(versions, DLUtil::compareVersions);

		return versions;
	}

	public File getRootDir(AreaType areaType) {
		return _rootDirs.get(areaType);
	}

	@Override
	public boolean hasFile(
		AreaType areaType, long companyId, long repositoryId, String fileName,
		String versionLabel) {

		if (Validator.isNull(versionLabel)) {
			versionLabel = getHeadVersionLabel(
				areaType, companyId, repositoryId, fileName);
		}

		File fileNameVersionFile = getFileNameVersionFile(
			areaType, companyId, repositoryId, fileName, versionLabel);

		return fileNameVersionFile.exists();
	}

	protected File getDirNameDir(
		AreaType areaType, long companyId, long repositoryId, String dirName) {

		return getFileNameDir(areaType, companyId, repositoryId, dirName);
	}

	protected File getFileNameDir(
		AreaType areaType, long companyId, long repositoryId, String fileName) {

		return new File(
			getRepositoryDir(areaType, companyId, repositoryId), fileName);
	}

	protected void getFileNames(
		List<String> fileNames, String dirName, String path) {

		String[] pathDirNames = FileUtil.listDirs(path);

		if (ArrayUtil.isNotEmpty(pathDirNames)) {
			for (String pathDirName : pathDirNames) {
				String subdirName = null;

				if (Validator.isBlank(dirName)) {
					subdirName = pathDirName;
				}
				else {
					subdirName = dirName + StringPool.SLASH + pathDirName;
				}

				getFileNames(
					fileNames, subdirName,
					path + StringPool.SLASH + pathDirName);
			}
		}
		else if (!dirName.isEmpty()) {
			File file = new File(path);

			if (file.isDirectory()) {
				fileNames.add(dirName);
			}
		}
	}

	protected File getFileNameVersionFile(
		AreaType areaType, long companyId, long repositoryId, String fileName,
		String version) {

		return new File(
			getFileNameDir(areaType, companyId, repositoryId, fileName),
			version);
	}

	protected String getHeadVersionLabel(
		AreaType areaType, long companyId, long repositoryId, String fileName) {

		File fileNameDir = getFileNameDir(
			areaType, companyId, repositoryId, fileName);

		if (!fileNameDir.exists()) {
			return VERSION_DEFAULT;
		}

		String[] versionLabels = FileUtil.listFiles(fileNameDir);

		String headVersionLabel = VERSION_DEFAULT;

		for (String versionLabel : versionLabels) {
			if (DLUtil.compareVersions(versionLabel, headVersionLabel) > 0) {
				headVersionLabel = versionLabel;
			}
		}

		return headVersionLabel;
	}

	protected File getRepositoryDir(
		AreaType areaType, long companyId, long repositoryId) {

		File repositoryDir = new File(
			getRootDir(areaType), companyId + StringPool.SLASH + repositoryId);

		if (!repositoryDir.exists()) {
			repositoryDir.mkdirs();
		}

		return repositoryDir;
	}

	private void _deleteEmptyAncestors(File file) {
		while (file != null) {
			if (!file.delete()) {
				return;
			}

			file = file.getParentFile();
		}
	}

	private final HashMap<AreaType, File> _rootDirs = new HashMap<>(
		ArrayUtil.getLength(AreaType.values()));

}