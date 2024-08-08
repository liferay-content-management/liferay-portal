/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dynamic.data.mapping.internal.upgrade.v5_5_1;

import com.liferay.dynamic.data.mapping.model.DDMStructureVersion;
import com.liferay.dynamic.data.mapping.service.DDMStructureVersionLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Alicia García
 */
public class DDMFieldUpgradeProcess extends UpgradeProcess {

	public DDMFieldUpgradeProcess(
		DDMStructureVersionLocalService ddmStructureVersionLocalService) {

		_ddmStructureVersionLocalService = ddmStructureVersionLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement selectPreparedStatement =
				connection.prepareStatement(
					SQLTransformer.transform(
						"select ctCollectionId, fieldId, structureVersionId " +
							"from DDMField where companyId = 0"));
			PreparedStatement updatePreparedStatement =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection,
					"update DDMField set companyId = ? where ctCollectionId " +
						"= ? and fieldId = ?")) {

			ResultSet resultSet = selectPreparedStatement.executeQuery();

			while (resultSet.next()) {
				long structureVersionId = resultSet.getLong(
					"structureVersionId");

				DDMStructureVersion ddmStructureVersion =
					_ddmStructureVersionLocalService.fetchDDMStructureVersion(
						structureVersionId);

				if (ddmStructureVersion == null) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							"DDMStructureVersion not found for field ID " +
								resultSet.getLong("fieldId"));
					}
				}
				else {
					updatePreparedStatement.setLong(
						1, ddmStructureVersion.getCompanyId());

					updatePreparedStatement.setLong(
						2, resultSet.getLong("ctCollectionId"));
					updatePreparedStatement.setLong(
						3, resultSet.getLong("fieldId"));

					updatePreparedStatement.addBatch();

					if (_log.isWarnEnabled()) {
						_log.warn(
							StringBundler.concat(
								"Change the company id value for field ID ",
								resultSet.getLong("fieldId"), " from 0 to ",
								ddmStructureVersion.getCompanyId()));
					}
				}
			}

			updatePreparedStatement.executeBatch();
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DDMFieldUpgradeProcess.class);

	private final DDMStructureVersionLocalService
		_ddmStructureVersionLocalService;

}