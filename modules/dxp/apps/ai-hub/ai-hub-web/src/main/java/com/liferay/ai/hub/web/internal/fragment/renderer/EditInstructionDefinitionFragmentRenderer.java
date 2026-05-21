/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.web.internal.fragment.renderer;

import com.liferay.ai.hub.web.internal.display.context.EditInstructionDefinitionDisplayContext;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.object.scope.ObjectScopeProviderRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryService;

import jakarta.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carolina Barbosa
 */
@Component(service = FragmentRenderer.class)
public class EditInstructionDefinitionFragmentRenderer
	extends BaseFragmentRenderer<EditInstructionDefinitionDisplayContext> {

	@Override
	public String getCollectionKey() {
		return "instruction-definition";
	}

	@Override
	protected EditInstructionDefinitionDisplayContext getDisplayContext(
		HttpServletRequest httpServletRequest) {

		return new EditInstructionDefinitionDisplayContext(
			httpServletRequest, _objectDefinitionLocalService,
			_objectEntryService, _objectScopeProviderRegistry);
	}

	@Override
	protected String getJSPPath() {
		return "/edit_instruction_definition.jsp";
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryService _objectEntryService;

	@Reference
	private ObjectScopeProviderRegistry _objectScopeProviderRegistry;

}