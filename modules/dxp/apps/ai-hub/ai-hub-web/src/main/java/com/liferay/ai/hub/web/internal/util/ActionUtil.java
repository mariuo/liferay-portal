/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.web.internal.util;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.scope.ObjectScopeProvider;
import com.liferay.object.scope.ObjectScopeProviderRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Carolina Barbosa
 */
public class ActionUtil {

	public static String getAIHubURL(ThemeDisplay themeDisplay)
		throws Exception {

		Company company = themeDisplay.getCompany();
		Group group = themeDisplay.getScopeGroup();

		return StringBundler.concat(
			company.getPortalURL(GroupConstants.DEFAULT_PARENT_GROUP_ID),
			"/web", group.getFriendlyURL());
	}

	public static boolean isReadOnly(
			String externalReferenceCode, HttpServletRequest httpServletRequest,
			String objectDefinitionExternalReferenceCode,
			ObjectDefinitionLocalService objectDefinitionLocalService,
			ObjectEntryService objectEntryService,
			ObjectScopeProviderRegistry objectScopeProviderRegistry)
		throws PortalException {

		if (Validator.isNull(externalReferenceCode)) {
			return false;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		ObjectDefinition objectDefinition =
			objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					objectDefinitionExternalReferenceCode,
					themeDisplay.getCompanyId());

		if (objectDefinition == null) {
			return false;
		}

		ObjectScopeProvider objectScopeProvider =
			objectScopeProviderRegistry.getObjectScopeProvider(
				objectDefinition.getScope());

		ObjectEntry objectEntry = objectEntryService.fetchObjectEntry(
			externalReferenceCode,
			objectScopeProvider.getGroupId(httpServletRequest),
			objectDefinition.getObjectDefinitionId());

		if (objectEntry == null) {
			return false;
		}

		return !objectEntryService.hasModelResourcePermission(
			objectEntry, ActionKeys.UPDATE);
	}

}