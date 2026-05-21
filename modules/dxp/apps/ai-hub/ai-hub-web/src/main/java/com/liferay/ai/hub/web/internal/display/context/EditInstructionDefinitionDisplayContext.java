/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.web.internal.display.context;

import com.liferay.account.model.AccountEntry;
import com.liferay.ai.hub.util.AccountEntryUtil;
import com.liferay.ai.hub.web.internal.util.ActionUtil;
import com.liferay.object.scope.ObjectScopeProviderRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * @author Carolina Barbosa
 */
public class EditInstructionDefinitionDisplayContext {

	public EditInstructionDefinitionDisplayContext(
		HttpServletRequest httpServletRequest,
		ObjectDefinitionLocalService objectDefinitionLocalService,
		ObjectEntryService objectEntryService,
		ObjectScopeProviderRegistry objectScopeProviderRegistry) {

		_httpServletRequest = httpServletRequest;
		_objectDefinitionLocalService = objectDefinitionLocalService;
		_objectEntryService = objectEntryService;
		_objectScopeProviderRegistry = objectScopeProviderRegistry;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public Map<String, Object> getReactData() throws Exception {
		return HashMapBuilder.<String, Object>put(
			"accountEntryExternalReferenceCode",
			() -> {
				AccountEntry accountEntry =
					AccountEntryUtil.getUserAccountEntry(
						_themeDisplay.getUserId());

				if (accountEntry == null) {
					return null;
				}

				return accountEntry.getExternalReferenceCode();
			}
		).put(
			"backURL", ActionUtil.getAIHubURL(_themeDisplay) + "/agent-builder"
		).put(
			"externalReferenceCode",
			_httpServletRequest.getParameter("externalReferenceCode")
		).put(
			"readonly",
			() -> ActionUtil.isReadOnly(
				_httpServletRequest.getParameter("externalReferenceCode"),
				_httpServletRequest, "L_AI_HUB_INSTRUCTION_DEFINITION",
				_objectDefinitionLocalService, _objectEntryService,
				_objectScopeProviderRegistry)
		).build();
	}

	private final HttpServletRequest _httpServletRequest;
	private final ObjectDefinitionLocalService _objectDefinitionLocalService;
	private final ObjectEntryService _objectEntryService;
	private final ObjectScopeProviderRegistry _objectScopeProviderRegistry;
	private final ThemeDisplay _themeDisplay;

}