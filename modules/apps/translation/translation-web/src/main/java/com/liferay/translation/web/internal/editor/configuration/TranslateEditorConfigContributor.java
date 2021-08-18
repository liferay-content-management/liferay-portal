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

import com.liferay.portal.kernel.editor.configuration.BaseEditorConfigContributor;
import com.liferay.portal.kernel.editor.configuration.EditorConfigContributor;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.translation.constants.TranslationPortletKeys;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jorge González
 */
@Component(
	property = {
		"editor.config.key=translateEditor",
		"javax.portlet.name=" + TranslationPortletKeys.TRANSLATION
	},
	service = EditorConfigContributor.class
)
public class TranslateEditorConfigContributor
	extends BaseEditorConfigContributor {

	@Override
	public void populateConfigJSONObject(
		JSONObject jsonObject, Map<String, Object> inputEditorTaglibAttributes,
		ThemeDisplay themeDisplay,
		RequestBackedPortletURLFactory requestBackedPortletURLFactory) {

		StringBundler sb = new StringBundler(5);

		sb.append(getAllowedContentText());
		sb.append(" a[*](*); div[*](*){text-align}; img[*](*){*}; p[*](*); ");
		sb.append(getAllowedContentLists());
		sb.append(getAllowedContentTable());
		sb.append(" span[*](*){*}; ");

		jsonObject.put(
			"allowedContent", sb.toString()
		).put(
			"enterMode", 2
		).put(
			"extraPlugins", getExtraPluginsLists()
		).put(
			"height", "265"
		).put(
			"removePlugins", getRemovePluginsLists()
		).put(
			"resize_enabled", false
		).put(
			"toolbar", getToolbarJSONArray()
		);
	}

	protected String getAllowedContentLists() {
		return "li ol ul [*](*){*};";
	}

	protected String getAllowedContentTable() {
		return "table[border, cellpadding, cellspacing] {width}; tbody td " +
			"th[scope]; thead tr[scope];";
	}

	protected String getAllowedContentText() {
		return "b code em h1 h2 h3 h4 h5 h6 hr i p pre strong u [*](*){*};";
	}

	protected String getExtraPluginsLists() {
		return "addimages,autolink,filebrowser,itemselector,lfrpopup";
	}

	protected String getRemovePluginsLists() {
		return "autogrow,elementspath,floatingspace," +
			"magicline,resize,ae_embed";
	}

	protected JSONArray getToolbarJSONArray() {
		return JSONUtil.putAll(
			toJSONArray("['Undo', 'Redo']"),
			toJSONArray("['Bold', 'Italic', 'Underline']"),
			toJSONArray("['NumberedList', 'BulletedList']"),
			toJSONArray(
				"['JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock']"),
			toJSONArray("['Link', Unlink]"),
			toJSONArray("['Table', 'ImageSelector','HorizontalRule']"),
			toJSONArray("['RemoveFormat']"),
			toJSONArray("['Source', 'Expand']"));
	}

}