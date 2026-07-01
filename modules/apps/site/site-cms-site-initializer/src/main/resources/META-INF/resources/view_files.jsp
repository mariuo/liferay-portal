<%--
/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
ViewFilesSectionDisplayContext viewFilesSectionDisplayContext = (ViewFilesSectionDisplayContext)request.getAttribute(ViewFilesSectionDisplayContext.class.getName());
%>

<div>
	<div class="align-items-center d-flex justify-content-between">
		<react:component
			module="{Breadcrumb} from site-cms-site-initializer"
			props="<%= viewFilesSectionDisplayContext.getBreadcrumbProps() %>"
		/>

		<react:component
			module="{FilesAIAssistant} from site-cms-site-initializer"
			props='<%=
				HashMapBuilder.<String, Object>put(
					"filesDataSetId", CMSSiteInitializerFDSNames.FILES_SECTION
				).put(
					"spaceGroupId", viewFilesSectionDisplayContext.getDefaultSpaceGroupId()
				).put(
					"spaces", viewFilesSectionDisplayContext.getSpacesJSONArray()
				).build()
			%>'
		/>
	</div>

	<div class="cms-section custom-empty-state">
		<frontend-data-set:headless-display
			additionalProps="<%= viewFilesSectionDisplayContext.getAdditionalProps() %>"
			apiURL="<%= viewFilesSectionDisplayContext.getAPIURL() %>"
			bulkActionDropdownItems="<%= viewFilesSectionDisplayContext.getBulkActionDropdownItems() %>"
			creationMenu="<%= viewFilesSectionDisplayContext.getCreationMenu() %>"
			emptyState="<%= viewFilesSectionDisplayContext.getEmptyState() %>"
			fdsActionDropdownItems="<%= viewFilesSectionDisplayContext.getFDSActionDropdownItems() %>"
			formName="fm"
			id="<%= CMSSiteInitializerFDSNames.FILES_SECTION %>"
			itemsPerPage="<%= 20 %>"
			propsTransformer="{AssetsFilesDropFDSPropsTransformer} from site-cms-site-initializer"
			selectedItemsKey="embedded.id"
			selectionType="multiple"
			showSelectAll="<%= true %>"
		/>
	</div>
</div>