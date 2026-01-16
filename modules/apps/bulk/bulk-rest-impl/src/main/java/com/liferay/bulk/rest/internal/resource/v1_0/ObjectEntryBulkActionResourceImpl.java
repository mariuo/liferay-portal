/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.bulk.rest.internal.resource.v1_0;

import com.liferay.bulk.rest.dto.v1_0.BulkAction;
import com.liferay.bulk.rest.dto.v1_0.BulkActionTask;
import com.liferay.bulk.rest.dto.v1_0.DefaultPermissionBulkAction;
import com.liferay.bulk.rest.dto.v1_0.KeywordBulkAction;
import com.liferay.bulk.rest.dto.v1_0.TaxonomyCategoryBulkAction;
import com.liferay.bulk.rest.internal.selection.v1_0.BulkActionBulkSelectionFactory;
import com.liferay.bulk.rest.resource.v1_0.ObjectEntryBulkActionResource;
import com.liferay.bulk.selection.BulkSelection;
import com.liferay.bulk.selection.BulkSelectionAction;
import com.liferay.bulk.selection.BulkSelectionFactoryRegistry;
import com.liferay.bulk.selection.BulkSelectionRunner;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Localization;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Alejandro Tardín
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/object-entry-bulk-action.properties",
	scope = ServiceScope.PROTOTYPE,
	service = ObjectEntryBulkActionResource.class
)
public class ObjectEntryBulkActionResourceImpl
	extends BaseObjectEntryBulkActionResourceImpl {

	@Override
	public BulkActionTask postObjectEntryBulk(
			String blueprintExternalReferenceCode, Boolean emptySearch,
			String entryClassNames, String scope, String search, Filter filter,
			Pagination pagination, Sort[] sorts, BulkAction bulkAction)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-17564")) {

			throw new UnsupportedOperationException();
		}

		BulkActionBulkSelectionFactory bulkActionBulkSelectionFactory =
			_getBulkActionBulkSelectionFactory(
				blueprintExternalReferenceCode, bulkAction, emptySearch,
				entryClassNames, filter, scope, search, sorts);

		BulkSelection<Object> bulkSelection =
			bulkActionBulkSelectionFactory.create();

		if (bulkSelection.getSize() == 0) {
			return new BulkActionTask();
		}

		BulkAction.Type type = bulkAction.getType();

		String typeString = type.toString();

		BulkActionTask bulkActionTask = new BulkActionTask() {
			{
				setActionName(() -> GetterUtil.getString(typeString));

				setExecuteStatus(() -> GetterUtil.getString("initial"));

				setType(() -> GetterUtil.getString(typeString));
			}
		};

		_bulkSelectionRunner.run(
			contextUser, bulkSelection, _deleteObjectEntryBulkSelectionAction,
			_getInputMap(bulkAction, bulkActionTask, type));

		return bulkActionTask;
	}

	private BulkActionBulkSelectionFactory _getBulkActionBulkSelectionFactory(
		String blueprintExternalReferenceCode, BulkAction bulkAction,
		Boolean emptySearch, String entryClassNames, Filter filter,
		String scope, String search, Sort[] sorts) {

		return new BulkActionBulkSelectionFactory.Builder(
		).acceptLanguage(
			contextAcceptLanguage
		).blueprintExternalReferenceCode(
			blueprintExternalReferenceCode
		).bulkAction(
			bulkAction
		).bulkSelectionFactoryRegistry(
			_bulkSelectionFactoryRegistry
		).company(
			contextCompany
		).emptySearch(
			emptySearch
		).entryClassNames(
			entryClassNames
		).filter(
			filter
		).filterFactory(
			_filterFactory
		).groupLocalService(
			_groupLocalService
		).httpServletRequest(
			contextHttpServletRequest
		).localization(
			_localization
		).objectDefinitionLocalService(
			_objectDefinitionLocalService
		).objectEntryLocalService(
			_objectEntryLocalService
		).scope(
			scope
		).search(
			search
		).searcher(
			_searcher
		).searchRequestBuilderFactory(
			_searchRequestBuilderFactory
		).sorts(
			sorts
		).user(
			contextUser
		).build();
	}

	private Map<String, Serializable> _getInputMap(
		BulkAction bulkAction, BulkActionTask bulkActionTask,
		BulkAction.Type type) {

		HashMapBuilder.HashMapWrapper<String, Serializable> hashMapWrapper =
			HashMapBuilder.<String, Serializable>put(
				"bulkActionTaskId", bulkActionTask.getId());

		if (BulkAction.Type.DEFAULT_PERMISSION_BULK_ACTION.equals(type)) {
			DefaultPermissionBulkAction defaultPermissionBulkAction =
				(DefaultPermissionBulkAction)bulkAction;

			return hashMapWrapper.put(
				"defaultPermissions",
				defaultPermissionBulkAction::getDefaultPermissions
			).put(
				"roleKey", defaultPermissionBulkAction.getRoleKey()
			).build();
		}
		else if (BulkAction.Type.DELETE_BULK_ACTION.equals(type)) {
			return hashMapWrapper.build();
		}
		else if (BulkAction.Type.KEYWORD_BULK_ACTION.equals(type)) {
			KeywordBulkAction keywordBulkAction = (KeywordBulkAction)bulkAction;

			return hashMapWrapper.put(
				"append", GetterUtil.getBoolean(keywordBulkAction.getAppend())
			).put(
				"toAddTagNames", keywordBulkAction.getKeywordsToAdd()
			).put(
				"toRemoveTagNames", keywordBulkAction.getKeywordsToRemove()
			).build();
		}
		else if (BulkAction.Type.RESET_PERMISSION_BULK_ACTION.equals(type)) {
			return hashMapWrapper.build();
		}
		else if (BulkAction.Type.TAXONOMY_CATEGORY_BULK_ACTION.equals(type)) {
			TaxonomyCategoryBulkAction taxonomyCategoryBulkAction =
				(TaxonomyCategoryBulkAction)bulkAction;

			return hashMapWrapper.put(
				"append",
				GetterUtil.getBoolean(taxonomyCategoryBulkAction.getAppend())
			).put(
				"toAddCategoryIds",
				taxonomyCategoryBulkAction.getTaxonomyCategoryIdsToAdd()
			).put(
				"toRemoveCategoryIds",
				taxonomyCategoryBulkAction.getTaxonomyCategoryIdsToRemove()
			).build();
		}

		throw new UnsupportedOperationException();
	}

	@Reference
	private BulkSelectionFactoryRegistry _bulkSelectionFactoryRegistry;

	@Reference
	private BulkSelectionRunner _bulkSelectionRunner;

	@Reference(target = "(bulk.selection.action.key=delete.object.entry)")
	private BulkSelectionAction<Object> _deleteObjectEntryBulkSelectionAction;

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Localization _localization;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

}