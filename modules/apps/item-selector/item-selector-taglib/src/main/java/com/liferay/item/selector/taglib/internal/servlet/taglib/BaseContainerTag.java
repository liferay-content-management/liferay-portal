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

package com.liferay.item.selector.taglib.internal.servlet.taglib;

import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.item.selector.taglib.internal.js.loader.modules.extender.npm.NPMResolverProvider;
import com.liferay.item.selector.taglib.internal.util.ServicesProvider;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.template.react.renderer.ComponentDescriptor;
import com.liferay.portal.template.react.renderer.ReactRenderer;
import com.liferay.taglib.util.AttributesTagSupport;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * @author Carlos Lancha
 */
public class BaseContainerTag extends AttributesTagSupport {

	@Override
	public int doEndTag() throws JspException {
		try {
			return processEndTag();
		}
		catch (Exception exception) {
			throw new JspException(exception);
		}
		finally {
			doClearTag();
		}
	}

	@Override
	public int doStartTag() throws JspException {
		try {
			return processStartTag();
		}
		catch (Exception exception) {
			throw new JspException(exception);
		}
	}

	public String getContainerElement() {
		return _containerElement;
	}

	public String getCssClass() {
		return _cssClass;
	}

	public String getHydratedContainerElement() {
		return _hydratedContainerElement;
	}

	public String getId() {
		return _id;
	}

	public void setContainerElement(String containerElement) {
		_containerElement = containerElement;
	}

	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	public void setHydratedContainerElement(String hydratedContainerElement) {
		_hydratedContainerElement = hydratedContainerElement;
	}

	public void setId(String id) {
		_id = id;
	}

	protected void cleanUp() {
		_containerElement = null;
		_cssClass = null;
		_hydratedContainerElement = "div";
		_id = null;
	}

	protected void doClearTag() {
		clearDynamicAttributes();
		clearParams();
		clearProperties();

		cleanUp();
	}

	protected String getHydratedModuleName() {
		return null;
	}

	protected Map<String, Object> prepareProps(Map<String, Object> props) {
		props.put("cssClass", getCssClass());
		props.put("id", getId());

		return props;
	}

	protected String processCssClasses(Set<String> cssClasses) {
		String cssClass = getCssClass();

		if (Validator.isNotNull(cssClass)) {
			cssClasses.addAll(StringUtil.split(cssClass, CharPool.SPACE));
		}

		return StringUtil.merge(cssClasses, StringPool.SPACE);
	}

	protected int processEndTag() throws Exception {
		JspWriter jspWriter = pageContext.getOut();

		jspWriter.write("</");
		jspWriter.write(_containerElement);
		jspWriter.write(">");

		String hydratedModuleName = getHydratedModuleName();

		if (hydratedModuleName != null) {
			NPMResolver npmResolver = NPMResolverProvider.getNPMResolver();

			String moduleName = npmResolver.resolveModuleName(
				hydratedModuleName);

			ComponentDescriptor componentDescriptor = new ComponentDescriptor(
				moduleName, getId(), new LinkedHashSet<>(), false);

			ReactRenderer reactRenderer = ServicesProvider.getReactRenderer();

			reactRenderer.renderReact(
				componentDescriptor, prepareProps(new HashMap<>()), request,
				jspWriter);

			jspWriter.write("</");
			jspWriter.write(_hydratedContainerElement);
			jspWriter.write(">");
		}

		return EVAL_PAGE;
	}

	protected int processStartTag() throws Exception {
		JspWriter jspWriter = pageContext.getOut();

		if (getHydratedModuleName() != null) {
			jspWriter.write("<");
			jspWriter.write(_hydratedContainerElement);
			jspWriter.write(">");
		}

		if (_containerElement == null) {
			setContainerElement("div");
		}

		jspWriter.write("<");
		jspWriter.write(_containerElement);

		writeCssClassAttribute();

		if (Validator.isNotNull(getId())) {
			writeIdAttribute();
		}

		jspWriter.write(">");

		return EVAL_BODY_INCLUDE;
	}

	protected void writeCssClassAttribute() throws Exception {
		JspWriter jspWriter = pageContext.getOut();

		jspWriter.write(" class=\"");
		jspWriter.write(processCssClasses(new LinkedHashSet<>()));
		jspWriter.write("\"");
	}

	protected void writeIdAttribute() throws Exception {
		JspWriter jspWriter = pageContext.getOut();

		jspWriter.write(" id=\"");
		jspWriter.write(getId());
		jspWriter.write("\"");
	}

	private String _containerElement;
	private String _cssClass;
	private String _hydratedContainerElement = "div";
	private String _id;

}