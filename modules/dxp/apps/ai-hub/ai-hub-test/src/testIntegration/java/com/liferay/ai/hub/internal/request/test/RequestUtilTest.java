/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.request.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountEntryUserRelLocalService;
import com.liferay.ai.hub.agent.SupervisorAgent;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.lock.Lock;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.site.initializer.SiteInitializer;
import com.liferay.site.initializer.SiteInitializerRegistry;

import java.io.Serializable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tina Tian
 */
@FeatureFlag("LPD-62272")
@RunWith(Arquillian.class)
public class RequestUtilTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		Class<?> clazz = _supervisorAgent.getClass();

		ClassLoader classLoader = clazz.getClassLoader();

		Class<?> requestUtilClass = classLoader.loadClass(
			"com.liferay.ai.hub.internal.request.RequestUtil");

		_acquireMethod = requestUtilClass.getMethod(
			"acquire", long.class, long.class, long.class);
		_releaseMethod = requestUtilClass.getMethod("release", Lock.class);

		_expirationTimesField = requestUtilClass.getDeclaredField(
			"_expirationTimes");

		_expirationTimesField.setAccessible(true);

		_semaphoreDCLSingletonField = requestUtilClass.getDeclaredField(
			"_semaphoreDCLSingleton");

		_semaphoreDCLSingletonField.setAccessible(true);

		Object semaphoreDCLSingleton = _semaphoreDCLSingletonField.get(null);

		Class<?> dclSingletonClass = semaphoreDCLSingleton.getClass();

		_destroyMethod = dclSingletonClass.getMethod("destroy", Consumer.class);
		_getSingletonMethod = dclSingletonClass.getMethod(
			"getSingleton", Supplier.class);

		_originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		PermissionThreadLocal.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(TestPropsValues.getUser()));

		_originalName = PrincipalThreadLocal.getName();

		PrincipalThreadLocal.setName(TestPropsValues.getUserId());

		ServiceContextThreadLocal.pushServiceContext(
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId(), TestPropsValues.getUserId()));

		SiteInitializer siteInitializer =
			_siteInitializerRegistry.getSiteInitializer(
				"com.liferay.ai.hub.site.initializer");

		siteInitializer.initialize(TestPropsValues.getGroupId());
	}

	@AfterClass
	public static void tearDownClass() {
		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);
		PrincipalThreadLocal.setName(_originalName);
		ServiceContextThreadLocal.popServiceContext();
	}

	@After
	public void tearDown() throws Exception {
		for (Lock lock : _locks) {
			_release(lock);
		}

		Object semaphoreDCLSingleton = _semaphoreDCLSingletonField.get(null);

		_destroyMethod.invoke(semaphoreDCLSingleton, new Object[] {null});

		Map<String, Long> expirationTimes =
			(Map<String, Long>)_expirationTimesField.get(null);

		expirationTimes.clear();
	}

	@Test
	public void testAcquireAndRelease() throws Exception {
		int maxRequests = RandomTestUtil.randomInt(2, 20);

		ObjectEntry objectEntry1 = _addObjectEntry(maxRequests);
		ObjectEntry objectEntry2 = _addObjectEntry(maxRequests);

		_setUpSemaphore(maxRequests);

		for (int i = 0; i < maxRequests; i++) {
			Assert.assertNotNull(
				_acquire(objectEntry1.getUserId(), Time.MINUTE));
		}

		_assertLocks(objectEntry1, maxRequests);

		_assertAcquireFailed(objectEntry1.getUserId(), Time.MINUTE);

		_assertAcquireFailed(objectEntry2.getUserId(), Time.MINUTE);

		Lock lock1 = _locks.remove(0);

		_release(lock1);

		Lock lock2 = _acquire(objectEntry1.getUserId(), Time.MINUTE);

		Assert.assertEquals(lock1.getKey(), lock2.getKey());
		Assert.assertNotEquals(lock1.getOwner(), lock2.getOwner());

		_assertAcquireFailed(objectEntry2.getUserId(), Time.MINUTE);

		_release(lock2);

		_locks.remove(lock2);

		Lock lock3 = _acquire(objectEntry2.getUserId(), Time.MINUTE);

		String key = lock3.getKey();

		Assert.assertTrue(
			key.startsWith(objectEntry2.getExternalReferenceCode()));

		_assertAcquireFailed(objectEntry1.getUserId(), Time.MINUTE);
	}

	@Test
	public void testAcquireWithConcurrentRequests() throws Exception {
		int maxRequests = RandomTestUtil.randomInt(2, 20);

		int threadCount = maxRequests + RandomTestUtil.randomInt(1, 10);

		ObjectEntry objectEntry = _addObjectEntry(maxRequests);

		List<ExecutionException> executionExceptions = new ArrayList<>();

		CountDownLatch countDownLatch = new CountDownLatch(1);

		ExecutorService executorService = Executors.newFixedThreadPool(
			threadCount);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				SqlExceptionHelper.class.getName(), LoggerTestUtil.OFF)) {

			List<Future<Lock>> futures = new ArrayList<>();

			for (int i = 0; i < threadCount; i++) {
				futures.add(
					executorService.submit(
						() -> {
							countDownLatch.await();

							try {
								return (Lock)_acquireMethod.invoke(
									null, TestPropsValues.getCompanyId(),
									Time.MINUTE, objectEntry.getUserId());
							}
							catch (Exception exception) {
								throw new RuntimeException(exception);
							}
						}));
			}

			countDownLatch.countDown();

			for (Future<Lock> future : futures) {
				try {
					_locks.add(future.get());
				}
				catch (ExecutionException executionException) {
					executionExceptions.add(executionException);
				}
			}
		}
		finally {
			executorService.shutdown();
		}

		_assertLocks(objectEntry, maxRequests);

		Assert.assertEquals(
			executionExceptions.toString(), threadCount - maxRequests,
			executionExceptions.size());
	}

	@Test
	public void testAcquireWithConfiguration() throws Exception {
		ObjectEntry objectEntry = _addObjectEntry(10);

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_CONFIGURATION_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"maxRequests", 2
					).build())) {

			_acquire(objectEntry.getUserId(), Time.MINUTE);

			_acquire(objectEntry.getUserId(), Time.MINUTE);

			_assertAcquireFailed(objectEntry.getUserId(), Time.MINUTE);
		}

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_CONFIGURATION_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"maxRequests", 3
					).build())) {

			_acquire(objectEntry.getUserId(), Time.MINUTE);

			_assertAcquireFailed(objectEntry.getUserId(), Time.MINUTE);
		}

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_CONFIGURATION_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"maxRequests", 2
					).build())) {

			_assertAcquireFailed(objectEntry.getUserId(), Time.MINUTE);

			Lock lock = _locks.remove(0);

			_release(lock);

			_assertAcquireFailed(objectEntry.getUserId(), Time.MINUTE);

			lock = _locks.remove(0);

			_release(lock);

			_acquire(objectEntry.getUserId(), Time.MINUTE);
		}
	}

	@Test
	public void testAcquireWithExpiration() throws Exception {
		int maxRequests = RandomTestUtil.randomInt(2, 20);

		ObjectEntry objectEntry = _addObjectEntry(maxRequests);

		Semaphore semaphore = _setUpSemaphore(maxRequests);

		for (int i = 0; i < maxRequests; i++) {
			_acquire(objectEntry.getUserId(), Time.SECOND);
		}

		Thread.sleep(Time.SECOND * maxRequests);

		Assert.assertEquals(0, semaphore.availablePermits());

		Map<String, Long> expirationTimes =
			(Map<String, Long>)_expirationTimesField.get(null);

		Assert.assertEquals(
			expirationTimes.toString(), maxRequests, expirationTimes.size());

		Assert.assertNotNull(_acquire(objectEntry.getUserId(), Time.MINUTE));

		Assert.assertEquals(maxRequests - 1, semaphore.availablePermits());

		Assert.assertEquals(
			expirationTimes.toString(), 1, expirationTimes.size());
	}

	private Lock _acquire(long userId, long timeout) throws Exception {
		try {
			Lock lock = (Lock)_acquireMethod.invoke(
				null, TestPropsValues.getCompanyId(), timeout, userId);

			_locks.add(lock);

			return lock;
		}
		catch (InvocationTargetException invocationTargetException) {
			Throwable throwable = invocationTargetException.getCause();

			if (throwable instanceof Exception) {
				throw (Exception)throwable;
			}

			throw invocationTargetException;
		}
	}

	private AccountEntry _addAccountEntry(User user) throws Exception {
		AccountEntry accountEntry = _accountEntryLocalService.addAccountEntry(
			null, TestPropsValues.getUserId(),
			AccountConstants.PARENT_ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), null, null, null, null, null,
			AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS,
			WorkflowConstants.STATUS_APPROVED,
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId(), TestPropsValues.getUserId()));

		AccountEntry aiHubAccountEntry =
			_accountEntryLocalService.getAccountEntryByExternalReferenceCode(
				"L_AI_HUB", TestPropsValues.getCompanyId());

		_accountEntryUserRelLocalService.addAccountEntryUserRels(
			aiHubAccountEntry.getAccountEntryId(),
			new long[] {user.getUserId()});

		_accountEntryUserRelLocalService.addAccountEntryUserRels(
			accountEntry.getAccountEntryId(), new long[] {user.getUserId()});

		return accountEntry;
	}

	private ObjectEntry _addObjectEntry(int maxRequests) throws Exception {
		User user = UserTestUtil.addUser();

		_users.add(user);

		AccountEntry accountEntry = _addAccountEntry(user);

		_accountEntries.add(accountEntry);

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_AI_HUB_QUOTA", TestPropsValues.getCompanyId());

		ObjectEntry objectEntry = _objectEntryLocalService.addObjectEntry(
			0, user.getUserId(), objectDefinition.getObjectDefinitionId(), 0,
			LocaleUtil.toLanguageId(LocaleUtil.getDefault()),
			HashMapBuilder.<String, Serializable>put(
				"externalReferenceCode",
				"quota-" + accountEntry.getAccountEntryId()
			).put(
				"maxRequests", maxRequests
			).put(
				"r_accountToAIHubQuotas_accountEntryId",
				accountEntry.getAccountEntryId()
			).build(),
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId(), TestPropsValues.getUserId()));

		_objectEntries.add(objectEntry);

		return objectEntry;
	}

	private void _assertAcquireFailed(long userId, long timeout)
		throws Exception {

		try {
			_acquire(userId, timeout);

			Assert.fail();
		}
		catch (UnsupportedOperationException unsupportedOperationException) {
			Assert.assertEquals(
				"You have exceeded your concurrent request limit",
				unsupportedOperationException.getMessage());
		}
	}

	private void _assertLocks(ObjectEntry objectEntry, int maxRequests) {
		Assert.assertEquals(_locks.toString(), maxRequests, _locks.size());

		String prefix =
			objectEntry.getExternalReferenceCode() + StringPool.COLON;

		String[] expectedKeys = new String[maxRequests];

		for (int i = 0; i < maxRequests; i++) {
			expectedKeys[i] = prefix + i;
		}

		Arrays.sort(expectedKeys);

		String[] keys = new String[_locks.size()];

		for (int i = 0; i < keys.length; i++) {
			Lock lock = _locks.get(i);

			keys[i] = lock.getKey();
		}

		Arrays.sort(keys);

		Assert.assertArrayEquals(expectedKeys, keys);
	}

	private void _release(Lock lock) throws Exception {
		_releaseMethod.invoke(null, lock);
	}

	private Semaphore _setUpSemaphore(int permits) throws Exception {
		Object semaphoreDCLSingleton = _semaphoreDCLSingletonField.get(null);

		_destroyMethod.invoke(semaphoreDCLSingleton, new Object[] {null});

		Semaphore semaphore = new Semaphore(permits);

		_getSingletonMethod.invoke(
			semaphoreDCLSingleton, (Supplier<Semaphore>)() -> semaphore);

		Map<String, Long> expirationTimes =
			(Map<String, Long>)_expirationTimesField.get(null);

		expirationTimes.clear();

		return semaphore;
	}

	private static final String _CONFIGURATION_PID =
		"com.liferay.ai.hub.configuration.AIHubAgentConfiguration";

	private static Method _acquireMethod;
	private static Method _destroyMethod;
	private static Field _expirationTimesField;
	private static Method _getSingletonMethod;
	private static String _originalName;
	private static PermissionChecker _originalPermissionChecker;
	private static Method _releaseMethod;
	private static Field _semaphoreDCLSingletonField;

	@Inject
	private static SiteInitializerRegistry _siteInitializerRegistry;

	@Inject
	private static SupervisorAgent _supervisorAgent;

	@DeleteAfterTestRun
	private List<AccountEntry> _accountEntries = new ArrayList<>();

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private AccountEntryUserRelLocalService _accountEntryUserRelLocalService;

	private final List<Lock> _locks = new ArrayList<>();

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@DeleteAfterTestRun
	private List<ObjectEntry> _objectEntries = new ArrayList<>();

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@DeleteAfterTestRun
	private List<User> _users = new ArrayList<>();

}