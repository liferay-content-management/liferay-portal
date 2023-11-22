/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.permission.template.service.impl;

import com.liferay.permission.template.model.PermissionTemplate;
import com.liferay.permission.template.service.base.PermissionTemplateLocalServiceBaseImpl;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.service.ClassNameLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Wing Shun Chan
 */
@Component(
	property = "model.class.name=com.liferay.permission.template.model.PermissionTemplate",
	service = AopService.class
)
public class PermissionTemplateLocalServiceImpl
	extends PermissionTemplateLocalServiceBaseImpl {

	public PermissionTemplate fetchPermissionTemplateSetting(
		String className, long classPK) {

		return permissionTemplatePersistence.fetchByC_C(
			_classNameLocalService.getClassNameId(className), classPK);
	}

	@Reference
	private ClassNameLocalService _classNameLocalService;

}