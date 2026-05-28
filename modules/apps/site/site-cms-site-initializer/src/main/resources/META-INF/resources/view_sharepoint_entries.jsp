<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
ViewSharepointEntriesSectionDisplayContext viewSharepointEntriesSectionDisplayContext = (ViewSharepointEntriesSectionDisplayContext)request.getAttribute(ViewSharepointEntriesSectionDisplayContext.class.getName());
%>

<div class="cms-section">
	<h2 class="mb-0 py-2 text-6 text-dark"><liferay-ui:message key="sharepoint-entries" /></h2>

	<c:if test="<%= viewSharepointEntriesSectionDisplayContext.isNotConfigured() %>">
		<div class="px-4 py-5 text-center">
			<p class="mb-3"><liferay-ui:message key="this-sharepoint-space-is-not-configured-yet" /></p>

			<a class="btn btn-primary" href="<%= viewSharepointEntriesSectionDisplayContext.getSpaceSettingsURL() %>">
				<liferay-ui:message key="open-space-settings" />
			</a>
		</div>
	</c:if>

	<c:if test="<%= viewSharepointEntriesSectionDisplayContext.isNotAuthenticated() %>">
		<div class="px-4 py-5 text-center">
			<p class="mb-3"><liferay-ui:message key="sign-in-to-sharepoint-to-view-files" /></p>

			<a class="btn btn-primary" href="<%= viewSharepointEntriesSectionDisplayContext.getConnectURL() %>">
				<liferay-ui:message key="connect-to-sharepoint" />
			</a>
		</div>
	</c:if>

	<c:if test="<%= viewSharepointEntriesSectionDisplayContext.isAuthenticated() %>">
		<div class="cms-fds-fluid">
			<frontend-data-set:headless-display
				apiURL="<%= viewSharepointEntriesSectionDisplayContext.getAPIURL() %>"
				formName="fm"
				id="<%= CMSSiteInitializerFDSNames.SPACE_SHAREPOINT_ENTRIES_SECTION %>"
				propsTransformer="{SharepointEntriesFDSPropsTransformer} from site-cms-site-initializer"
				showManagementBar="<%= false %>"
				showPagination="<%= false %>"
				showSearch="<%= false %>"
				showSelectAll="<%= false %>"
				style="fluid"
			/>
		</div>
	</c:if>
</div>