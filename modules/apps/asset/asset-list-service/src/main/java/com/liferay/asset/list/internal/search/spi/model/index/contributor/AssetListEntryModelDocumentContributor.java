/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.internal.search.spi.model.index.contributor;

import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import org.osgi.service.component.annotations.Component;

/**
 * @author Luis Ortiz
 */
@Component(
	property = "indexer.class.name=com.liferay.asset.list.model.AssetListEntry",
	service = ModelDocumentContributor.class
)
public class AssetListEntryModelDocumentContributor
	implements ModelDocumentContributor<AssetListEntry> {

	@Override
	public void contribute(Document document, AssetListEntry assetListEntry) {
		document.addText(Field.TITLE, assetListEntry.getTitle());
		document.addKeyword(
			"assetEntrySubtype", assetListEntry.getAssetEntrySubtype());
		document.addKeyword(
			"assetEntryType", assetListEntry.getAssetEntryType());
	}

}