/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.fragment.renderer;

import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.fragment.renderer.FragmentRendererContext;
import com.liferay.layout.display.page.LayoutDisplayPageObjectProvider;
import com.liferay.layout.display.page.constants.LayoutDisplayPageWebKeys;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.cms.site.initializer.internal.util.InfoItemUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Sandro Chinea
 */
@Component(service = FragmentRenderer.class)
public class ContentEditorToolbarComponentSectionFragmentRenderer
	extends BaseComponentSectionFragmentRenderer {

	@Override
	public String getCollectionKey() {
		return "content-editor";
	}

	@Override
	public void render(
			FragmentRendererContext fragmentRendererContext,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if ((themeDisplay != null) && themeDisplay.isStatePopUp()) {
			return;
		}

		super.render(
			fragmentRendererContext, httpServletRequest, httpServletResponse);
	}

	@Override
	protected String getLabelKey() {
		return "content-editor-management-bar";
	}

	@Override
	protected String getModuleName() {
		return "ContentEditorToolbar";
	}

	@Override
	protected Map<String, Object> getProps(
		FragmentRendererContext fragmentRendererContext,
		HttpServletRequest httpServletRequest) {

		boolean readOnly = Objects.equals(
			ParamUtil.getString(httpServletRequest, "p_l_mode", Constants.VIEW),
			Constants.READ);

		HashMapBuilder.HashMapWrapper<String, Object> hashMapWrapper =
			HashMapBuilder.<String, Object>put(
				"backURL", ParamUtil.getString(httpServletRequest, "redirect")
			).put(
				"readOnly", readOnly
			);

		LayoutDisplayPageObjectProvider<?> layoutDisplayPageObjectProvider =
			(LayoutDisplayPageObjectProvider<?>)httpServletRequest.getAttribute(
				LayoutDisplayPageWebKeys.LAYOUT_DISPLAY_PAGE_OBJECT_PROVIDER);

		if (layoutDisplayPageObjectProvider == null) {
			return hashMapWrapper.build();
		}

		Object displayObject =
			layoutDisplayPageObjectProvider.getDisplayObject();

		if (!(displayObject instanceof ObjectEntry)) {
			return hashMapWrapper.build();
		}

		ObjectEntry objectEntry = (ObjectEntry)displayObject;

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.fetchObjectDefinition(
				objectEntry.getObjectDefinitionId());

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		boolean hasUpdatePermission = _hasUpdatePermission(
			objectEntry, themeDisplay);

		hashMapWrapper.put("readOnly", readOnly || !hasUpdatePermission);

		return hashMapWrapper.put(
			"displayDate",
			() -> {
				Date displayDate = objectEntry.getDisplayDate();

				if (displayDate == null) {
					return null;
				}

				return DateUtil.getDate(
					displayDate, "yyyy-MM-dd'T'HH:mm",
					themeDisplay.getLocale());
			}
		).put(
			"hasWorkflow",
			() -> {
				if (objectDefinition == null) {
					return null;
				}

				return _workflowDefinitionLinkLocalService.
					hasWorkflowDefinitionLink(
						themeDisplay.getCompanyId(),
						InfoItemUtil.getGroupId(httpServletRequest),
						objectDefinition.getClassName());
			}
		).put(
			"headerTitle",
			() -> {
				String title = _getTitle(
					layoutDisplayPageObjectProvider, objectEntry, themeDisplay);

				if (readOnly || !hasUpdatePermission) {
					return language.format(
						themeDisplay.getLocale(), "view-x", title);
				}

				Layout layout = themeDisplay.getLayout();

				LayoutPageTemplateEntry layoutPageTemplateEntry =
					_layoutPageTemplateEntryLocalService.
						fetchLayoutPageTemplateEntryByPlid(layout.getPlid());

				String layoutPageTemplateEntryKey =
					layoutPageTemplateEntry.getLayoutPageTemplateEntryKey();

				if (layoutPageTemplateEntryKey.startsWith(
						"LFR_CMS_TRANSLATION_")) {

					return language.format(
						themeDisplay.getLocale(), "translate-x", title);
				}

				if (objectEntry.getVersion() > 0) {
					return language.format(
						themeDisplay.getLocale(), "edit-x", title);
				}

				return language.format(
					themeDisplay.getLocale(), "new-x", title);
			}
		).put(
			"type",
			() -> {
				if (objectDefinition == null) {
					return null;
				}

				return objectDefinition.getLabel(themeDisplay.getLocale());
			}
		).build();
	}

	private String _getTitle(
		LayoutDisplayPageObjectProvider<?> layoutDisplayPageObjectProvider,
		ObjectEntry objectEntry, ThemeDisplay themeDisplay) {

		String title = layoutDisplayPageObjectProvider.getTitle(
			themeDisplay.getLocale());

		if (Validator.isNull(title)) {
			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.fetchObjectDefinition(
					objectEntry.getObjectDefinitionId());

			return objectDefinition.getLabel(themeDisplay.getLocale());
		}

		return title;
	}

	private boolean _hasUpdatePermission(
		ObjectEntry objectEntry, ThemeDisplay themeDisplay) {

		try {
			ModelResourcePermission<ObjectEntry> modelResourcePermission =
				_objectEntryService.getModelResourcePermission(
					objectEntry.getObjectDefinitionId());

			return modelResourcePermission.contains(
				themeDisplay.getPermissionChecker(), objectEntry,
				ActionKeys.UPDATE);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}

			return false;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ContentEditorToolbarComponentSectionFragmentRenderer.class);

	@Reference
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryService _objectEntryService;

	@Reference
	private WorkflowDefinitionLinkLocalService
		_workflowDefinitionLinkLocalService;

}