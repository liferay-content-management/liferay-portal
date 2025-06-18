<%--
/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
ViewSpaceMembersAbstractSectionDisplayContext viewSpaceMembersAbstractSectionDisplayContext = (ViewSpaceMembersAbstractSectionDisplayContext)request.getAttribute(ViewSpaceMembersAbstractSectionDisplayContext.class.getName());
%>

<div class="cms-section">
	<div id="<%= CMSSiteInitializerFDSNames.SPACE_MEMBERS_ABSTRACT_SECTION %>">
		<react:component
			module="{SpaceAbstractHeader} from site-cms-site-initializer"
			props="<%= viewSpaceMembersAbstractSectionDisplayContext.getHeaderProps() %>"
		/>
	</div>

	<clay:tabs
		tabsItems="<%= viewSpaceMembersAbstractSectionDisplayContext.getTabsItems() %>"
	>
		<clay:tabs-panel>
			<div class="cms-section custom-empty-state">
				<frontend-data-set:headless-display
					apiURL='<%= viewSpaceMembersAbstractSectionDisplayContext.getAPIURL("user-accounts") %>'
					fdsActionDropdownItems='<%= viewSpaceMembersAbstractSectionDisplayContext.getFDSActionDropdownItems("user-accounts") %>'
					formName="fm"
					id="<%= CMSSiteInitializerFDSNames.SPACE_MEMBERS_USERS_ABSTRACT_SECTION %>"
					showManagementBar="<%= false %>"
					showPagination="<%= false %>"
					showSearch="<%= false %>"
					showSelectAll="<%= false %>"
					style="fluid"
				/>
			</div>
		</clay:tabs-panel>

		<c:if test="<%= viewSpaceMembersAbstractSectionDisplayContext.hasUserGroups() %>">
			<clay:tabs-panel>
				<div class="cms-section custom-empty-state">
					<frontend-data-set:headless-display
						apiURL='<%= viewSpaceMembersAbstractSectionDisplayContext.getAPIURL("user-groups") %>'
						fdsActionDropdownItems='<%= viewSpaceMembersAbstractSectionDisplayContext.getFDSActionDropdownItems("user-groups") %>'
						formName="fm"
						id="<%= CMSSiteInitializerFDSNames.SPACE_MEMBERS_USER_GROUPS_ABSTRACT_SECTION %>"
						showManagementBar="<%= false %>"
						showPagination="<%= false %>"
						showSearch="<%= false %>"
						showSelectAll="<%= false %>"
						style="fluid"
					/>
				</div>
			</clay:tabs-panel>
		</c:if>
	</clay:tabs>
</div>