<%--
/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
LockedItemsDisplayContext lockedItemsDisplayContext = (LockedItemsDisplayContext)request.getAttribute(LockedItemsDisplayContext.class.getName());

LockedItemsScreen lockedItemsScreen = lockedItemsDisplayContext.getLockedItemsScreen();
%>

<clay:container-fluid
	cssClass="container-view"
>
	<clay:row>
		<c:if test='<%= FeatureFlagManagerUtil.isEnabled("LPD-11003") %>'>
			<clay:col
				lg="3"
			>
				<clay:vertical-nav
					verticalNavItems="<%= lockedItemsDisplayContext.getVerticalNavItemList() %>"
				/>
			</clay:col>
		</c:if>

		<clay:col
			lg="9"
		>
			<clay:sheet
				cssClass="full"
			>
				<clay:sheet-header>
					<p class="sheet-title"><%= lockedItemsScreen.getName(locale) %></p>
					<p class="sheet-text"><%= lockedItemsScreen.getDescription(locale) %></p>
				</clay:sheet-header>

				<liferay-util:include page="<%= lockedItemsScreen.getJspPath() %>" servletContext="<%= lockedItemsScreen.getServletContext() %>" />
			</clay:sheet>
		</clay:col>
	</clay:row>
</clay:container-fluid>