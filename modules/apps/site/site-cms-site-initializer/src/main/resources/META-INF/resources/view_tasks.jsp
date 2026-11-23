<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
ViewTasksDisplayContext viewTasksDisplayContext = (ViewTasksDisplayContext)request.getAttribute(ViewTasksDisplayContext.class.getName());
%>

<div>
	<div>
		<react:component
			module="{Breadcrumb} from site-cms-site-initializer"
			props="<%= viewTasksDisplayContext.getBreadcrumbProps() %>"
		/>
	</div>

	<c:choose>
		<c:when test="<%= viewTasksDisplayContext.isCMPEnabled() %>">
			<div class="component-tabs">
				<div>
					<react:component
						module="{TasksTabs} from site-cms-site-initializer"
					/>
				</div>

				<div class="tab-panel-item" hidden id="cmsAllTasksPanel">
					<%= viewTasksDisplayContext.getAllTasksHTML() %>
				</div>

				<div class="tab-panel-item" hidden id="cmsProjectTasksPanel">
					<%= viewTasksDisplayContext.getProjectTasksHTML() %>
				</div>

				<div class="tab-panel-item" hidden id="cmsWorkflowTasksPanel">
					<%= viewTasksDisplayContext.getWorkflowTasksOverviewHTML() %>

					<%= viewTasksDisplayContext.getWorkflowTasksHTML() %>
				</div>
			</div>
		</c:when>
		<c:otherwise>
			<c:if test="<%= viewTasksDisplayContext.isCMPMarketplaceBannerVisible() %>">
				<div>
					<react:component
						module="{CMPMarketplaceBanner} from site-cms-site-initializer"
						props="<%= viewTasksDisplayContext.getCMPMarketplaceBannerProps() %>"
					/>
				</div>
			</c:if>

			<div>
				<react:component
					module="{TasksQuickFilters} from site-cms-site-initializer"
					props="<%= viewTasksDisplayContext.getTasksQuickFiltersProps() %>"
				/>
			</div>

			<div class="cms-section custom-empty-state">
				<frontend-data-set:headless-display
					apiURL="<%= viewTasksDisplayContext.getAPIURL() %>"
					emptyState="<%= viewTasksDisplayContext.getEmptyState() %>"
					fdsActionDropdownItems="<%= viewTasksDisplayContext.getFDSActionDropdownItems() %>"
					formName="fm"
					id="<%= CMSSiteInitializerFDSNames.TASKS_SECTION %>"
					itemsPerPage="<%= 20 %>"
					propsTransformer="{TasksFDSPropsTransformer} from site-cms-site-initializer"
					uniformActionsDisplay="<%= true %>"
				/>
			</div>
		</c:otherwise>
	</c:choose>
</div>