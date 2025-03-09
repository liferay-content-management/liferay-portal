/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.web.internal.info.item.provider;

import com.liferay.info.exception.NoSuchInfoItemException;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.ERCInfoItemIdentifier;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.provider.InfoItemObjectProvider;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Guilherme Camacho
 */
@Component(
	property = "item.class.name=com.liferay.object.model.ObjectEntryFolder",
	service = InfoItemObjectProvider.class
)
public class ObjectEntryFolderInfoItemObjectProvider
	implements InfoItemObjectProvider<ObjectEntryFolder> {

	@Override
	public ObjectEntryFolder getInfoItem(InfoItemIdentifier infoItemIdentifier)
		throws NoSuchInfoItemException {

		if (!(infoItemIdentifier instanceof ClassPKInfoItemIdentifier) &&
			!(infoItemIdentifier instanceof ERCInfoItemIdentifier)) {

			throw new NoSuchInfoItemException(
				"Unsupported info item identifier type " + infoItemIdentifier);
		}

		if (infoItemIdentifier instanceof ClassPKInfoItemIdentifier) {
			ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
				(ClassPKInfoItemIdentifier)infoItemIdentifier;

			ObjectEntryFolder objectEntryFolder =
				_objectEntryFolderLocalService.fetchObjectEntryFolder(
					classPKInfoItemIdentifier.getClassPK());

			if (objectEntryFolder == null) {
				throw new NoSuchInfoItemException(
					"Unable to get object entry folder " +
						classPKInfoItemIdentifier.getClassPK());
			}

			return objectEntryFolder;
		}

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		ERCInfoItemIdentifier ercInfoItemIdentifier =
			(ERCInfoItemIdentifier)infoItemIdentifier;

		Map<InfoItemIdentifier, ObjectEntryFolder> objectEntryFolders =
			_getObjectEntryFolder(serviceContext.getRequest());

		if (objectEntryFolders.containsKey(ercInfoItemIdentifier)) {
			return objectEntryFolders.get(ercInfoItemIdentifier);
		}

		throw new NoSuchInfoItemException(
			"Unable to get object entry " +
				ercInfoItemIdentifier.getExternalReferenceCode());
	}

	private Map<InfoItemIdentifier, ObjectEntryFolder> _getObjectEntryFolder(
		HttpServletRequest httpServletRequest) {

		if (httpServletRequest == null) {
			return new HashMap<>();
		}

		Map<InfoItemIdentifier, ObjectEntryFolder> objectEntryFolders =
			(Map<InfoItemIdentifier, ObjectEntryFolder>)
				httpServletRequest.getAttribute(_OBJECT_ENTRY_FOLDER);

		if (objectEntryFolders == null) {
			objectEntryFolders = new HashMap<>();

			httpServletRequest.setAttribute(
				_OBJECT_ENTRY_FOLDER, objectEntryFolders);
		}

		return objectEntryFolders;
	}

	private static final String _OBJECT_ENTRY_FOLDER =
		ObjectEntryFolderInfoItemObjectProvider.class.getName() +
			"#OBJECT_ENTRY_FOLDER";

	@Reference
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

}