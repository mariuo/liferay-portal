/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.request.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.dao.orm.EntityCacheUtil;
import com.liferay.portal.kernel.dao.orm.FinderCacheUtil;
import com.liferay.portal.kernel.lock.LockManagerUtil;
import com.liferay.portal.kernel.test.rule.TomcatClusterTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.cluster.tomcat.TomcatCluster;
import com.liferay.portal.test.cluster.tomcat.TomcatNode;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tina Tian
 */
@RunWith(Arquillian.class)
public class RequestHelperTest implements Serializable {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@ClassRule
	public static final TomcatClusterTestRule tomcatClusterTestRule =
		new TomcatClusterTestRule();

	@Test
	public void test() throws Exception {
		String className = RandomTestUtil.randomString();
		String key1 = RandomTestUtil.randomString();
		String key2 = RandomTestUtil.randomString();

		try {
			_lockAndAssert(className, key1);
			_lockAndAssert(_LOCK_CLASS_NAME, key1);
			_lockAndAssert(_LOCK_CLASS_NAME, key2);

			TomcatCluster.Builder builder1 =
				tomcatClusterTestRule.buildTomcatNode();

			TomcatNode tomcatNode1 = builder1.build();

			tomcatNode1.start(true);

			EntityCacheUtil.clearCache();
			FinderCacheUtil.clearCache();

			Assert.assertTrue(LockManagerUtil.isLocked(className, key1));
			Assert.assertFalse(
				LockManagerUtil.isLocked(_LOCK_CLASS_NAME, key1));
			Assert.assertFalse(
				LockManagerUtil.isLocked(_LOCK_CLASS_NAME, key2));

			_lockAndAssert(_LOCK_CLASS_NAME, key1);
			_lockAndAssert(_LOCK_CLASS_NAME, key2);

			TomcatCluster.Builder builder2 =
				tomcatClusterTestRule.buildTomcatNode();

			TomcatNode tomcatNode2 = builder2.build();

			tomcatNode2.start(true);

			EntityCacheUtil.clearCache();
			FinderCacheUtil.clearCache();

			Assert.assertTrue(LockManagerUtil.isLocked(className, key1));
			Assert.assertTrue(LockManagerUtil.isLocked(_LOCK_CLASS_NAME, key1));
			Assert.assertTrue(LockManagerUtil.isLocked(_LOCK_CLASS_NAME, key2));
		}
		finally {
			LockManagerUtil.unlock(className, key1);
			LockManagerUtil.unlock(_LOCK_CLASS_NAME, key1);
			LockManagerUtil.unlock(_LOCK_CLASS_NAME, key2);
		}
	}

	private void _lockAndAssert(String className, String key) throws Exception {
		LockManagerUtil.lock(
			TestPropsValues.getUserId(), className, key,
			RequestHelperTest.class.getName(), false, Time.HOUR);

		Assert.assertTrue(LockManagerUtil.isLocked(className, key));
	}

	private static final String _LOCK_CLASS_NAME =
		"com.liferay.ai.hub.internal.request.RequestUtil";

}