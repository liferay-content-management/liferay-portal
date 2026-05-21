/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.internal.resource.v1_0;

import com.liferay.headless.delivery.dto.v1_0.AssetListEntry;
import com.liferay.headless.delivery.internal.odata.entity.v1_0.AssetListEntryEntityModel;
import com.liferay.headless.delivery.resource.v1_0.AssetListEntryResource;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.SearchUtil;

import jakarta.ws.rs.core.MultivaluedMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Luis Ortiz
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/asset-list-entry.properties",
	scope = ServiceScope.PROTOTYPE, service = AssetListEntryResource.class
)
public class AssetListEntryResourceImpl extends BaseAssetListEntryResourceImpl {

	@Override
	public Page<AssetListEntry> getAssetLibraryAssetListEntriesPage(
			Long assetLibraryId, String search, Filter filter,
			Pagination pagination, Sort[] sorts)
		throws Exception {

		return _getAssetListEntriesPage(
			assetLibraryId, search, filter, pagination, sorts);
	}

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap) {
		return _entityModel;
	}

	@Override
	public Page<AssetListEntry> getSiteAssetListEntriesPage(
			Long siteId, String search, Filter filter, Pagination pagination,
			Sort[] sorts)
		throws Exception {

		return _getAssetListEntriesPage(
			siteId, search, filter, pagination, sorts);
	}

	private Page<AssetListEntry> _getAssetListEntriesPage(
			Long groupId, String search, Filter filter, Pagination pagination,
			Sort[] sorts)
		throws Exception {

		return SearchUtil.search(
			null,
			booleanQuery -> {
			},
			filter, com.liferay.asset.list.model.AssetListEntry.class.getName(),
			GetterUtil.getString(search), pagination,
			queryConfig -> queryConfig.setSelectedFieldNames(
				Field.ENTRY_CLASS_PK),
			searchContext -> {
				searchContext.setCompanyId(contextCompany.getCompanyId());
				searchContext.setGroupIds(new long[] {groupId});
				searchContext.setUserId(contextUser.getUserId());
			},
			sorts,
			document -> _toAssetListEntry(
				GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK))));
	}

	private AssetListEntry _toAssetListEntry(long assetListEntryId)
		throws Exception {

		return _assetListEntryDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				null, assetListEntryId,
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser));
	}

	private static final EntityModel _entityModel =
		new AssetListEntryEntityModel();

	@Reference(
		target = "(component.name=com.liferay.headless.delivery.internal.dto.v1_0.converter.AssetListEntryDTOConverter)"
	)
	private DTOConverter
		<com.liferay.asset.list.model.AssetListEntry, AssetListEntry>
			_assetListEntryDTOConverter;

}