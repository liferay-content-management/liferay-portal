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

	<c:choose>
		<c:when test="<%= viewTasksDisplayContext.isCMPEnabled() %>">
			<ul class="nav nav-underline" id="tasksSectionTabs" role="tablist">
				<li class="nav-item" role="presentation">
					<button aria-controls="tasksSectionTabPanelAllTasks" class="active nav-link" data-target="tasksSectionTabPanelAllTasks" role="tab" type="button">
						<liferay-ui:message key="all-tasks" />
					</button>
				</li>
				<li class="nav-item" role="presentation">
					<button aria-controls="tasksSectionTabPanelProjectTasks" class="nav-link" data-target="tasksSectionTabPanelProjectTasks" role="tab" type="button">
						<liferay-ui:message key="project-tasks" />
					</button>
				</li>
				<li class="nav-item" role="presentation">
					<button aria-controls="tasksSectionTabPanelWorkflowTasks" class="nav-link" data-target="tasksSectionTabPanelWorkflowTasks" role="tab" type="button">
						<liferay-ui:message key="workflow-tasks" />
					</button>
				</li>
			</ul>

			<div class="tab-content">
				<div class="active cms-section tab-pane" id="tasksSectionTabPanelAllTasks" role="tabpanel">
					<%= viewTasksDisplayContext.getAllTasksHTML() %>
				</div>

				<div class="cms-section tab-pane" id="tasksSectionTabPanelProjectTasks" role="tabpanel">
					<%= viewTasksDisplayContext.getProjectTasksHTML() %>
				</div>

				<div class="cms-section tab-pane" id="tasksSectionTabPanelWorkflowTasks" role="tabpanel">
					<%= viewTasksDisplayContext.getWorkflowTasksOverviewHTML() %>

					<%= viewTasksDisplayContext.getWorkflowTasksHTML() %>
				</div>
			</div>

			<aui:script>
				(function () {
					var tabs = document.querySelectorAll('#tasksSectionTabs .nav-link');

					Array.prototype.forEach.call(tabs, function (tab) {
						tab.addEventListener('click', function () {
							Array.prototype.forEach.call(tabs, function (otherTab) {
								otherTab.classList.remove('active');
							});

							tab.classList.add('active');

							var panels = document.querySelectorAll(
								'#tasksSectionTabs ~ .tab-content .tab-pane'
							);

							Array.prototype.forEach.call(panels, function (panel) {
								panel.classList.remove('active');
							});

							document
								.getElementById(tab.getAttribute('data-target'))
								.classList.add('active');
						});
					});
				})();
			</aui:script>
		</c:when>
		<c:otherwise>
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