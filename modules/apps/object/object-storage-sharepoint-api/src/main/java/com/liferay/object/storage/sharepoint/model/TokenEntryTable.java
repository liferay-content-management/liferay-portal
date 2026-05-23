/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.model;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.base.BaseTable;

import java.sql.Types;

import java.util.Date;

/**
 * The table class for the &quot;OSSharepoint_TokenEntry&quot; database table.
 *
 * @author Jürgen Kappler
 * @see TokenEntry
 * @generated
 */
public class TokenEntryTable extends BaseTable<TokenEntryTable> {

	public static final TokenEntryTable INSTANCE = new TokenEntryTable();

	public final Column<TokenEntryTable, Long> mvccVersion = createColumn(
		"mvccVersion", Long.class, Types.BIGINT, Column.FLAG_NULLITY);
	public final Column<TokenEntryTable, Long> tokenEntryId = createColumn(
		"tokenEntryId", Long.class, Types.BIGINT, Column.FLAG_PRIMARY);
	public final Column<TokenEntryTable, Long> groupId = createColumn(
		"groupId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<TokenEntryTable, Long> companyId = createColumn(
		"companyId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<TokenEntryTable, Long> userId = createColumn(
		"userId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<TokenEntryTable, String> userName = createColumn(
		"userName", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<TokenEntryTable, Date> createDate = createColumn(
		"createDate", Date.class, Types.TIMESTAMP, Column.FLAG_DEFAULT);
	public final Column<TokenEntryTable, String> accessToken = createColumn(
		"accessToken", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<TokenEntryTable, Date> expirationDate = createColumn(
		"expirationDate", Date.class, Types.TIMESTAMP, Column.FLAG_DEFAULT);
	public final Column<TokenEntryTable, String> refreshToken = createColumn(
		"refreshToken", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);

	private TokenEntryTable() {
		super("OSSharepoint_TokenEntry", TokenEntryTable::new);
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:103927750