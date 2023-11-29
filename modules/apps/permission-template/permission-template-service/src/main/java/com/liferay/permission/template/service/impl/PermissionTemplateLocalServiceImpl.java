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

	public PermissionTemplate addPermissionTemplate(
		long companyId, long groupId, String className, long classPK,
		boolean permissionTemplateEnabled) {

		long permissionTemplateId = counterLocalService.increment();

		PermissionTemplate permissionTemplate =
			permissionTemplatePersistence.create(permissionTemplateId);

		permissionTemplate.setGroupId(groupId);
		permissionTemplate.setCompanyId(companyId);
		permissionTemplate.setClassNameId(
			_classNameLocalService.getClassNameId(className));
		permissionTemplate.setClassPK(classPK);
		permissionTemplate.setPermissionTemplateEnabled(
			permissionTemplateEnabled);

		return permissionTemplatePersistence.update(permissionTemplate);
	}

	public PermissionTemplate fetchPermissionTemplate(
		String className, long classPK) {

		return permissionTemplatePersistence.fetchByC_C(
			_classNameLocalService.getClassNameId(className), classPK);
	}

	public PermissionTemplate updatePermissionTemplate(
		long companyId, long groupId, String className, long classPK,
		boolean permissionTemplateEnabled) {

		PermissionTemplate permissionTemplate = fetchPermissionTemplate(
			className, classPK);

		if (permissionTemplate == null) {
			return addPermissionTemplate(
				companyId, groupId, className, classPK,
				permissionTemplateEnabled);
		}

		permissionTemplate.setPermissionTemplateEnabled(
			permissionTemplateEnabled);

		return permissionTemplatePersistence.update(permissionTemplate);
	}

	@Reference
	private ClassNameLocalService _classNameLocalService;

}