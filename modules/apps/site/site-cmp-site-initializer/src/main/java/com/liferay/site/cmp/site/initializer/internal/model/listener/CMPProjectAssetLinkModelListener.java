/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cmp.site.initializer.internal.model.listener;

import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Rejects a duplicate link between the same asset and the same project. A link
 * lives in the project's depot, so a group-scoped count of matching
 * {@code (className, classExternalReferenceCode, scopeKey)} rows is a per-project
 * uniqueness check. This is used instead of the built-in unique composite key
 * validation rule, which requires non-system fields the link object cannot have.
 *
 * @author Guilherme Camacho
 */
@Component(service = ModelListener.class)
public class CMPProjectAssetLinkModelListener
	extends BaseModelListener<ObjectEntry> {

	@Override
	public void onBeforeCreate(ObjectEntry objectEntry)
		throws ModelListenerException {

		try {
			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.fetchObjectDefinition(
					objectEntry.getObjectDefinitionId());

			if ((objectDefinition == null) ||
				!StringUtil.equals(
					objectDefinition.getExternalReferenceCode(),
					"L_CMP_PROJECT_ASSET_RELATIONSHIP")) {

				return;
			}

			Map<String, Serializable> values = objectEntry.getValues();

			int count = _objectEntryLocalService.getValuesListCount(
				new Long[] {objectEntry.getGroupId()}, 0, 0,
				objectEntry.getObjectDefinitionId(),
				_filterFactory.create(
					StringBundler.concat(
						"className eq '",
						_escape(GetterUtil.getString(values.get("className"))),
						"' and classExternalReferenceCode eq '",
						_escape(
							GetterUtil.getString(
								values.get("classExternalReferenceCode"))),
						"' and scopeKey eq '",
						_escape(GetterUtil.getString(values.get("scopeKey"))),
						"'"),
					objectDefinition),
				false, null);

			if (count > 0) {
				throw new ModelListenerException(
					"This asset is already linked to this project");
			}
		}
		catch (ModelListenerException modelListenerException) {
			throw modelListenerException;
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	private String _escape(String value) {
		return StringUtil.replace(value, '\'', "''");
	}

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

}