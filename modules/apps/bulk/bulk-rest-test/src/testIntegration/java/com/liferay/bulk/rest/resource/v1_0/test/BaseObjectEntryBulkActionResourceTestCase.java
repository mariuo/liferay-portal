/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.bulk.rest.resource.v1_0.test;

import com.liferay.bulk.rest.client.dto.v1_0.BulkActionTask;
import com.liferay.bulk.rest.client.http.HttpInvoker;
import com.liferay.bulk.rest.client.pagination.Page;
import com.liferay.bulk.rest.client.resource.v1_0.ObjectEntryBulkActionResource;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import jakarta.annotation.Generated;

import jakarta.ws.rs.core.MultivaluedHashMap;

import java.lang.reflect.Method;

import java.text.Format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Alejandro Tardín
 * @generated
 */
@Generated("")
public abstract class BaseObjectEntryBulkActionResourceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_format = FastDateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		_objectEntryBulkActionResource.setContextCompany(testCompany);

		_testCompanyAdminUser = UserTestUtil.getAdminUser(
			testCompany.getCompanyId());

		objectEntryBulkActionResource = ObjectEntryBulkActionResource.builder(
		).authentication(
			_testCompanyAdminUser.getEmailAddress(),
			PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(), 8080, "http"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	@After
	public void tearDown() throws Exception {
		GroupTestUtil.deleteGroup(irrelevantGroup);
		GroupTestUtil.deleteGroup(testGroup);
	}

	@Test
	public void testPostObjectEntryBulk() throws Exception {
		Assert.assertTrue(true);
	}

	protected void assertContains(
		Object objectEntryBulkAction, List<Object> objectEntryBulkActions) {

		boolean contains = false;

		for (Object item : objectEntryBulkActions) {
			if (equals(objectEntryBulkAction, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			objectEntryBulkActions + " does not contain " +
				objectEntryBulkAction,
			contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(
		Object objectEntryBulkAction1, Object objectEntryBulkAction2) {

		Assert.assertTrue(
			objectEntryBulkAction1 + " does not equal " +
				objectEntryBulkAction2,
			equals(objectEntryBulkAction1, objectEntryBulkAction2));
	}

	protected void assertEquals(
		List<Object> objectEntryBulkActions1,
		List<Object> objectEntryBulkActions2) {

		Assert.assertEquals(
			objectEntryBulkActions1.size(), objectEntryBulkActions2.size());

		for (int i = 0; i < objectEntryBulkActions1.size(); i++) {
			Object objectEntryBulkAction1 = objectEntryBulkActions1.get(i);
			Object objectEntryBulkAction2 = objectEntryBulkActions2.get(i);

			assertEquals(objectEntryBulkAction1, objectEntryBulkAction2);
		}
	}

	protected void assertEquals(
		BulkActionTask bulkActionTask1, BulkActionTask bulkActionTask2) {

		Assert.assertTrue(
			bulkActionTask1 + " does not equal " + bulkActionTask2,
			equals(bulkActionTask1, bulkActionTask2));
	}

	protected void assertEqualsIgnoringOrder(
		List<Object> objectEntryBulkActions1,
		List<Object> objectEntryBulkActions2) {

		Assert.assertEquals(
			objectEntryBulkActions1.size(), objectEntryBulkActions2.size());

		for (Object objectEntryBulkAction1 : objectEntryBulkActions1) {
			boolean contains = false;

			for (Object objectEntryBulkAction2 : objectEntryBulkActions2) {
				if (equals(objectEntryBulkAction1, objectEntryBulkAction2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				objectEntryBulkActions2 + " does not contain " +
					objectEntryBulkAction1,
				contains);
		}
	}

	protected void assertValid(Object objectEntryBulkAction) throws Exception {
		boolean valid = true;

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(Page<Object> page) {
		assertValid(page, Collections.emptyMap());
	}

	protected void assertValid(
		Page<Object> page, Map<String, Map<String, String>> expectedActions) {

		boolean valid = false;

		java.util.Collection<Object> objectEntryBulkActions = page.getItems();

		int size = objectEntryBulkActions.size();

		if ((page.getLastPage() > 0) && (page.getPage() > 0) &&
			(page.getPageSize() > 0) && (page.getTotalCount() > 0) &&
			(size > 0)) {

			valid = true;
		}

		Assert.assertTrue(valid);

		assertValid(page.getActions(), expectedActions);
	}

	protected void assertValid(
		Map<String, Map<String, String>> actions1,
		Map<String, Map<String, String>> actions2) {

		for (String key : actions2.keySet()) {
			Map action = actions1.get(key);

			Assert.assertNotNull(key + " does not contain an action", action);

			Map<String, String> expectedAction = actions2.get(key);

			Assert.assertEquals(
				expectedAction.get("method"), action.get("method"));
			Assert.assertEquals(expectedAction.get("href"), action.get("href"));
		}
	}

	protected void assertValid(BulkActionTask bulkActionTask) {
		boolean valid = true;

		if (bulkActionTask.getExternalReferenceCode() == null) {
			valid = false;
		}

		if (bulkActionTask.getId() == null) {
			valid = false;
		}

		for (String additionalAssertFieldName :
				getAdditionalBulkActionTaskAssertFieldNames()) {

			if (Objects.equals("actionName", additionalAssertFieldName)) {
				if (bulkActionTask.getActionName() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("author", additionalAssertFieldName)) {
				if (bulkActionTask.getAuthor() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("completionDate", additionalAssertFieldName)) {
				if (bulkActionTask.getCompletionDate() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("createdDate", additionalAssertFieldName)) {
				if (bulkActionTask.getCreatedDate() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("executeStatus", additionalAssertFieldName)) {
				if (bulkActionTask.getExecuteStatus() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (bulkActionTask.getExternalReferenceCode() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("numberOfItems", additionalAssertFieldName)) {
				if (bulkActionTask.getNumberOfItems() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("type", additionalAssertFieldName)) {
				if (bulkActionTask.getType() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected String[] getAdditionalAssertFieldNames() {
		return new String[0];
	}

	protected String[] getAdditionalBulkActionTaskAssertFieldNames() {
		return new String[0];
	}

	protected List<GraphQLField> getGraphQLFields() throws Exception {
		List<GraphQLField> graphQLFields = new ArrayList<>();

		return graphQLFields;
	}

	protected List<GraphQLField> getGraphQLFields(
			java.lang.reflect.Field... fields)
		throws Exception {

		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field : fields) {
			com.liferay.portal.vulcan.graphql.annotation.GraphQLField
				vulcanGraphQLField = field.getAnnotation(
					com.liferay.portal.vulcan.graphql.annotation.GraphQLField.
						class);

			if (vulcanGraphQLField != null) {
				Class<?> clazz = field.getType();

				if (clazz.isArray()) {
					clazz = clazz.getComponentType();
				}

				List<GraphQLField> childrenGraphQLFields = getGraphQLFields(
					getDeclaredFields(clazz));

				graphQLFields.add(
					new GraphQLField(field.getName(), childrenGraphQLFields));
			}
		}

		return graphQLFields;
	}

	protected String[] getIgnoredEntityFieldNames() {
		return new String[0];
	}

	protected boolean equals(
		Object objectEntryBulkAction1, Object objectEntryBulkAction2) {

		if (objectEntryBulkAction1 == objectEntryBulkAction2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		Map<String, Object> map1, Map<String, Object> map2) {

		if (Objects.equals(map1.keySet(), map2.keySet())) {
			for (Map.Entry<String, Object> entry : map1.entrySet()) {
				if (entry.getValue() instanceof Map) {
					if (!equals(
							(Map)entry.getValue(),
							(Map)map2.get(entry.getKey()))) {

						return false;
					}
				}
				else if (!Objects.deepEquals(
							entry.getValue(), map2.get(entry.getKey()))) {

					return false;
				}
			}

			return true;
		}

		return false;
	}

	protected boolean equals(
		BulkActionTask bulkActionTask1, BulkActionTask bulkActionTask2) {

		if (bulkActionTask1 == bulkActionTask2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalBulkActionTaskAssertFieldNames()) {

			if (Objects.equals("actionName", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						bulkActionTask1.getActionName(),
						bulkActionTask2.getActionName())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("author", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						bulkActionTask1.getAuthor(),
						bulkActionTask2.getAuthor())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("completionDate", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						bulkActionTask1.getCompletionDate(),
						bulkActionTask2.getCompletionDate())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("createdDate", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						bulkActionTask1.getCreatedDate(),
						bulkActionTask2.getCreatedDate())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("executeStatus", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						bulkActionTask1.getExecuteStatus(),
						bulkActionTask2.getExecuteStatus())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						bulkActionTask1.getExternalReferenceCode(),
						bulkActionTask2.getExternalReferenceCode())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("id", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						bulkActionTask1.getId(), bulkActionTask2.getId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("numberOfItems", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						bulkActionTask1.getNumberOfItems(),
						bulkActionTask2.getNumberOfItems())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("type", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						bulkActionTask1.getType(), bulkActionTask2.getType())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected java.lang.reflect.Field[] getDeclaredFields(Class clazz)
		throws Exception {

		if (clazz.getClassLoader() == null) {
			return new java.lang.reflect.Field[0];
		}

		return TransformUtil.transform(
			ReflectionUtil.getDeclaredFields(clazz),
			field -> {
				if (field.isSynthetic()) {
					return null;
				}

				return field;
			},
			java.lang.reflect.Field.class);
	}

	protected java.util.Collection<EntityField> getEntityFields()
		throws Exception {

		if (!(_objectEntryBulkActionResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_objectEntryBulkActionResource;

		EntityModel entityModel = entityModelResource.getEntityModel(
			new MultivaluedHashMap());

		if (entityModel == null) {
			return Collections.emptyList();
		}

		Map<String, EntityField> entityFieldsMap =
			entityModel.getEntityFieldsMap();

		return entityFieldsMap.values();
	}

	protected List<EntityField> getEntityFields(EntityField.Type type)
		throws Exception {

		return TransformUtil.transform(
			getEntityFields(),
			entityField -> {
				if (!Objects.equals(entityField.getType(), type) ||
					ArrayUtil.contains(
						getIgnoredEntityFieldNames(), entityField.getName())) {

					return null;
				}

				return entityField;
			});
	}

	protected String getFilterString(
		EntityField entityField, String operator,
		Object objectEntryBulkAction) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		throw new IllegalArgumentException(
			"Invalid entity field " + entityFieldName);
	}

	protected String invoke(String query) throws Exception {
		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.body(
			JSONUtil.put(
				"query", query
			).toString(),
			"application/json");
		httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);
		httpInvoker.path("http://localhost:8080/o/graphql");
		httpInvoker.userNameAndPassword(
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD);

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		return httpResponse.getContent();
	}

	protected JSONObject invokeGraphQLMutation(GraphQLField graphQLField)
		throws Exception {

		GraphQLField mutationGraphQLField = new GraphQLField(
			"mutation", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(mutationGraphQLField.toString()));
	}

	protected JSONObject invokeGraphQLQuery(GraphQLField graphQLField)
		throws Exception {

		GraphQLField queryGraphQLField = new GraphQLField(
			"query", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(queryGraphQLField.toString()));
	}

	protected BulkActionTask randomBulkActionTask() throws Exception {
		return new BulkActionTask() {
			{
				actionName = RandomTestUtil.randomString();
				author = RandomTestUtil.randomString();
				completionDate = RandomTestUtil.nextDate();
				createdDate = RandomTestUtil.nextDate();
				executeStatus = RandomTestUtil.randomString();
				externalReferenceCode = RandomTestUtil.randomString();
				id = RandomTestUtil.randomLong();
				numberOfItems = RandomTestUtil.randomInt();
				type = RandomTestUtil.randomString();
			}
		};
	}

	protected ObjectEntryBulkActionResource objectEntryBulkActionResource;
	protected com.liferay.portal.kernel.model.Group irrelevantGroup;
	protected com.liferay.portal.kernel.model.Company testCompany;
	protected com.liferay.portal.kernel.model.Group testGroup;

	protected static class BeanTestUtil {

		public static void copyProperties(Object source, Object target)
			throws Exception {

			Class<?> sourceClass = source.getClass();

			Class<?> targetClass = target.getClass();

			for (java.lang.reflect.Field field :
					_getAllDeclaredFields(sourceClass)) {

				if (field.isSynthetic()) {
					continue;
				}

				Method getMethod = _getMethod(
					sourceClass, field.getName(), "get");

				try {
					Method setMethod = _getMethod(
						targetClass, field.getName(), "set",
						getMethod.getReturnType());

					setMethod.invoke(target, getMethod.invoke(source));
				}
				catch (Exception e) {
					continue;
				}
			}
		}

		public static boolean hasProperty(Object bean, String name) {
			Method setMethod = _getMethod(
				bean.getClass(), "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod != null) {
				return true;
			}

			return false;
		}

		public static void setProperty(Object bean, String name, Object value)
			throws Exception {

			Class<?> clazz = bean.getClass();

			Method setMethod = _getMethod(
				clazz, "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod == null) {
				throw new NoSuchMethodException();
			}

			Class<?>[] parameterTypes = setMethod.getParameterTypes();

			setMethod.invoke(bean, _translateValue(parameterTypes[0], value));
		}

		private static List<java.lang.reflect.Field> _getAllDeclaredFields(
			Class<?> clazz) {

			List<java.lang.reflect.Field> fields = new ArrayList<>();

			while ((clazz != null) && (clazz != Object.class)) {
				for (java.lang.reflect.Field field :
						clazz.getDeclaredFields()) {

					fields.add(field);
				}

				clazz = clazz.getSuperclass();
			}

			return fields;
		}

		private static Method _getMethod(Class<?> clazz, String name) {
			for (Method method : clazz.getMethods()) {
				if (name.equals(method.getName()) &&
					(method.getParameterCount() == 1) &&
					_parameterTypes.contains(method.getParameterTypes()[0])) {

					return method;
				}
			}

			return null;
		}

		private static Method _getMethod(
				Class<?> clazz, String fieldName, String prefix,
				Class<?>... parameterTypes)
			throws Exception {

			return clazz.getMethod(
				prefix + StringUtil.upperCaseFirstLetter(fieldName),
				parameterTypes);
		}

		private static Object _translateValue(
			Class<?> parameterType, Object value) {

			if ((value instanceof Integer) &&
				parameterType.equals(Long.class)) {

				Integer intValue = (Integer)value;

				return intValue.longValue();
			}

			return value;
		}

		private static final Set<Class<?>> _parameterTypes = new HashSet<>(
			Arrays.asList(
				Boolean.class, Date.class, Double.class, Integer.class,
				Long.class, Map.class, String.class));

	}

	protected class GraphQLField {

		public GraphQLField(String key, GraphQLField... graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(String key, List<GraphQLField> graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			GraphQLField... graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = Arrays.asList(graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			List<GraphQLField> graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = graphQLFields;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(_key);

			if (!_parameterMap.isEmpty()) {
				sb.append("(");

				for (Map.Entry<String, Object> entry :
						_parameterMap.entrySet()) {

					sb.append(entry.getKey());
					sb.append(": ");
					sb.append(entry.getValue());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append(")");
			}

			if (!_graphQLFields.isEmpty()) {
				sb.append("{");

				for (GraphQLField graphQLField : _graphQLFields) {
					sb.append(graphQLField.toString());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append("}");
			}

			return sb.toString();
		}

		private final List<GraphQLField> _graphQLFields;
		private final String _key;
		private final Map<String, Object> _parameterMap;

	}

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(BaseObjectEntryBulkActionResourceTestCase.class);

	private static Format _format;

	private com.liferay.portal.kernel.model.User _testCompanyAdminUser;

	@Inject
	private com.liferay.bulk.rest.resource.v1_0.ObjectEntryBulkActionResource
		_objectEntryBulkActionResource;

}