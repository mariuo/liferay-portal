/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';

import injectImageIntoFileUploadField from '../../../../src/main/resources/META-INF/resources/js/AIAssistantChat/utils/injectImageIntoFileUploadField';

const PNG_DATA_URL = 'data:image/png;base64,aGk=';

describe('injectImageIntoFileUploadField', () => {
	const originalDataTransfer = (global as any).DataTransfer;

	beforeEach(() => {

		// JSDOM ships no DataTransfer constructor, so stub the browser API the
		// util relies on to move a File into the file input.

		class FakeDataTransfer {
			items = {
				_files: [] as File[],
				add(file: File) {
					this._files.push(file);
				},
			};

			get files() {
				return this.items._files;
			}
		}

		(global as any).DataTransfer = FakeDataTransfer;
	});

	afterEach(() => {
		(global as any).DataTransfer = originalDataTransfer;

		document.body.innerHTML = '';
	});

	function renderField(fieldId: string) {
		const input = document.createElement('input');

		input.id = `${fieldId}-file-upload`;
		input.type = 'file';

		// JSDOM rejects assigning a fake FileList to input.files, so back the
		// property with a plain slot for the test.

		let files: File[] = [];

		Object.defineProperty(input, 'files', {
			configurable: true,
			get: () => files,
			set: (value) => {
				files = value;
			},
		});

		document.body.appendChild(input);

		return input;
	}

	it('injects the generated image as a pending file and notifies the field', () => {
		const input = renderField('field-1');

		const onChange = jest.fn();

		input.addEventListener('change', onChange);

		injectImageIntoFileUploadField('field-1', PNG_DATA_URL);

		const file = ((input.files as unknown) as File[])[0];

		expect(file).toBeInstanceOf(File);
		expect(file.type).toBe('image/png');
		expect(onChange).toHaveBeenCalledTimes(1);
	});

	it('does nothing when the field is not present', () => {
		expect(() =>
			injectImageIntoFileUploadField('missing', PNG_DATA_URL)
		).not.toThrow();
	});
});
