/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context;

import com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition;
import com.liferay.object.admin.rest.resource.v1_0.ObjectDefinitionResource;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Marco Galluzzi
 */
public class StructureUsagesDisplayContext extends BaseSectionDisplayContext {

	public StructureUsagesDisplayContext(
		HttpServletRequest httpServletRequest,
		ObjectDefinitionResource.Factory objectDefinitionResourceFactory) {

		super(null, httpServletRequest);

		_objectDefinitionResourceFactory = objectDefinitionResourceFactory;
	}

	@Override
	public String getAPIURL() {
		ObjectDefinition objectDefinition = _getObjectDefinition();

		if (objectDefinition != null) {
			return objectDefinition.getRestContextPath();
		}

		return null;
	}

	private ObjectDefinition _getObjectDefinition() {
		if (_objectDefinition != null) {
			return _objectDefinition;
		}

		long objectDefinitionId = ParamUtil.getLong(
			httpServletRequest, "objectDefinitionId");

		if (objectDefinitionId <= 0) {
			return null;
		}

		ObjectDefinitionResource.Builder builder =
			_objectDefinitionResourceFactory.create();

		ObjectDefinitionResource objectDefinitionResource = builder.user(
			themeDisplay.getUser()
		).build();

		try {
			_objectDefinition = objectDefinitionResource.getObjectDefinition(
				objectDefinitionId);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		return _objectDefinition;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		StructureUsagesDisplayContext.class);

	private ObjectDefinition _objectDefinition;
	private final ObjectDefinitionResource.Factory
		_objectDefinitionResourceFactory;

}