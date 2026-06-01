/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Tina Tian
 */
@ExtendedObjectClassDefinition(
	category = "ai-hub", featureFlagKey = "LPD-62272",
	scope = ExtendedObjectClassDefinition.Scope.SYSTEM
)
@Meta.OCD(
	id = "com.liferay.ai.hub.configuration.AIHubAgentConfiguration",
	localization = "content/Language", name = "ai-hub-agent-configuration-name"
)
public interface AIHubAgentConfiguration {

	@Meta.AD(deflt = "100", name = "max-requests", required = false)
	public int maxRequests();

}