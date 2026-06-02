/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dynamic.data.mapping.internal.upgrade.v5_6_1.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.IndexMetadata;
import com.liferay.portal.kernel.dao.db.IndexMetadataFactoryUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import java.sql.Connection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jorge Avalos
 */
@RunWith(Arquillian.class)
public class DDMFieldAttributeIndexUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_connection = DataAccess.getConnection();
		_db = DBManagerUtil.getDB();
		_dbInspector = new DBInspector(_connection);
		_indexMetadata = IndexMetadataFactoryUtil.createIndexMetadata(
			false, "DDMFieldAttribute", "fieldId", "ctCollectionId");

		if (_dbInspector.hasIndex(
				"DDMFieldAttribute", _indexMetadata.getIndexName())) {

			_db.runSQL(_connection, _indexMetadata.getDropSQL());
		}
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		if (!_dbInspector.hasIndex(
				"DDMFieldAttribute", _indexMetadata.getIndexName())) {

			_db.runSQL(_connection, _indexMetadata.getCreateSQL(null));
		}

		DataAccess.cleanUp(_connection);
	}

	@Test
	public void testUpgradeProcess() throws Exception {
		Assert.assertFalse(
			_dbInspector.hasIndex(
				"DDMFieldAttribute", _indexMetadata.getIndexName()));

		_runUpgrade();

		Assert.assertTrue(
			_dbInspector.hasIndex(
				"DDMFieldAttribute", _indexMetadata.getIndexName()));

		_runUpgrade();

		Assert.assertTrue(
			_dbInspector.hasIndex(
				"DDMFieldAttribute", _indexMetadata.getIndexName()));
	}

	private void _runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);

		upgradeProcess.upgrade();
	}

	private static final String _CLASS_NAME =
		"com.liferay.dynamic.data.mapping.internal.upgrade.v5_6_1." +
			"DDMFieldAttributeIndexUpgradeProcess";

	private static Connection _connection;
	private static DB _db;
	private static DBInspector _dbInspector;
	private static IndexMetadata _indexMetadata;

	@Inject(
		filter = "(&(component.name=com.liferay.dynamic.data.mapping.internal.upgrade.registry.DDMServiceUpgradeStepRegistrator))"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}
