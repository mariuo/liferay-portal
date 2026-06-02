/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.request;

import com.liferay.portal.kernel.cluster.ClusterExecutor;
import com.liferay.portal.kernel.cluster.ClusterMasterExecutor;
import com.liferay.portal.kernel.cluster.ClusterNode;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.lock.model.Lock;
import com.liferay.portal.lock.service.LockLocalService;

import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tina Tian
 */
@Component(
	configurationPid = "com.liferay.ai.hub.configuration.AIHubAgentConfiguration",
	service = {}
)
public class RequestHelper {

	@Activate
	protected void activate() {
		if (!_clusterMasterExecutor.isMaster()) {
			return;
		}

		if (_clusterExecutor.isEnabled()) {
			List<ClusterNode> clusterNodes = _clusterExecutor.getClusterNodes();

			if (clusterNodes.size() > 1) {
				return;
			}
		}

		_companyLocalService.forEachCompany(
			company -> {
				List<Lock> locks = _lockLocalService.getLocks(
					company.getCompanyId(), RequestUtil.class.getName());

				for (Lock lock : locks) {
					_lockLocalService.deleteLock(lock);
				}
			});
	}

	@Modified
	protected void modified() {
		RequestUtil.reset();
	}

	@Reference
	private ClusterExecutor _clusterExecutor;

	@Reference
	private ClusterMasterExecutor _clusterMasterExecutor;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private LockLocalService _lockLocalService;

}