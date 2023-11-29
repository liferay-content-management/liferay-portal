/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.permission.template.model;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.base.BaseTable;

import java.sql.Types;

/**
 * The table class for the &quot;PermissionTemplate&quot; database table.
 *
 * @author Brian Wing Shun Chan
 * @see PermissionTemplate
 * @generated
 */
public class PermissionTemplateTable
	extends BaseTable<PermissionTemplateTable> {

	public static final PermissionTemplateTable INSTANCE =
		new PermissionTemplateTable();

	public final Column<PermissionTemplateTable, Long> mvccVersion =
		createColumn(
			"mvccVersion", Long.class, Types.BIGINT, Column.FLAG_NULLITY);
	public final Column<PermissionTemplateTable, Long> ctCollectionId =
		createColumn(
			"ctCollectionId", Long.class, Types.BIGINT, Column.FLAG_PRIMARY);
	public final Column<PermissionTemplateTable, Long> permissionTemplateId =
		createColumn(
			"permissionTemplateId", Long.class, Types.BIGINT,
			Column.FLAG_PRIMARY);
	public final Column<PermissionTemplateTable, Long> groupId = createColumn(
		"groupId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<PermissionTemplateTable, Long> companyId = createColumn(
		"companyId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<PermissionTemplateTable, Long> classNameId =
		createColumn(
			"classNameId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<PermissionTemplateTable, Long> classPK = createColumn(
		"classPK", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<PermissionTemplateTable, Boolean>
		permissionTemplateEnabled = createColumn(
			"permissionTemplateEnabled", Boolean.class, Types.BOOLEAN,
			Column.FLAG_DEFAULT);

	private PermissionTemplateTable() {
		super("PermissionTemplate", PermissionTemplateTable::new);
	}

}