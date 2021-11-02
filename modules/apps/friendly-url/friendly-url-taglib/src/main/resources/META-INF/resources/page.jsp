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
final int friendlyUrlMaxLength = 255; // TODO: move to constant
%>

<liferay-util:html-top
	outputKey="com.liferay.friendly.url.taglib.servlet.taglib#/page.jsp"
>
	<link href="<%= PortalUtil.getStaticResourceURL(request, application.getContextPath() + "/css/main.css") %>" rel="stylesheet" type="text/css" />
</liferay-util:html-top>

<%-- TODO: abstract MVC*Commands and create urls--%>

<div class="form-group friendly-url">
	<label for="<portlet:namespace />friendlyURL"><liferay-ui:message key="friendly-url" /> <liferay-ui:icon-help message='<%= LanguageUtil.format(request, "there-is-a-limit-of-x-characters-in-encoded-format-for-friendly-urls-(e.g.-x)", new String[] {String.valueOf(friendlyUrlMaxLength), "<em>/news</em>"}, false) %>' /></label>

	<div class="btn-url-history-wrapper">

		<%
		User defaultUser = company.getDefaultUser();
		%>

		<react:component
			module="js/FriendlyURLHistory"
			props='<%=
				HashMapBuilder.<String, Object>put(
					"defaultLanguageId", LocaleUtil.toLanguageId(defaultUser.getLocale())
				).put(
					"deleteFriendlyURLEntryLocalizationURL", "deleteFriendlyURLEntryLocalizationURL"
				).put(
					"friendlyURLEntryLocalizationsURL", "friendlyURLEntryLocalizationsURL"
				).put(
					"restoreFriendlyURLEntryLocalizationURL", "restoreFriendlyURLEntryLocalizationURL"
				).build()
			%>'
		/>
	</div>

	<%-- TODO: pass the xml via taglib attribute
	<liferay-ui:input-localized
		defaultLanguageId="<%= LocaleUtil.toLanguageId(themeDisplay.getSiteDefaultLocale()) %>"
		ignoreRequestValue="<%= SessionErrors.isEmpty(liferayPortletRequest) %>"
		inputAddon="<%= friendlyURLBase.toString() %>"
		name="friendlyURL"
		xml="<%= HttpUtil.decodeURL(selLayout.getFriendlyURLsXML()) %>"
	/>
	--%>

</div>