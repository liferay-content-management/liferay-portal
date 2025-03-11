/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.depot.web.internal.info.item.provider;

import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.info.exception.NoSuchInfoItemException;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.ERCInfoItemIdentifier;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.provider.InfoItemObjectProvider;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(
	property = "item.class.name=com.liferay.depot.model.DepotEntry",
	service = InfoItemObjectProvider.class
)
public class DepotEntryInfoItemObjectProvider
	implements InfoItemObjectProvider<DepotEntry> {

	@Override
	public DepotEntry getInfoItem(InfoItemIdentifier infoItemIdentifier)
		throws NoSuchInfoItemException {

		if (!(infoItemIdentifier instanceof ClassPKInfoItemIdentifier) &&
			!(infoItemIdentifier instanceof ERCInfoItemIdentifier)) {

			throw new NoSuchInfoItemException(
				"Unsupported info item identifier type " + infoItemIdentifier);
		}

		if (infoItemIdentifier instanceof ClassPKInfoItemIdentifier) {
			ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
				(ClassPKInfoItemIdentifier)infoItemIdentifier;

			DepotEntry depotEntry = _depotEntryLocalService.fetchDepotEntry(
				classPKInfoItemIdentifier.getClassPK());

			if (depotEntry == null) {
				throw new NoSuchInfoItemException(
					"Unable to get depot entry folder " +
						classPKInfoItemIdentifier.getClassPK());
			}

			return depotEntry;
		}

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		ERCInfoItemIdentifier ercInfoItemIdentifier =
			(ERCInfoItemIdentifier)infoItemIdentifier;

		Map<InfoItemIdentifier, DepotEntry> depotEntrys = _getDepotEntry(
			serviceContext.getRequest());

		if (depotEntrys.containsKey(ercInfoItemIdentifier)) {
			return depotEntrys.get(ercInfoItemIdentifier);
		}

		throw new NoSuchInfoItemException(
			"Unable to get depot entry " +
				ercInfoItemIdentifier.getExternalReferenceCode());
	}

	private Map<InfoItemIdentifier, DepotEntry> _getDepotEntry(
		HttpServletRequest httpServletRequest) {

		if (httpServletRequest == null) {
			return new HashMap<>();
		}

		Map<InfoItemIdentifier, DepotEntry> depotEntrys =
			(Map<InfoItemIdentifier, DepotEntry>)
				httpServletRequest.getAttribute(_DEPOT_ENTRY);

		if (depotEntrys == null) {
			depotEntrys = new HashMap<>();

			httpServletRequest.setAttribute(_DEPOT_ENTRY, depotEntrys);
		}

		return depotEntrys;
	}

	private static final String _DEPOT_ENTRY =
		DepotEntryInfoItemObjectProvider.class.getName() + "#DEPOT_ENTRY";

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

}