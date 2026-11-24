/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.fragment.renderer;

import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.fragment.renderer.FragmentRendererContext;
import com.liferay.info.constants.InfoDisplayWebKeys;
import com.liferay.object.entry.folder.subscription.util.ObjectEntryFolderSubscriptionUtil;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.subscription.service.SubscriptionLocalService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Ambrín Chaudhary
 */
@Component(service = FragmentRenderer.class)
public class EditFolderComponentSectionFragmentRenderer
	extends BaseComponentSectionFragmentRenderer {

	@Override
	public String getCollectionKey() {
		return "sections";
	}

	@Override
	protected String getComponentName() {
		return "EditFolder";
	}

	@Override
	protected String getLabelKey() {
		return "edit-folder";
	}

	@Override
	protected Map<String, Object> getProps(
			FragmentRendererContext fragmentRendererContext,
			HttpServletRequest httpServletRequest)
		throws Exception {

		Object object = httpServletRequest.getAttribute(
			InfoDisplayWebKeys.INFO_ITEM);

		ObjectEntryFolder objectEntryFolder =
			object instanceof ObjectEntryFolder ? (ObjectEntryFolder)object :
				null;

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return HashMapBuilder.<String, Object>put(
			"backURL",
			PortalUtil.escapeRedirect(
				ParamUtil.getString(httpServletRequest, "redirect"))
		).put(
			"folderId",
			() -> {
				if (objectEntryFolder == null) {
					return null;
				}

				return objectEntryFolder.getObjectEntryFolderId();
			}
		).put(
			"isSubscribed",
			() -> {
				if (objectEntryFolder == null) {
					return false;
				}

				return _subscriptionLocalService.isSubscribed(
					themeDisplay.getCompanyId(), themeDisplay.getUserId(),
					ObjectEntryFolder.class.getName(),
					objectEntryFolder.getObjectEntryFolderId());
			}
		).put(
			"subscribedToParentFolder",
			() -> {
				if (objectEntryFolder == null) {
					return false;
				}

				return !_subscriptionLocalService.isSubscribed(
					themeDisplay.getCompanyId(), themeDisplay.getUserId(),
					ObjectEntryFolder.class.getName(),
					objectEntryFolder.getObjectEntryFolderId()) &&
					   ObjectEntryFolderSubscriptionUtil.
						   isSubscribedToObjectEntryFolder(
							   themeDisplay.getCompanyId(),
							   objectEntryFolder.getGroupId(),
							   objectEntryFolder.getObjectEntryFolderId(),
							   themeDisplay.getUserId());
			}
		).put(
			"subscribeURL",
			() -> {
				if (objectEntryFolder == null) {
					return null;
				}

				return StringBundler.concat(
					themeDisplay.getPortalURL(), themeDisplay.getPathMain(),
					GroupConstants.CMS_FRIENDLY_URL,
					"/subscribe_folder?classPK=",
					objectEntryFolder.getObjectEntryFolderId());
			}
		).build();
	}

	@Reference
	private SubscriptionLocalService _subscriptionLocalService;

}