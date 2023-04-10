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

package com.liferay.document.library.kernel.exception;

import com.liferay.document.library.kernel.store.AreaStore;
import com.liferay.portal.kernel.exception.NoSuchModelException;

/**
 * @author Brian Wing Shun Chan
 */
public class NoSuchFileException extends NoSuchModelException {

	public NoSuchFileException() {
	}

	public NoSuchFileException(
		AreaStore.AreaType areaType, long companyId, long repositoryId,
		String fileName, String version) {

		super(
			String.format(
				"{areaType=%s, companyId=%s, repositoryId=%s, fileName=%s, " +
					"version=%s}",
				areaType, companyId, repositoryId, fileName, version));
	}

	public NoSuchFileException(
		AreaStore.AreaType areaType, long companyId, long repositoryId,
		String fileName, String version, Throwable throwable) {

		super(
			String.format(
				"{areaType=%s, companyId=%s, repositoryId=%s, fileName=%s, " +
					"version=%s, cause=%s}",
				areaType, companyId, repositoryId, fileName, version,
				throwable),
			throwable);
	}

	public NoSuchFileException(
		long companyId, long repositoryId, String fileName) {

		super(
			String.format(
				"{companyId=%s, repositoryId=%s, fileName=%s}", companyId,
				repositoryId, fileName));
	}

	public NoSuchFileException(
		long companyId, long repositoryId, String fileName, String version) {

		super(
			String.format(
				"{companyId=%s, repositoryId=%s, fileName=%s, version=%s}",
				companyId, repositoryId, fileName, version));
	}

	public NoSuchFileException(
		long companyId, long repositoryId, String fileName, String version,
		Throwable throwable) {

		super(
			String.format(
				"{companyId=%s, repositoryId=%s, fileName=%s, version=%s, " +
					"cause=%s}",
				companyId, repositoryId, fileName, version, throwable),
			throwable);
	}

	public NoSuchFileException(
		long companyId, long repositoryId, String fileName,
		Throwable throwable) {

		super(
			String.format(
				"{companyId=%s, repositoryId=%s, fileName=%s, cause=%s}",
				companyId, repositoryId, fileName, throwable),
			throwable);
	}

	public NoSuchFileException(String msg) {
		super(msg);
	}

	public NoSuchFileException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public NoSuchFileException(Throwable throwable) {
		super(throwable);
	}

}