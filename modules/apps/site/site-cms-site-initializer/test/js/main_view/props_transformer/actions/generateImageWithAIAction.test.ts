/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import generateImageWithAIAction from '../../../../../src/main/resources/META-INF/resources/js/main_view/props_transformer/actions/generateImageWithAIAction';

describe('generateImageWithAIAction', () => {
	let fireSpy: jest.SpyInstance;

	beforeEach(() => {
		fireSpy = jest.spyOn(Liferay, 'fire').mockImplementation(() => {});
	});

	afterEach(() => {
		fireSpy.mockRestore();
	});

	it('fires the open-chat event full size with the given message', () => {
		generateImageWithAIAction({
			action: 'generateImageWithAI',
			message: 'Generate Single Image',
		});

		expect(fireSpy).toHaveBeenCalledWith('openAIAssistantChat', {
			fullSize: true,
			message: 'Generate Single Image',
		});
	});
});
