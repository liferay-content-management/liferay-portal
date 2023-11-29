/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.permission.template.model;

import com.liferay.portal.kernel.annotation.ImplementationClassName;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.util.Accessor;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The extended model interface for the PermissionTemplate service. Represents a row in the &quot;PermissionTemplate&quot; database table, with each column mapped to a property of this class.
 *
 * @author Brian Wing Shun Chan
 * @see PermissionTemplateModel
 * @generated
 */
@ImplementationClassName(
	"com.liferay.permission.template.model.impl.PermissionTemplateImpl"
)
@ProviderType
public interface PermissionTemplate
	extends PermissionTemplateModel, PersistedModel {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this interface directly. Add methods to <code>com.liferay.permission.template.model.impl.PermissionTemplateImpl</code> and rerun ServiceBuilder to automatically copy the method declarations to this interface.
	 */
	public static final Accessor<PermissionTemplate, Long>
		PERMISSION_TEMPLATE_ID_ACCESSOR =
			new Accessor<PermissionTemplate, Long>() {

				@Override
				public Long get(PermissionTemplate permissionTemplate) {
					return permissionTemplate.getPermissionTemplateId();
				}

				@Override
				public Class<Long> getAttributeClass() {
					return Long.class;
				}

				@Override
				public Class<PermissionTemplate> getTypeClass() {
					return PermissionTemplate.class;
				}

			};

}