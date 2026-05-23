/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.service.persistence.impl;

import com.liferay.object.storage.sharepoint.model.TokenEntryTable;
import com.liferay.object.storage.sharepoint.model.impl.TokenEntryImpl;
import com.liferay.object.storage.sharepoint.model.impl.TokenEntryModelImpl;
import com.liferay.portal.kernel.dao.orm.ArgumentsResolver;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.model.BaseModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;

/**
 * The arguments resolver class for retrieving value from TokenEntry.
 *
 * @author Jürgen Kappler
 * @generated
 */
@Component(
	property = {
		"class.name=com.liferay.object.storage.sharepoint.model.impl.TokenEntryImpl",
		"table.name=OSSharepoint_TokenEntry"
	},
	service = ArgumentsResolver.class
)
public class TokenEntryModelArgumentsResolver implements ArgumentsResolver {

	@Override
	public Object[] getArguments(
		FinderPath finderPath, BaseModel<?> baseModel, boolean checkColumn,
		boolean original) {

		String[] columnNames = finderPath.getColumnNames();

		if ((columnNames == null) || (columnNames.length == 0)) {
			if (baseModel.isNew()) {
				return new Object[0];
			}

			return null;
		}

		TokenEntryModelImpl tokenEntryModelImpl =
			(TokenEntryModelImpl)baseModel;

		long columnBitmask = tokenEntryModelImpl.getColumnBitmask();

		if (!checkColumn || (columnBitmask == 0)) {
			return _getValue(tokenEntryModelImpl, finderPath, original);
		}

		Long finderPathColumnBitmask = _finderPathColumnBitmasksCache.get(
			finderPath);

		if (finderPathColumnBitmask == null) {
			finderPathColumnBitmask = 0L;

			for (String columnName : columnNames) {
				finderPathColumnBitmask |= tokenEntryModelImpl.getColumnBitmask(
					columnName);
			}

			_finderPathColumnBitmasksCache.put(
				finderPath, finderPathColumnBitmask);
		}

		if ((columnBitmask & finderPathColumnBitmask) != 0) {
			return _getValue(tokenEntryModelImpl, finderPath, original);
		}

		return null;
	}

	@Override
	public String getClassName() {
		return TokenEntryImpl.class.getName();
	}

	@Override
	public String getTableName() {
		return TokenEntryTable.INSTANCE.getTableName();
	}

	private static Object[] _getValue(
		TokenEntryModelImpl tokenEntryModelImpl, FinderPath finderPath,
		boolean original) {

		String[] columnNames = finderPath.getColumnNames();

		Object[] arguments = new Object[columnNames.length];

		for (int i = 0; i < arguments.length; i++) {
			String columnName = columnNames[i];

			Object value;

			if (original) {
				value = tokenEntryModelImpl.getColumnOriginalValue(columnName);
			}
			else {
				value = tokenEntryModelImpl.getColumnValue(columnName);
			}

			arguments[i] = finderPath.normalizeArgument(i, value);
		}

		return arguments;
	}

	private static final Map<FinderPath, Long> _finderPathColumnBitmasksCache =
		new ConcurrentHashMap<>();

}
// LIFERAY-SERVICE-BUILDER-HASH:-362538233