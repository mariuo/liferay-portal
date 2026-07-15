/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate;

import com.liferay.ai.hub.workflow.node.ServiceNodeDelegate;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.translation.translator.JSONTranslatorPacket;
import com.liferay.translation.translator.Translator;
import com.liferay.translation.translator.TranslatorPacket;
import com.liferay.translation.translator.TranslatorRegistry;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carolina Barbosa
 */
@Component(service = ServiceNodeDelegate.class)
public class TranslateContentServiceNodeDelegate
	implements ServiceNodeDelegate {

	@Override
	public String execute(
			ExecutionContext executionContext,
			Map<String, String> inputVariables, KaleoNode kaleoNode,
			Map<String, Serializable> workflowContext)
		throws Exception {

		Translator translator = _translatorRegistry.getCompanyTranslator(
			kaleoNode.getCompanyId());

		if ((translator == null) || translator.isAIAssisted()) {
			workflowContext.put("translatedContent", StringPool.BLANK);

			_completeWorkflowNode(executionContext, workflowContext);

			return StringPool.BLANK;
		}

		JSONArray resultsJSONArray = _jsonFactory.createJSONArray();

		JSONArray targetLanguageIdsJSONArray = _jsonFactory.createJSONArray(
			GetterUtil.getString(inputVariables.get("targetLanguageIds")));

		for (int i = 0; i < targetLanguageIdsJSONArray.length(); i++) {
			TranslatorPacket translatorPacket = translator.translate(
				new JSONTranslatorPacket(
					kaleoNode.getCompanyId(),
					_jsonFactory.createJSONObject(
					).put(
						"fields",
						_jsonFactory.createJSONObject(
							inputVariables.get("fields"))
					).put(
						"html",
						_jsonFactory.createJSONObject(
							inputVariables.get("html"))
					).put(
						"sourceLanguageId",
						inputVariables.get("sourceLanguageId")
					).put(
						"targetLanguageId",
						targetLanguageIdsJSONArray.getString(i)
					)));

			resultsJSONArray.put(
				_jsonFactory.createJSONObject(
				).put(
					"fields",
					_jsonFactory.createJSONObject(
						translatorPacket.getFieldsMap())
				).put(
					"targetLanguageId", targetLanguageIdsJSONArray.getString(i)
				));
		}

		String translatedContent = JSONUtil.put(
			"action", "autoTranslate"
		).put(
			"results", resultsJSONArray
		).put(
			"status", "complete"
		).toString();

		workflowContext.put("translatedContent", translatedContent);

		_completeWorkflowNode(executionContext, workflowContext);

		return translatedContent;
	}

	@Override
	public String getKey() {
		return "javaDelegate#translateContent";
	}

	private void _completeWorkflowNode(
			ExecutionContext executionContext,
			Map<String, Serializable> workflowContext)
		throws Exception {

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		_workflowNodeManager.completeWorkflowNode(
			kaleoInstanceToken.getCompanyId(), kaleoInstanceToken.getUserId(),
			kaleoInstanceToken.getKaleoInstanceTokenId(),
			"composeTranslatedContent", workflowContext, false);
	}

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private TranslatorRegistry _translatorRegistry;

	@Reference
	private WorkflowNodeManager _workflowNodeManager;

}