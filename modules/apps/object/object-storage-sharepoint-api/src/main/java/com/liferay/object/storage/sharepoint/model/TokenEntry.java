/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.model;

import com.liferay.portal.kernel.annotation.ImplementationClassName;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.util.Accessor;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The extended model interface for the TokenEntry service. Represents a row in the &quot;OSSharepoint_TokenEntry&quot; database table, with each column mapped to a property of this class.
 *
 * @author Jürgen Kappler
 * @see TokenEntryModel
 * @generated
 */
@ImplementationClassName(
	"com.liferay.object.storage.sharepoint.model.impl.TokenEntryImpl"
)
@ProviderType
public interface TokenEntry extends PersistedModel, TokenEntryModel {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this interface directly. Add methods to <code>com.liferay.object.storage.sharepoint.model.impl.TokenEntryImpl</code> and rerun ServiceBuilder to automatically copy the method declarations to this interface.
	 */
	public static final Accessor<TokenEntry, Long> TOKEN_ENTRY_ID_ACCESSOR =
		new Accessor<TokenEntry, Long>() {

			@Override
			public Long get(TokenEntry tokenEntry) {
				return tokenEntry.getTokenEntryId();
			}

			@Override
			public Class<Long> getAttributeClass() {
				return Long.class;
			}

			@Override
			public Class<TokenEntry> getTypeClass() {
				return TokenEntry.class;
			}

		};

}
// LIFERAY-SERVICE-BUILDER-HASH:-1675381365