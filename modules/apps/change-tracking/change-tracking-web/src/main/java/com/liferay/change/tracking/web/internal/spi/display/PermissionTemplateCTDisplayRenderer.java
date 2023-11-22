/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.change.tracking.web.internal.spi.display;

import com.liferay.change.tracking.spi.display.BaseCTDisplayRenderer;
import com.liferay.change.tracking.spi.display.CTDisplayRenderer;
import com.liferay.permission.template.model.PermissionTemplate;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.security.permission.ResourceActions;
import com.liferay.portal.kernel.service.ClassNameLocalService;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Quan Huynh
 */
@Component(service = CTDisplayRenderer.class)
public class PermissionTemplateCTDisplayRenderer
	extends BaseCTDisplayRenderer<PermissionTemplate> {

	@Override
	public Class<PermissionTemplate> getModelClass() {
		return PermissionTemplate.class;
	}

	@Override
	public String getTitle(
		Locale locale, PermissionTemplate permissionTemplate) {

		ClassName className = _classNameLocalService.fetchClassName(
			permissionTemplate.getClassNameId());

		if (className == null) {
			return null;
		}

		return _language.format(
			locale, "x-for-x",
			new String[] {
				_resourceActions.getModelResourceNamePrefix() +
					PermissionTemplate.class.getName(),
				_resourceActions.getModelResource(
					locale, className.getClassName())
			});
	}

	@Override
	public boolean isHideable(PermissionTemplate permissionTemplate) {
		return true;
	}

	@Override
	protected void buildDisplay(
		DisplayBuilder<PermissionTemplate> displayBuilder) {

		PermissionTemplate permissionTemplate = displayBuilder.getModel();

		displayBuilder.display(
			"permission-template-enabled",
			permissionTemplate.isPermissionTemplateEnabled());
	}

	@Reference
	private ClassNameLocalService _classNameLocalService;

	@Reference
	private Language _language;

	@Reference
	private ResourceActions _resourceActions;

}