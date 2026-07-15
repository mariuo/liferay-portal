/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate;

import com.liferay.ai.hub.rest.resource.v1_0.util.SseUtil;
import com.liferay.ai.hub.workflow.node.ServiceNodeDelegate;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;

import java.io.Serializable;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carolina Barbosa
 */
@Component(service = ServiceNodeDelegate.class)
public class RequestPageContextServiceNodeDelegate
	implements ServiceNodeDelegate {

	@Override
	public String execute(
			ExecutionContext executionContext,
			Map<String, String> inputVariables, KaleoNode kaleoNode,
			Map<String, Serializable> workflowContext)
		throws Exception {

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		Set<Locale> availableLocales = _language.getCompanyAvailableLocales(
			kaleoInstanceToken.getCompanyId());

		SseUtil.send(
			JSONUtil.put(
				"action", "autoTranslate"
			).put(
				"agentInstanceId", kaleoInstanceToken.getKaleoInstanceId()
			).put(
				"availableLanguageIds",
				JSONUtil.toJSONArray(availableLocales, LocaleUtil::toLanguageId)
			).put(
				"targetLanguageIds",
				() -> {
					String instruction = StringUtil.toLowerCase(
						GetterUtil.getString(
							inputVariables.get("instruction")));

					if (Validator.isNull(instruction)) {
						return _jsonFactory.createJSONArray();
					}

					JSONArray jsonArray = _jsonFactory.createJSONArray();

					for (Locale locale : availableLocales) {
						String languageId = LocaleUtil.toLanguageId(locale);

						if (instruction.contains(
								StringUtil.toLowerCase(languageId)) ||
							instruction.contains(
								StringUtil.toLowerCase(
									locale.getDisplayLanguage()))) {

							jsonArray.put(languageId);
						}
					}

					return jsonArray;
				}
			).toString(),
			"Chat Message Sent", kaleoNode.getName(),
			GetterUtil.getString(workflowContext.get("sseEventSinkKey")));

		return StringPool.BLANK;
	}

	@Override
	public String getKey() {
		return "javaDelegate#requestPageContext";
	}

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

}