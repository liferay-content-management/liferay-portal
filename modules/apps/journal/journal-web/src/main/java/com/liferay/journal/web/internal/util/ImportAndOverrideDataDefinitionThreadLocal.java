/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.journal.web.internal.util;

import com.liferay.petra.lang.CentralizedThreadLocal;
import com.liferay.petra.lang.SafeCloseable;

/**
 * @author Jürgen Kappler
 */
public class ImportAndOverrideDataDefinitionThreadLocal {

	public static Boolean isImportAndOverrideDataDefinition() {
		return _importAndOverrideDataDefinition.get();
	}

	public static SafeCloseable
		setImportAndOverrideDataDefinitionWithSafeCloseable(
			Boolean importAndOverrideDataDefinition) {

		boolean currentImportAndOverrideDataDefinition =
			_importAndOverrideDataDefinition.get();

		_importAndOverrideDataDefinition.set(importAndOverrideDataDefinition);

		return () -> _importAndOverrideDataDefinition.set(
			currentImportAndOverrideDataDefinition);
	}

	private static final CentralizedThreadLocal<Boolean>
		_importAndOverrideDataDefinition = new CentralizedThreadLocal<>(
			ImportAndOverrideDataDefinitionThreadLocal.class +
				"._importAndOverrideDataDefinition",
			() -> Boolean.FALSE);

}