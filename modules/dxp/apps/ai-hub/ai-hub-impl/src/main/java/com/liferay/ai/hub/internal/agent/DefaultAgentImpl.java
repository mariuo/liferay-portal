/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.agent;

import com.liferay.ai.hub.agent.AgentContext;
import com.liferay.ai.hub.agent.DefaultAgent;
import com.liferay.ai.hub.internal.langchain4j.agentic.internal.InternalAgentImpl;
import com.liferay.ai.hub.quota.QuotaManager;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManager;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceTokenLocalService;
import com.liferay.portal.workflow.manager.WorkflowDefinitionManager;

import dev.langchain4j.agentic.planner.AgentArgument;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Feliphe Marinho
 */
@Component(service = DefaultAgent.class)
public class DefaultAgentImpl implements DefaultAgent {

	@Override
	public Object invoke(AgentContext agentContext) {
		InternalAgentImpl internalAgentImpl = new InternalAgentImpl(
			agentContext, _quotaManager, _workflowDefinitionManager,
			_workflowInstanceManager);

		internalAgentImpl.setAgentArguments(
			TransformUtil.transform(
				agentContext.getInputVariableNames(),
				inputVariableName -> new AgentArgument(
					String.class, inputVariableName)));
		internalAgentImpl.setAsync(agentContext.isAsynchronous());
		internalAgentImpl.setName(
			agentContext.getAgentDefinitionExternalReferenceCode());
		internalAgentImpl.setOutBoundEventName(
			agentContext.getAgentDefinitionExternalReferenceCode());
		internalAgentImpl.setWorkflowDefinitionName(
			agentContext.getWorkflowDefinitionName());

		return internalAgentImpl.invoke(agentContext.getInput());
	}

	@Override
	public void resume(AgentContext agentContext, long workflowInstanceId)
		throws Exception {

		WorkflowInstance workflowInstance =
			_workflowInstanceManager.getWorkflowInstance(
				agentContext.getCompanyId(), workflowInstanceId);

		Map<String, Serializable> workflowContext = new HashMap<>(
			workflowInstance.getWorkflowContext());

		Map<String, ?> input = agentContext.getInput();

		if (input != null) {
			for (String key : input.keySet()) {
				workflowContext.put(key, MapUtil.getString(input, key));
			}
		}

		KaleoInstanceToken kaleoInstanceToken =
			_kaleoInstanceTokenLocalService.getRootKaleoInstanceToken(
				workflowInstanceId, workflowContext,
				agentContext.getServiceContext());

		_workflowNodeManager.completeWorkflowNode(
			agentContext.getCompanyId(), agentContext.getUserId(),
			kaleoInstanceToken.getKaleoInstanceTokenId(), null, workflowContext,
			false);
	}

	@Reference
	private KaleoInstanceTokenLocalService _kaleoInstanceTokenLocalService;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private QuotaManager _quotaManager;

	@Reference
	private WorkflowDefinitionManager _workflowDefinitionManager;

	@Reference
	private WorkflowInstanceManager _workflowInstanceManager;

	@Reference
	private WorkflowNodeManager _workflowNodeManager;

}