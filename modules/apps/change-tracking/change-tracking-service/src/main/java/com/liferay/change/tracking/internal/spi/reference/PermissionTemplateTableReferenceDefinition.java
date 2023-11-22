/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.change.tracking.internal.spi.reference;

import com.liferay.change.tracking.spi.reference.TableReferenceDefinition;
import com.liferay.change.tracking.spi.reference.builder.ChildTableReferenceInfoBuilder;
import com.liferay.change.tracking.spi.reference.builder.ParentTableReferenceInfoBuilder;
import com.liferay.permission.template.model.PermissionTemplate;
import com.liferay.permission.template.model.PermissionTemplateTable;
import com.liferay.permission.template.service.persistence.PermissionTemplatePersistence;
import com.liferay.portal.kernel.service.persistence.BasePersistence;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Quan Huynh
 */
@Component(service = TableReferenceDefinition.class)
public class PermissionTemplateTableReferenceDefinition
	implements TableReferenceDefinition<PermissionTemplateTable> {

	@Override
	public void defineChildTableReferences(
		ChildTableReferenceInfoBuilder<PermissionTemplateTable>
			childTableReferenceInfoBuilder) {

		childTableReferenceInfoBuilder.resourcePermissionReference(
			PermissionTemplateTable.INSTANCE.permissionTemplateId,
			PermissionTemplate.class);
	}

	@Override
	public void defineParentTableReferences(
		ParentTableReferenceInfoBuilder<PermissionTemplateTable>
			parentTableReferenceInfoBuilder) {
	}

	@Override
	public BasePersistence<?> getBasePersistence() {
		return _permissionTemplatePersistence;
	}

	@Override
	public PermissionTemplateTable getTable() {
		return PermissionTemplateTable.INSTANCE;
	}

	@Reference
	private PermissionTemplatePersistence _permissionTemplatePersistence;

}