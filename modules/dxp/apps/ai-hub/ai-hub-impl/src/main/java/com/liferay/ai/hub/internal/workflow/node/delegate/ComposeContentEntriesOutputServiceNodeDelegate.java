/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate;

import com.liferay.ai.hub.workflow.node.ServiceNodeDelegate;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.model.KaleoTransition;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;

import java.io.Serializable;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pedro Leite
 */
@Component(service = ServiceNodeDelegate.class)
public class ComposeContentEntriesOutputServiceNodeDelegate
	implements ServiceNodeDelegate {

	@Override
	public String execute(
			ExecutionContext executionContext,
			Map<String, String> inputVariables, KaleoNode kaleoNode,
			Map<String, Serializable> workflowContext)
		throws Exception {

		String contentEntriesPayload = inputVariables.get(
			"contentEntriesPayload");

		if (Validator.isNull(contentEntriesPayload)) {
			String output =
				"I could not generate any content. Please try again.";

			workflowContext.put("output", output);

			_completeWorkflowNode(executionContext, kaleoNode, workflowContext);

			return output;
		}

		JSONArray jsonArray = _jsonFactory.createJSONArray(
			contentEntriesPayload);

		if (jsonArray.length() == 0) {
			String output = _getOutput(inputVariables);

			workflowContext.put("output", output);

			_completeWorkflowNode(executionContext, kaleoNode, workflowContext);

			return output;
		}

		String output = StringUtil.merge(
			JSONUtil.toList(
				jsonArray,
				jsonObject -> {
					JSONObject propertiesJSONObject = jsonObject.getJSONObject(
						"properties");

					return StringBundler.concat(
						"- [", propertiesJSONObject.getString("title"), "](",
						MapUtil.getString(
							workflowContext, "aiHubCellLiferayDXPURL"),
						"/c/cms/edit_content_item?externalReferenceCode=",
						URLCodec.encodeURL(
							jsonObject.getString("externalReferenceCode")),
						"&groupId=",
						URLCodec.encodeURL(inputVariables.get("spaceId")),
						"&objectDefinitionName=",
						URLCodec.encodeURL(
							inputVariables.get("objectDefinitionName")),
						")");
				}),
			"\n");

		workflowContext.put("output", output);

		_completeWorkflowNode(executionContext, kaleoNode, workflowContext);

		return output;
	}

	@Override
	public String getKey() {
		return "javaDelegate#composeContentEntriesOutput";
	}

	private void _completeWorkflowNode(
			ExecutionContext executionContext, KaleoNode kaleoNode,
			Map<String, Serializable> workflowContext)
		throws Exception {

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		List<KaleoTransition> kaleoTransitions =
			kaleoNode.getKaleoTransitions();

		KaleoTransition kaleoTransition = kaleoTransitions.get(0);

		_workflowNodeManager.completeWorkflowNode(
			kaleoInstanceToken.getCompanyId(), kaleoInstanceToken.getUserId(),
			kaleoInstanceToken.getKaleoInstanceTokenId(),
			kaleoTransition.getName(), workflowContext, false);
	}

	private String _getOutput(Map<String, String> inputVariables) {
		if (Validator.isNull(inputVariables.get("objectDefinitionName")) ||
			Validator.isNull(inputVariables.get("spaceId"))) {

			return StringBundler.concat(
				"I can only generate content when a destination space and a ",
				"content type are selected. Please open the AI Assistant from ",
				"a content section.");
		}

		return StringBundler.concat(
			"I can only generate ", inputVariables.get("objectDefinitionName"),
			" content here. To generate a different content type, open the AI ",
			"Assistant from that content type's section.");
	}

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private WorkflowNodeManager _workflowNodeManager;

}