/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.comment.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Roberto Díaz
 */
@ExtendedObjectClassDefinition(
	category = "comments", scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
	id = "com.liferay.comment.configuration.CommentCompanyConfiguration",
	localization = "content/Language", name = "discussion-configuration-name"
)
@ProviderType
public interface CommentCompanyConfiguration {

	@Meta.AD(
		deflt = "false", description = "always-editable-by-owner-description",
		name = "always-editable-by-owner", required = false
	)
	public boolean alwaysEditableByOwner();

}