/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.roles.admin.edit.role.permissions.portlet.filter;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.roles.admin.edit.role.permissions.portlet.filter.EditRolePermissionsPortletFilter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mario Gomes
 */
@Component(service = EditRolePermissionsPortletFilter.class)
public class AIHubEditRolePermissionsPortletFilter
	implements EditRolePermissionsPortletFilter {

	@Override
	public boolean isHidden(String portletId, Role role) {
		if (role.getType() == RoleConstants.TYPE_ACCOUNT) {
			return false;
		}

		for (ObjectDefinition objectDefinition :
				_objectDefinitionLocalService.getObjectDefinitions(
					role.getCompanyId(), WorkflowConstants.STATUS_APPROVED)) {

			String externalReferenceCode =
				objectDefinition.getExternalReferenceCode();

			if ((externalReferenceCode == null) ||
				!externalReferenceCode.startsWith(
					_AI_HUB_EXTERNAL_REFERENCE_CODE_PREFIX)) {

				continue;
			}

			if (portletId.equals(objectDefinition.getPortletId())) {
				return true;
			}
		}

		return false;
	}

	private static final String _AI_HUB_EXTERNAL_REFERENCE_CODE_PREFIX =
		"L_AI_HUB_";

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

}