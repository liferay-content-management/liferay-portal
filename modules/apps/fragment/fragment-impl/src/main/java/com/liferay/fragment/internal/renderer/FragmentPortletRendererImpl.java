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

package com.liferay.fragment.internal.renderer;

import com.liferay.fragment.constants.FragmentWebKeys;
import com.liferay.fragment.exception.FragmentEntryContentException;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.FragmentPortletRenderer;
import com.liferay.petra.io.unsync.UnsyncStringWriter;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.portlet.PortletJSONUtil;
import com.liferay.portal.kernel.portlet.constants.PortletPreferencesFactoryConstants;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.servlet.PipingServletResponse;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.taglib.portletext.RuntimeTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pavel Savinov
 */
@Component(immediate = true, service = FragmentPortletRenderer.class)
public class FragmentPortletRendererImpl implements FragmentPortletRenderer {

	@Override
	public String renderPortlet(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String portletName,
			String instanceId, String defaultPreferences)
		throws PortalException {

		UnsyncStringWriter unsyncStringWriter = new UnsyncStringWriter();

		PipingServletResponse pipingServletResponse = new PipingServletResponse(
			httpServletResponse, unsyncStringWriter);

		boolean inheritedFromMaster = false;

		FragmentEntryLink fragmentEntryLink =
			(FragmentEntryLink)httpServletRequest.getAttribute(
				FragmentWebKeys.FRAGMENT_ENTRY_LINK);

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if ((fragmentEntryLink != null) && (themeDisplay != null) &&
			(fragmentEntryLink.getPlid() != themeDisplay.getPlid())) {

			inheritedFromMaster = true;
		}

		_writeHeaderPaths(
			portletName, themeDisplay, httpServletRequest, httpServletResponse);

		try {
			RuntimeTag.doTag(
				portletName, instanceId, StringPool.BLANK,
				PortletPreferencesFactoryConstants.
					SETTINGS_SCOPE_PORTLET_INSTANCE,
				defaultPreferences, inheritedFromMaster, null,
				httpServletRequest, pipingServletResponse);
		}
		catch (Exception exception) {
			throw new FragmentEntryContentException(exception);
		}

		return unsyncStringWriter.toString();
	}

	private void _writeHeaderPaths(
		String portletInstanceKey, ThemeDisplay themeDisplay,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		Portlet portlet = _portletLocalService.getPortletById(
			themeDisplay.getCompanyId(), portletInstanceKey);

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		try {
			PortletJSONUtil.populatePortletJSONObject(
				httpServletRequest, StringPool.BLANK, portlet, jsonObject);

			PortletJSONUtil.writeHeaderPaths(httpServletResponse, jsonObject);
		}
		catch (Exception exception) {
			_log.error(exception);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FragmentPortletRendererImpl.class);

	@Reference
	private PortletLocalService _portletLocalService;

}