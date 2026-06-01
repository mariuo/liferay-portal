/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.request;

import com.liferay.account.model.AccountEntry;
import com.liferay.ai.hub.configuration.AIHubAgentConfiguration;
import com.liferay.ai.hub.util.AccountEntryUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.petra.concurrent.DCLSingleton;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.lock.DuplicateLockException;
import com.liferay.portal.kernel.lock.Lock;
import com.liferay.portal.kernel.lock.LockManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.lock.service.LockLocalServiceUtil;

import jakarta.persistence.PersistenceException;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

/**
 * @author Tina Tian
 */
public class RequestUtil {

	public static Lock acquire(long companyId, long timeout, long userId)
		throws PortalException {

		ObjectEntry objectEntry = _fetchQuotaObjectEntry(companyId, userId);

		if (objectEntry == null) {
			return null;
		}

		Semaphore semaphore = _acquireSemaphore();

		boolean acquired = false;

		try {
			String prefix =
				objectEntry.getExternalReferenceCode() + StringPool.COLON;

			Set<String> occupiedKeys = _getOccupiedKeys(prefix);

			String owner = PortalUUIDUtil.generate();

			int maxRequests = MapUtil.getInteger(
				objectEntry.getValues(), "maxRequests");

			if (maxRequests <= 0) {
				maxRequests = _DEFAULT_MAX_REQUESTS;
			}

			for (int i = 0; i < maxRequests; i++) {
				String key =
					prefix + Math.floorMod(owner.hashCode() + i, maxRequests);

				if (occupiedKeys.contains(key)) {
					continue;
				}

				try {
					Lock lock = LockManagerUtil.lock(
						userId, RequestUtil.class.getName(), key, owner, false,
						timeout, false);

					if (Objects.equals(lock.getOwner(), owner)) {
						_expirationTimes.put(
							owner, System.currentTimeMillis() + timeout);

						acquired = true;

						return lock;
					}
				}
				catch (DuplicateLockException | PersistenceException
							exception) {

					if (_log.isDebugEnabled()) {
						_log.debug(exception);
					}
				}
			}

			throw new ConcurrentRequestLimitException();
		}
		finally {
			if (!acquired) {
				semaphore.release();
			}
		}
	}

	public static void release(Lock lock) {
		if (lock == null) {
			return;
		}

		LockManagerUtil.unlock(
			lock.getClassName(), lock.getKey(), lock.getOwner());

		Semaphore semaphore = _semaphoreDCLSingleton.getSingleton(() -> null);

		if (semaphore == null) {
			return;
		}

		if (_expirationTimes.remove(lock.getOwner()) != null) {
			semaphore.release();
		}
	}

	public static void reset() {
		_semaphoreDCLSingleton.destroy(null);
	}

	private static Semaphore _acquireSemaphore() {
		long currentTime = System.currentTimeMillis();

		Semaphore semaphore = _semaphoreDCLSingleton.getSingleton(
			() -> {
				try {
					AIHubAgentConfiguration aiHubAgentConfiguration =
						ConfigurationProviderUtil.getSystemConfiguration(
							AIHubAgentConfiguration.class);

					int maxRequests = aiHubAgentConfiguration.maxRequests();

					return new Semaphore(maxRequests - _expirationTimes.size());
				}
				catch (PortalException portalException) {
					return ReflectionUtil.throwException(portalException);
				}
			});

		for (Map.Entry<String, Long> entry : _expirationTimes.entrySet()) {
			if ((entry.getValue() < currentTime) &&
				_expirationTimes.remove(entry.getKey(), entry.getValue())) {

				semaphore.release();
			}
		}

		if (!semaphore.tryAcquire()) {
			throw new ConcurrentRequestLimitException();
		}

		return semaphore;
	}

	private static ObjectEntry _fetchQuotaObjectEntry(
			long companyId, long userId)
		throws PortalException {

		AccountEntry accountEntry = AccountEntryUtil.getUserAccountEntry(
			userId);

		if (accountEntry == null) {
			return null;
		}

		ObjectDefinition objectDefinition =
			ObjectDefinitionLocalServiceUtil.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_AI_HUB_QUOTA", companyId);

		if (objectDefinition == null) {
			return null;
		}

		User user = UserLocalServiceUtil.getUser(userId);

		String externalReferenceCode =
			"quota-" + accountEntry.getAccountEntryId();

		if (user.isServiceAccountUser()) {
			externalReferenceCode =
				"guest-quota-" + accountEntry.getAccountEntryId();
		}

		return ObjectEntryLocalServiceUtil.fetchObjectEntry(
			externalReferenceCode, 0, objectDefinition.getObjectDefinitionId());
	}

	private static Set<String> _getOccupiedKeys(String prefix) {
		DynamicQuery dynamicQuery = LockLocalServiceUtil.dynamicQuery();

		dynamicQuery.add(
			PropertyFactoryUtil.forName(
				"className"
			).eq(
				RequestUtil.class.getName()
			));
		dynamicQuery.add(
			PropertyFactoryUtil.forName(
				"key"
			).like(
				prefix + "%"
			));
		dynamicQuery.add(
			PropertyFactoryUtil.forName(
				"expirationDate"
			).gt(
				new Date()
			));

		dynamicQuery.setProjection(ProjectionFactoryUtil.property("key"));

		List<String> keys = LockLocalServiceUtil.dynamicQuery(dynamicQuery);

		return new HashSet<>(keys);
	}

	private static final int _DEFAULT_MAX_REQUESTS = 10;

	private static final Log _log = LogFactoryUtil.getLog(RequestUtil.class);

	private static final ConcurrentMap<String, Long> _expirationTimes =
		new ConcurrentHashMap<>();
	private static final DCLSingleton<Semaphore> _semaphoreDCLSingleton =
		new DCLSingleton<>();

}