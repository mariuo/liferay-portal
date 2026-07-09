/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export interface SavedDocument {
	id: number;
	title: string;
}

export default function setFileUploadFieldValue(
	fieldId: string,
	{id, title}: SavedDocument
) {
	const fileInput = document.getElementById(
		`${fieldId}-file-upload`
	) as HTMLInputElement | null;

	const fileName = document.querySelector(
		`[data-ai-assistant-field-id="${fieldId}"] .forms-file-upload-file-name`
	) as HTMLElement | null;

	const removeButton = document.getElementById(
		`${fieldId}-file-upload-remove-button`
	);

	if (fileInput) {
		fileInput.value = String(id);
	}

	if (fileName) {
		fileName.innerText = title;
	}

	removeButton?.classList.remove('d-none');
}
