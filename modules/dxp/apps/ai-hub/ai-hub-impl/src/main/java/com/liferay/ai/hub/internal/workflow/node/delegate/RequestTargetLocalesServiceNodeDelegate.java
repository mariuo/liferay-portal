/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate;

import com.liferay.ai.hub.rest.resource.v1_0.util.SseUtil;
import com.liferay.ai.hub.workflow.node.ServiceNodeDelegate;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Carolina Barbosa
 */
@Component(service = ServiceNodeDelegate.class)
public class RequestTargetLocalesServiceNodeDelegate
	implements ServiceNodeDelegate {

	@Override
	public String execute(
			ExecutionContext executionContext,
			Map<String, String> inputVariables, KaleoNode kaleoNode,
			Map<String, Serializable> workflowContext)
		throws Exception {

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		SseUtil.send(
			JSONUtil.put(
				"action", "autoTranslate"
			).put(
				"availableLanguageIds",
				JSONUtil.toJSONArray(
					LanguageUtil.getAvailableLocales(),
					LocaleUtil::toLanguageId)
			).put(
				"agentInstanceId", kaleoInstanceToken.getKaleoInstanceId()
			).toString(),
			"Chat Message Sent", kaleoNode.getName(),
			GetterUtil.getString(workflowContext.get("sseEventSinkKey")));

		return StringPool.BLANK;
	}

	@Override
	public String getKey() {
		return "javaDelegate#requestTargetLocales";
	}

}