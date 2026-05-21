/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.roles.admin.edit.role.permissions.portlet.filter;

import com.liferay.portal.kernel.model.Role;

/**
 * Hides a portlet from the Define Permissions navigation in the Roles Admin
 * portlet for a given role. Implementations contribute exclusion rules tied to
 * the role's type or other role attributes.
 *
 * @author Mario Gomes
 */
public interface EditRolePermissionsPortletFilter {

	public boolean isHidden(String portletId, Role role);

}