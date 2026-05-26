/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.list.internal.search;

import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryLocalService;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.spi.model.index.contributor.ModelIndexerWriterContributor;
import com.liferay.portal.search.spi.model.registrar.ModelSearchConfigurator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Ortiz
 */
@Component(service = ModelSearchConfigurator.class)
public class AssetListEntryModelSearchConfigurator
	implements ModelSearchConfigurator<AssetListEntry> {

	@Override
	public String getClassName() {
		return AssetListEntry.class.getName();
	}

	@Override
	public String[] getDefaultSelectedFieldNames() {
		return new String[] {
			Field.COMPANY_ID, Field.ENTRY_CLASS_NAME, Field.ENTRY_CLASS_PK,
			Field.GROUP_ID, Field.UID
		};
	}

	@Override
	public ModelIndexerWriterContributor<AssetListEntry>
		getModelIndexerWriterContributor() {

		return _modelIndexerWriterContributor;
	}

	@Activate
	protected void activate() {
		_modelIndexerWriterContributor = new ModelIndexerWriterContributor<>(
			_assetListEntryLocalService::getIndexableActionableDynamicQuery);
	}

	@Reference
	private AssetListEntryLocalService _assetListEntryLocalService;

	private ModelIndexerWriterContributor<AssetListEntry>
		_modelIndexerWriterContributor;

}