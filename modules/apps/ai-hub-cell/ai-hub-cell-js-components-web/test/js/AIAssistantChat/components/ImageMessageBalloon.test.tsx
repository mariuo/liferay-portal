/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';

// eslint-disable-next-line @liferay/portal/no-cross-module-deep-import, @liferay/no-extraneous-dependencies
import {checkAccessibility} from '@liferay/layout-js-components-web/test/__lib__/index';
import {act, fireEvent, render, screen, waitFor} from '@testing-library/react';
import {fetch} from 'frontend-js-web';
import React from 'react';

import ImageMessageBalloon from '../../../../src/main/resources/META-INF/resources/js/AIAssistantChat/components/ImageMessageBalloon';

jest.mock('frontend-js-web', () => ({fetch: jest.fn()}));

const mockFetch = fetch as jest.MockedFunction<typeof fetch>;

const IMAGE_ONE = 'data:image/png;base64,one';
const IMAGE_TWO = 'data:image/png;base64,two';

function response() {
	return {
		json: () => Promise.resolve({id: 1, title: 'AI-image.png'}),
		ok: true,
	};
}

function lastPostBody() {
	const postCall = mockFetch.mock.calls.find(
		([, init]) => (init as RequestInit)?.method === 'POST'
	);

	return JSON.parse((postCall?.[1] as RequestInit).body as string);
}

describe('ImageMessageBalloon', () => {
	beforeEach(() => {
		mockFetch.mockReset();
		mockFetch.mockResolvedValue(response() as never);
	});

	it('renders the message and a single generated image', () => {
		render(
			<ImageMessageBalloon
				images={[IMAGE_ONE]}
				message="Your image is ready!"
			/>
		);

		expect(screen.getByText('Your image is ready!')).toBeInTheDocument();
		expect(screen.getByAltText('generated-image')).toHaveAttribute(
			'src',
			IMAGE_ONE
		);
	});

	it('renders the image without a caption when no message is provided', () => {
		render(<ImageMessageBalloon images={[IMAGE_ONE]} />);

		expect(screen.getByAltText('generated-image')).toHaveAttribute(
			'src',
			IMAGE_ONE
		);
	});

	it('saves the single image to the group basic-documents endpoint when the save button is clicked', async () => {
		render(
			<ImageMessageBalloon
				images={[IMAGE_ONE]}
				message="message"
				saveProps={{groupId: 123}}
			/>
		);

		fireEvent.click(screen.getByRole('button', {name: 'save-image'}));

		await waitFor(() =>
			expect(mockFetch).toHaveBeenCalledWith(
				'/o/cms/basic-documents/scopes/123',
				expect.objectContaining({method: 'POST'})
			)
		);

		expect(lastPostBody().file.fileBase64).toBe('one');
	});

	it('selects every image by default and saves only the images still selected', async () => {
		render(
			<ImageMessageBalloon
				images={[IMAGE_ONE, IMAGE_TWO]}
				message="message"
				saveProps={{groupId: 123}}
			/>
		);

		const checkboxes = screen.getAllByRole('checkbox', {
			name: 'generated-image',
		});

		expect(checkboxes).toHaveLength(2);
		expect(checkboxes[0]).toBeChecked();
		expect(checkboxes[1]).toBeChecked();

		fireEvent.click(checkboxes[1]);

		expect(checkboxes[1]).not.toBeChecked();

		fireEvent.click(screen.getByRole('button', {name: 'save-image'}));

		await waitFor(() =>
			expect(
				mockFetch.mock.calls.filter(
					([, init]) => (init as RequestInit)?.method === 'POST'
				)
			).toHaveLength(1)
		);

		expect(lastPostBody().file.fileBase64).toBe('one');
	});

	it('writes the saved document into the file-upload field in field mode', async () => {
		const field = document.createElement('div');

		field.dataset.aiAssistantFieldId = 'field-1';
		field.innerHTML =
			'<input id="field-1-file-upload" type="text" />' +
			'<span class="forms-file-upload-file-name"></span>' +
			'<button class="d-none" id="field-1-file-upload-remove-button"></button>';

		document.body.appendChild(field);

		render(
			<ImageMessageBalloon
				images={[IMAGE_ONE]}
				message="message"
				saveProps={{fieldId: 'field-1', groupId: 123}}
			/>
		);

		fireEvent.click(screen.getByRole('button', {name: 'save-image'}));

		const fileInput = document.getElementById(
			'field-1-file-upload'
		) as HTMLInputElement;

		await waitFor(() => expect(fileInput.value).toBe('1'));

		expect(
			document.getElementById('field-1-file-upload-remove-button')
		).not.toHaveClass('d-none');

		field.remove();
	});

	it('disables the save button when no image is selected', () => {
		render(
			<ImageMessageBalloon
				images={[IMAGE_ONE, IMAGE_TWO]}
				message="message"
				saveProps={{groupId: 123}}
			/>
		);

		const checkboxes = screen.getAllByRole('checkbox', {
			name: 'generated-image',
		});

		fireEvent.click(checkboxes[0]);
		fireEvent.click(checkboxes[1]);

		expect(screen.getByRole('button', {name: 'save-image'})).toBeDisabled();
	});

	it('disables every action and the image selection while saving', async () => {
		let release: () => void = () => {};

		const gate = new Promise<void>((resolve) => {
			release = resolve;
		});

		mockFetch.mockImplementation(
			() => gate.then(() => response()) as never
		);

		render(
			<ImageMessageBalloon
				images={[IMAGE_ONE, IMAGE_TWO]}
				message="message"
				onRegenerate={jest.fn()}
				saveProps={{groupId: 123}}
			/>
		);

		const saveButton = screen.getByRole('button', {name: 'save-images'});

		fireEvent.click(saveButton);

		await waitFor(() => expect(saveButton).toBeDisabled());

		expect(saveButton).toHaveTextContent('saving');
		expect(screen.getByRole('button', {name: 'regenerate'})).toBeDisabled();

		screen
			.getAllByRole('checkbox', {name: 'generated-image'})
			.forEach((checkbox) => expect(checkbox).toBeDisabled());

		await act(async () => {
			release();
		});

		await waitFor(() => expect(saveButton).toBeEnabled());
	});

	it('calls onRegenerate when the regenerate button is clicked', () => {
		const onRegenerate = jest.fn();

		render(
			<ImageMessageBalloon
				images={[IMAGE_ONE]}
				message="message"
				onRegenerate={onRegenerate}
			/>
		);

		fireEvent.click(screen.getByRole('button', {name: 'regenerate'}));

		expect(onRegenerate).toHaveBeenCalledTimes(1);
	});

	it('has no accessibility violations', async () => {
		const {container} = render(
			<ImageMessageBalloon
				images={[IMAGE_ONE, IMAGE_TWO]}
				message="Your image is ready!"
				onRegenerate={jest.fn()}
			/>
		);

		await checkAccessibility({context: container});
	});
});
