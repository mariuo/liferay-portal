<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
ViewTeamsDisplayContext viewTeamsDisplayContext = (ViewTeamsDisplayContext)request.getAttribute(ViewTeamsDisplayContext.class.getName());
%>

<div class="cms-section custom-empty-state">
	<frontend-data-set:headless-display
		apiURL="<%= viewTeamsDisplayContext.getAPIURL() %>"
		emptyState="<%= viewTeamsDisplayContext.getEmptyState() %>"
		fdsActionDropdownItems="<%= viewTeamsDisplayContext.getFDSActionDropdownItems() %>"
		formName="fm"
		id="<%= CMSSiteInitializerFDSNames.TEAMS_SECTION %>"
		itemsPerPage="<%= 20 %>"
		selectedItemsKey="id"
		selectionType="multiple"
		style="fluid"
	/>
</div>