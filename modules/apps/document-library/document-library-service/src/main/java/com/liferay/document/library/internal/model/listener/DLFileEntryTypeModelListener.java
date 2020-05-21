/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.document.library.internal.model.listener;

import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.dynamic.data.mapping.model.DDMStructureLink;
import com.liferay.dynamic.data.mapping.service.DDMStructureLinkLocalService;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.util.Portal;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alicia Garcia
 */
@Component(service = ModelListener.class)
public class DLFileEntryTypeModelListener
	extends BaseModelListener<DLFileEntryType> {

	@Override
	public void onBeforeRemove(DLFileEntryType dlFileEntryType)
		throws ModelListenerException {

		try {
			List<DDMStructureLink> ddmStructureLinks =
				_ddmStructureLinkLocalService.getStructureLinks(
					_portal.getClassNameId(DLFileEntryType.class),
					dlFileEntryType.getFileEntryTypeId());

			for (DDMStructureLink ddmStructureLink : ddmStructureLinks) {
				_ddmStructureLinkLocalService.deleteStructureLink(
					_portal.getClassNameId(DLFileEntryType.class),
					dlFileEntryType.getFileEntryTypeId(),
					ddmStructureLink.getStructureId());
			}
		}
		catch (PortalException portalException) {
			_log.error(portalException, portalException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DLFileEntryTypeModelListener.class);

	@Reference
	private DDMStructureLinkLocalService _ddmStructureLinkLocalService;

	@Reference
	private Portal _portal;

}