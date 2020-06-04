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

package com.liferay.document.library.internal.upgrade.v3_1_0;

import com.liferay.document.library.kernel.model.DLFileEntryMetadata;
import com.liferay.document.library.kernel.util.DLUtil;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.Portal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Alicia Garcia
 */
public class UpgradeDLFileEntryType extends UpgradeProcess {

	public UpgradeDLFileEntryType(Portal portal) {
		_portal = portal;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_upgradeSchema();

		_populateFields();
	}

	private void _populateFields() throws Exception {
		try (PreparedStatement ps1 = connection.prepareStatement(
				"select uuid_, fileEntryTypeId, groupId, fileEntryTypeKey " +
					"from DLFileEntryType where ( dataDefinitionId IS NULL " +
						"OR dataDefinitionId = '')");
			PreparedStatement ps2 = connection.prepareStatement(
				"select structureId FROM DDMStructure where groupId = ? AND " +
					"classNameId = ? AND ( structureKey = ? OR structureKey " +
						"= ? OR structureKey = ? ) ");
			PreparedStatement ps3 = AutoBatchPreparedStatementUtil.autoBatch(
				connection.prepareStatement(
					"update DLFileEntryType set dataDefinitionId = ? where " +
						"fileEntryTypeId = ? "));
			ResultSet rs = ps1.executeQuery()) {

			long classNameId = _portal.getClassNameId(
				DLFileEntryMetadata.class);

			while (rs.next()) {
				ps2.setLong(1, rs.getLong(3));
				ps2.setLong(2, classNameId);
				ps2.setString(3, DLUtil.getDDMStructureKey(rs.getString(1)));
				ps2.setString(
					4, DLUtil.getDeprecatedDDMStructureKey(rs.getLong(2)));
				ps2.setString(5, rs.getString(4));

				try (ResultSet rs2 = ps2.executeQuery()) {
					if (rs2.next()) {
						ps3.setLong(1, rs2.getLong(1));
						ps3.setLong(2, rs.getLong(2));

						ps3.addBatch();
					}
				}
			}

			ps3.executeBatch();
		}
	}

	private void _upgradeSchema() throws Exception {
		if (!hasColumn("DLFileEntryType", "dataDefinitionId")) {
			runSQL("alter table DLFileEntryType add dataDefinitionId LONG ");
		}
	}

	private final Portal _portal;

}