<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
LocalizedItemSelectorRendering localizedItemSelectorRendering = LocalizedItemSelectorRendering.get(liferayPortletRequest);

List<NavigationItem> navigationItems = localizedItemSelectorRendering.getNavigationItems();
%>

<c:choose>
	<c:when test="<%= navigationItems.isEmpty() %>">

		<%
		if (_log.isWarnEnabled()) {
			String[] criteria = ParamUtil.getParameterValues(renderRequest, "criteria");

			_log.warn("No item selector views found for " + StringUtil.merge(criteria, StringPool.COMMA_AND_SPACE));
		}
		%>

		<div class="alert alert-info">
			<liferay-ui:message key="selection-is-not-available" />
		</div>
	</c:when>
	<c:otherwise>
		<c:choose>
			<c:when test="<%= navigationItems.size() > 1 %>">
				<c:choose>
					<c:when test="<%= navigationItems.size() <= 5 %>">
						<clay:navigation-bar
							cssClass="border-bottom"
							inverted="<%= false %>"
							navigationItems="<%= navigationItems %>"
						/>

						<liferay-util:include page="/view_item_selector.jsp" servletContext="<%= application %>" />
					</c:when>
					<c:otherwise>
						<clay:container-fluid
							cssClass="container-view"
						>
							<clay:row>
								<clay:col
									lg="3"
								>
									<clay:vertical-nav
										verticalNavItems="<%= localizedItemSelectorRendering.getVerticalNavItemList() %>"
									/>
								</clay:col>

								<clay:col
									lg="9"
								>
									<clay:sheet
										size="full"
									>

										<%
										NavigationItem activeNavigationItem = localizedItemSelectorRendering.getActiveNavigationItem();
										%>

										<c:if test="<%= activeNavigationItem != null %>">
											<h2 class="sheet-title">
												<clay:content-row
													verticalAlign="center"
												>
													<clay:content-col>
														<%= GetterUtil.getString(activeNavigationItem.get("label")) %>
													</clay:content-col>
												</clay:content-row>
											</h2>
										</c:if>

										<clay:sheet-section>
											<liferay-util:include page="/view_item_selector.jsp" servletContext="<%= application %>" />
										</clay:sheet-section>
									</clay:sheet>
								</clay:col>
							</clay:row>
						</clay:container-fluid>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
				<liferay-util:include page="/view_item_selector.jsp" servletContext="<%= application %>" />
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>

<%!
private static final Log _log = LogFactoryUtil.getLog("com_liferay_item_selector_web.view_jsp");
%>