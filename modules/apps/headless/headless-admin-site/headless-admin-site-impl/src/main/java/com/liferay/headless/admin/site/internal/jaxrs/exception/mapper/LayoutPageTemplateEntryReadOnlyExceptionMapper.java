/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.jaxrs.exception.mapper;

import com.liferay.layout.page.template.exception.LayoutPageTemplateEntryReadOnlyException;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.BaseExceptionMapper;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.Problem;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jürgen Kappler
 */
@Component(
	property = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.Headless.Admin.Site)",
		"osgi.jaxrs.extension=true",
		"osgi.jaxrs.name=Liferay.Headless.Admin.Site.LayoutPageTemplateEntryReadOnlyExceptionMapper"
	},
	service = ExceptionMapper.class
)
public class LayoutPageTemplateEntryReadOnlyExceptionMapper
	extends BaseExceptionMapper<LayoutPageTemplateEntryReadOnlyException> {

	@Override
	protected Problem getProblem(
		LayoutPageTemplateEntryReadOnlyException
			layoutPageTemplateEntryReadOnlyException) {

		return new Problem(
			Response.Status.BAD_REQUEST,
			"Read only is only supported in a design library");
	}

}