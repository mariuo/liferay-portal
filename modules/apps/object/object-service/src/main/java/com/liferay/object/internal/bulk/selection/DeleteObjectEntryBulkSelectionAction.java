/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.bulk.selection;

import com.liferay.bulk.selection.BulkSelection;
import com.liferay.bulk.selection.BulkSelectionAction;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.rest.manager.v1_0.DefaultObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.DefaultObjectEntryManagerProvider;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryFolderLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.trash.TrashHelper;

import java.io.Serializable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	property = "bulk.selection.action.key=delete.object.entry",
	service = BulkSelectionAction.class
)
public class DeleteObjectEntryBulkSelectionAction
	implements BulkSelectionAction<Object> {

	@Override
	public void execute(
			User user, BulkSelection<Object> bulkSelection,
			Map<String, Serializable> inputMap)
		throws Exception {

		Map<String, Serializable> values = new HashMap<>();

		values.put("numberOfItems", bulkSelection.getSize());

		String executionStatus = "completed";

		AtomicInteger numberOfFailedItems = new AtomicInteger(0);
		AtomicInteger numberOfSuccessfulItems = new AtomicInteger(0);

		try {
			bulkSelection.forEach(
				object -> {
					try {
						if (object instanceof ObjectEntry) {
							ObjectEntry objectObjectEntry = (ObjectEntry)object;

							ObjectDefinition objectDefinition =
								_objectDefinitionLocalService.
									getObjectDefinition(
										objectObjectEntry.
											getObjectDefinitionId());

							DefaultObjectEntryManager
								defaultObjectEntryManager =
									DefaultObjectEntryManagerProvider.provide(
										_objectEntryManagerRegistry.
											getObjectEntryManager(
												objectDefinition.getCompanyId(),
												objectDefinition.
													getStorageType()));

							defaultObjectEntryManager.deleteObjectEntry(
								objectDefinition,
								objectObjectEntry.getObjectEntryId());
						}
						else {
							ObjectEntryFolder objectEntryFolder =
								(ObjectEntryFolder)object;

							_deleteObjectEntryFolder(
								user.getUserId(), objectEntryFolder);
						}

						numberOfSuccessfulItems.getAndIncrement();
					}
					catch (Exception exception) {
						if (_log.isWarnEnabled()) {
							_log.warn(exception);
						}

						numberOfFailedItems.getAndIncrement();
					}
				});
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException);
			}

			executionStatus = "failed";
		}
		finally {
			values.put("completionDate", new Date());
			values.put("executionStatus", executionStatus);
			values.put("numberOfFailedItems", numberOfFailedItems.get());
			values.put(
				"numberOfSuccessfulItems", numberOfSuccessfulItems.get());
		}
	}

	private void _deleteObjectEntryFolder(
			long userId, ObjectEntryFolder objectEntryFolder)
		throws PortalException {

		if (FeatureFlagManagerUtil.isEnabled(
				objectEntryFolder.getCompanyId(), "LPD-17564") &&
			objectEntryFolder.isTrashable(_trashHelper)) {

			_objectEntryFolderLocalService.moveObjectEntryFolderToTrash(
				userId, objectEntryFolder, new ServiceContext());
		}
		else {
			_objectEntryFolderLocalService.deleteObjectEntryFolder(
				objectEntryFolder.getObjectEntryFolderId());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DeleteObjectEntryBulkSelectionAction.class);

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryFolderLocalService _objectEntryFolderLocalService;

	@Reference
	private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

	@Reference
	private TrashHelper _trashHelper;

}