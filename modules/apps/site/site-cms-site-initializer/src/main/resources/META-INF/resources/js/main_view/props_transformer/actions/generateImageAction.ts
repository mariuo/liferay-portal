/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export const OPEN_GENERATE_IMAGE_FLOW_EVENT =
	'openAIAssistantGenerateImageFlow';

export default function generateImageAction(data: {
	objectEntryFolderExternalReferenceCode?: string;
}) {
	Liferay.fire(OPEN_GENERATE_IMAGE_FLOW_EVENT, {
		objectEntryFolderExternalReferenceCode:
			data?.objectEntryFolderExternalReferenceCode,
	});
}
