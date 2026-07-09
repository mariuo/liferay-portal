/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fetch} from 'frontend-js-web';

const AI_GENERATED_KEYWORD = 'AI-generated';

const HEADLESS_DELIVERY_ENDPOINT = '/o/headless-delivery/v1.0';

interface SaveDestination {
	folderId?: number | string;
	siteId?: number | string;
}

function resolveContainerURL(
	{folderId, siteId}: SaveDestination,
	resource: string
) {
	if (folderId && Number(folderId) > 0) {
		return `${HEADLESS_DELIVERY_ENDPOINT}/document-folders/${folderId}/${resource}`;
	}

	return `${HEADLESS_DELIVERY_ENDPOINT}/sites/${siteId ?? Liferay.ThemeDisplay.getSiteGroupId()}/${resource}`;
}

async function createGeneratedImagesFolder({
	folderId,
	siteId,
}: SaveDestination) {
	const url = resolveContainerURL({folderId, siteId}, 'document-folders');

	const response = await fetch(url, {
		body: JSON.stringify({
			name: `${Liferay.Language.get(
				'images-generated-by-ai'
			)} ${crypto.randomUUID()}`,
		}),
		headers: new Headers({'Content-Type': 'application/json'}),
		method: 'POST',
	});

	if (!response.ok) {
		throw new Error(`Unable to create folder: ${response.statusText}`);
	}

	const folder = await response.json();

	return folder.id;
}

export async function saveGeneratedImages(
	images: string[],
	{folderId, siteId}: SaveDestination
) {
	const targetFolderId =
		images.length > 1
			? await createGeneratedImagesFolder({folderId, siteId})
			: folderId;

	const uploadURL = resolveContainerURL(
		{folderId: targetFolderId, siteId},
		'documents'
	);

	return Promise.all(
		images.map(async (image) => {
			const blob = await (await fetch(image)).blob();

			const formData = new FormData();

			formData.append(
				'file',
				blob,
				`AI-image-${crypto.randomUUID()}.png`
			);
			formData.append(
				'document',
				JSON.stringify({keywords: [AI_GENERATED_KEYWORD]})
			);

			const response = await fetch(uploadURL, {
				body: formData,
				method: 'POST',
			});

			if (!response.ok) {
				throw new Error(
					`Unable to save generated image: ${response.statusText}`
				);
			}

			return response.json();
		})
	);
}
