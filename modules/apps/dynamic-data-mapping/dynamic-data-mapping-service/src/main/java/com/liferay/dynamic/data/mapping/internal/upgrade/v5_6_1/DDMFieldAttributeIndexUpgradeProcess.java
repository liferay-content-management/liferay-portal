/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dynamic.data.mapping.internal.upgrade.v5_6_1;

import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.dao.db.IndexMetadata;
import com.liferay.portal.kernel.dao.db.IndexMetadataFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Jorge Avalos
 */
public class DDMFieldAttributeIndexUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		DBInspector dbInspector = new DBInspector(connection);

		IndexMetadata indexMetadata =
			IndexMetadataFactoryUtil.createIndexMetadata(
				false, "DDMFieldAttribute", "fieldId", "ctCollectionId");

		if (!dbInspector.hasIndex(
				"DDMFieldAttribute", indexMetadata.getIndexName())) {

			runSQL(indexMetadata.getCreateSQL(null));
		}
	}

}