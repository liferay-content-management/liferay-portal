/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.internal.dto.v1_0.converter;

import com.liferay.asset.list.service.AssetListEntryLocalService;
import com.liferay.headless.delivery.dto.v1_0.AssetListEntry;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Ortiz
 */
@Component(
	property = "dto.class.name=com.liferay.asset.list.model.AssetListEntry",
	service = DTOConverter.class
)
public class AssetListEntryDTOConverter
	implements DTOConverter
		<com.liferay.asset.list.model.AssetListEntry, AssetListEntry> {

	@Override
	public String getContentType() {
		return AssetListEntry.class.getSimpleName();
	}

	@Override
	public AssetListEntry toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		return toDTO(
			dtoConverterContext,
			_assetListEntryLocalService.getAssetListEntry(
				(Long)dtoConverterContext.getId()));
	}

	@Override
	public AssetListEntry toDTO(
			DTOConverterContext dtoConverterContext,
			com.liferay.asset.list.model.AssetListEntry assetListEntry)
		throws Exception {

		return new AssetListEntry() {
			{
				setAssetListEntryId(assetListEntry::getAssetListEntryId);
				setClassNameId(
					() -> _portal.getClassNameId(
						com.liferay.asset.list.model.AssetListEntry.class));
				setClassPK(assetListEntry::getAssetListEntryId);
				setDateCreated(assetListEntry::getCreateDate);
				setDateModified(assetListEntry::getModifiedDate);
				setExternalReferenceCode(
					assetListEntry::getExternalReferenceCode);
				setItemSubtype(assetListEntry::getAssetEntrySubtype);
				setItemType(assetListEntry::getAssetEntryType);
				setTitle(assetListEntry::getTitle);
			}
		};
	}

	@Reference
	private AssetListEntryLocalService _assetListEntryLocalService;

	@Reference
	private Portal _portal;

}