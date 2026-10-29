/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.fragment.renderer;

import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.DefaultFragmentRendererContext;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.fragment.renderer.FragmentRendererContext;
import com.liferay.fragment.renderer.FragmentRendererRegistry;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.petra.io.unsync.UnsyncStringWriter;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.license.util.App;
import com.liferay.portal.kernel.license.util.LicenseManagerUtil;
import com.liferay.portal.kernel.servlet.PipingServletResponse;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.site.cms.site.initializer.internal.display.context.ViewTasksDisplayContext;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jan Brychta
 */
@Component(service = FragmentRenderer.class)
public class ViewTasksJSPSectionFragmentRenderer
	extends BaseJSPSectionFragmentRenderer<ViewTasksDisplayContext> {

	@Override
	public String getLabelKey() {
		return "tasks";
	}

	@Override
	public void render(
			FragmentRendererContext fragmentRendererContext,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		try {
			RequestDispatcher requestDispatcher =
				servletContext.getRequestDispatcher(getJSPPath());

			ViewTasksDisplayContext viewTasksDisplayContext = getDisplayContext(
				httpServletRequest);

			if (LicenseManagerUtil.isAppEnabled(App.CMP)) {
				Map<String, String> cmpTasksHTML = _getCMPTasksHTML(
					httpServletRequest, httpServletResponse);

				if (cmpTasksHTML != null) {
					viewTasksDisplayContext.setCMPTasksHTML(cmpTasksHTML);
				}
			}

			httpServletRequest.setAttribute(
				ViewTasksDisplayContext.class.getName(),
				viewTasksDisplayContext);

			requestDispatcher.include(httpServletRequest, httpServletResponse);
		}
		catch (IOException | RuntimeException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new IOException(exception);
		}
	}

	@Override
	protected ViewTasksDisplayContext getDisplayContext(
		HttpServletRequest httpServletRequest) {

		return new ViewTasksDisplayContext(httpServletRequest);
	}

	@Override
	protected String getJSPPath() {
		return "/view_tasks.jsp";
	}

	private Map<String, String> _getCMPTasksHTML(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		FragmentRenderer allTasksFragmentRenderer =
			_fragmentRendererRegistry.getFragmentRenderer(
				_ALL_TASKS_FRAGMENT_RENDERER_KEY);

		if (allTasksFragmentRenderer == null) {
			return null;
		}

		return HashMapBuilder.put(
			ViewTasksDisplayContext.ALL_TASKS,
			_renderFragment(
				_ALL_TASKS_FRAGMENT_RENDERER_KEY, httpServletRequest,
				httpServletResponse)
		).put(
			ViewTasksDisplayContext.PROJECT_TASKS,
			_renderFragment(
				_PROJECT_TASKS_FRAGMENT_RENDERER_KEY, httpServletRequest,
				httpServletResponse)
		).put(
			ViewTasksDisplayContext.WORKFLOW_TASKS,
			_renderFragment(
				_WORKFLOW_TASKS_FRAGMENT_RENDERER_KEY, httpServletRequest,
				httpServletResponse)
		).put(
			ViewTasksDisplayContext.WORKFLOW_TASKS_OVERVIEW,
			_renderFragment(
				_WORKFLOW_TASKS_OVERVIEW_FRAGMENT_RENDERER_KEY,
				httpServletRequest, httpServletResponse)
		).build();
	}

	private String _renderFragment(
			String key, HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		FragmentRenderer fragmentRenderer =
			_fragmentRendererRegistry.getFragmentRenderer(key);

		if (fragmentRenderer == null) {
			return StringPool.BLANK;
		}

		FragmentEntryLink fragmentEntryLink =
			_fragmentEntryLinkLocalService.createFragmentEntryLink(0);

		UnsyncStringWriter unsyncStringWriter = new UnsyncStringWriter();

		fragmentRenderer.render(
			new DefaultFragmentRendererContext(fragmentEntryLink),
			httpServletRequest,
			new PipingServletResponse(httpServletResponse, unsyncStringWriter));

		return unsyncStringWriter.toString();
	}

	private static final String _ALL_TASKS_FRAGMENT_RENDERER_KEY =
		"com.liferay.content.marketing.platform.web.internal.fragment." +
			"renderer.ViewAllTasksJSPSectionFragmentRenderer";

	private static final String _PROJECT_TASKS_FRAGMENT_RENDERER_KEY =
		"com.liferay.content.marketing.platform.web.internal.fragment." +
			"renderer.ViewProjectTasksJSPSectionFragmentRenderer";

	private static final String _WORKFLOW_TASKS_FRAGMENT_RENDERER_KEY =
		"com.liferay.content.marketing.platform.web.internal.fragment." +
			"renderer.ViewWorkflowTasksJSPSectionFragmentRenderer";

	private static final String _WORKFLOW_TASKS_OVERVIEW_FRAGMENT_RENDERER_KEY =
		"com.liferay.content.marketing.platform.web.internal.fragment." +
			"renderer.WorkflowTasksOverviewComponentSectionFragmentRenderer";

	@Reference
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Reference
	private FragmentRendererRegistry _fragmentRendererRegistry;

}