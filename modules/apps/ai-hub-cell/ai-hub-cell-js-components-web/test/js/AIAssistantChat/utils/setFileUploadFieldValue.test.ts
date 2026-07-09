/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';

import setFileUploadFieldValue from '../../../../src/main/resources/META-INF/resources/js/AIAssistantChat/utils/setFileUploadFieldValue';

describe('setFileUploadFieldValue', () => {
	afterEach(() => {
		document.body.innerHTML = '';
	});

	function renderField(fieldId: string) {
		const field = document.createElement('div');

		field.dataset.aiAssistantFieldId = fieldId;
		field.innerHTML =
			`<input id="${fieldId}-file-upload" type="text" />` +
			'<span class="forms-file-upload-file-name"></span>' +
			`<button class="d-none" id="${fieldId}-file-upload-remove-button"></button>`;

		document.body.appendChild(field);
	}

	it('stores the document id in the field input and reveals the remove button', () => {
		renderField('field-1');

		setFileUploadFieldValue('field-1', {id: 7, title: 'AI-image.png'});

		expect(
			(document.getElementById('field-1-file-upload') as HTMLInputElement)
				.value
		).toBe('7');
		expect(
			document.getElementById('field-1-file-upload-remove-button')
		).not.toHaveClass('d-none');
	});

	it('does nothing when the field is not present', () => {
		expect(() =>
			setFileUploadFieldValue('missing', {id: 1, title: 'x.png'})
		).not.toThrow();
	});
});
