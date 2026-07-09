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
		blob: () => Promise.resolve(new Blob(['image'])),
		json: () => Promise.resolve({id: 1}),
		ok: true,
	};
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

	it('saves the single image to the target folder when the save button is clicked', async () => {
		render(
			<ImageMessageBalloon
				folderId={123}
				images={[IMAGE_ONE]}
				message="message"
			/>
		);

		fireEvent.click(screen.getByRole('button', {name: 'save-image'}));

		await waitFor(() => expect(mockFetch).toHaveBeenCalledWith(IMAGE_ONE));

		const postCall = mockFetch.mock.calls.find(
			([, init]) => (init as RequestInit)?.method === 'POST'
		);

		expect(postCall?.[0]).toContain('/document-folders/123/documents');
	});

	it('selects every image by default and saves only the images still selected', async () => {
		render(
			<ImageMessageBalloon
				folderId={123}
				images={[IMAGE_ONE, IMAGE_TWO]}
				message="message"
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

		await waitFor(() => expect(mockFetch).toHaveBeenCalledWith(IMAGE_ONE));

		expect(mockFetch).not.toHaveBeenCalledWith(IMAGE_TWO);
	});

	it('disables the save button when no image is selected', () => {
		render(
			<ImageMessageBalloon
				images={[IMAGE_ONE, IMAGE_TWO]}
				message="message"
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
				folderId={123}
				images={[IMAGE_ONE, IMAGE_TWO]}
				message="message"
				onRegenerate={jest.fn()}
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
