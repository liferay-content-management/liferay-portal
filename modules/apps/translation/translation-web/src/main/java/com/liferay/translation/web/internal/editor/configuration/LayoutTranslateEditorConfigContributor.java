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

package com.liferay.translation.web.internal.editor.configuration;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.editor.configuration.BaseEditorConfigContributor;
import com.liferay.portal.kernel.editor.configuration.EditorConfigContributor;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.translation.constants.TranslationPortletKeys;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jorge González
 */
@Component(
	property = {
		"editor.config.key=layoutTranslateEditor",
		"javax.portlet.name=" + TranslationPortletKeys.TRANSLATION
	},
	service = EditorConfigContributor.class
)
public class LayoutTranslateEditorConfigContributor
	extends BaseEditorConfigContributor {

	@Override
	public void populateConfigJSONObject(
		JSONObject jsonObject, Map<String, Object> inputEditorTaglibAttributes,
		ThemeDisplay themeDisplay,
		RequestBackedPortletURLFactory requestBackedPortletURLFactory) {

		jsonObject.put(
			"allowedContent",
			"b code em h1 h2 h3 h4 h5 h6 hr i p pre strong u [*](*){*}; " +
			"a[*](*); div[*](*){text-align}; img[*](*){*}; p[*](*); span[*](*){*};" +
			"li ol ul [*](*){*};" +
			"table[border, cellpadding, cellspacing] {width}; tbody td " +
			"th[scope]; thead tr[scope];"
		).put(
			"enterMode", 2
		).put(
			"extraPlugins",
			"addimages,autolink,filebrowser,itemselector,lfrpopup"
		).put(
			"height", "265"
		).put(
			"removePlugins",
			"autogrow,elementspath,floatingspace,magicline,resize,ae_embed"
		).put(
			"resize_enabled", false
		).put(
			"toolbar", _TOOLBAR_JSON_ARRAY
		);
	}

	private static final JSONArray _TOOLBAR_JSON_ARRAY = JSONUtil.putAll(
		JSONUtil.putAll("Undo", "Redo"),
		JSONUtil.putAll("Bold", "Italic", "Underline"),
		JSONUtil.putAll("NumberedList", "BulletedList"),
		JSONUtil.putAll(
			"JustifyLeft", "JustifyCenter", "JustifyRight", "JustifyBlock"),
		JSONUtil.putAll("Link", "Unlink"),
		JSONUtil.putAll("Table", "ImageSelector", "HorizontalRule"),
		JSONUtil.putAll("RemoveFormat"), JSONUtil.putAll("Source", "Expand"));

}
