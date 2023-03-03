<%@ include file="/document_library/init.jsp" %>

<%
String eventName = ParamUtil.getString(request, "eventName", liferayPortletResponse.getNamespace() + "FilterByExtensions");
%>

<section class="h-100">
	<div class="align-items-center d-flex justify-content-center min-vh-100">
		<span aria-hidden="true" class="loading-animation"></span>
	</div>

	<react:component
		module="document_library/js/FilterExtensions"
		props='<%=
			HashMapBuilder.<String, Object>put(
				"itemSelectorSaveEvent", eventName
			).build()
		%>'
	/>
</section>
