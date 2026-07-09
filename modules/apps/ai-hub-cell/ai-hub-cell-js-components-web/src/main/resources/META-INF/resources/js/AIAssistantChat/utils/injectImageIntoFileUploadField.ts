/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

/**
 * Injects a generated image into a file-upload field as a pending File, without
 * persisting it. The image travels to the server only when the content is
 * published, exactly like a file the user picked from their computer.
 */
export default function injectImageIntoFileUploadField(
	fieldId: string,
	imageDataURL: string
) {
	const fileInput = document.getElementById(
		`${fieldId}-file-upload`
	) as HTMLInputElement | null;

	if (!fileInput) {
		return;
	}

	const [metadata, base64] = imageDataURL.split(',');

	const mimeType = metadata.match(/data:(.*?);/)?.[1] ?? 'image/png';

	const byteCharacters = atob(base64 ?? '');

	const byteNumbers = new Uint8Array(byteCharacters.length);

	for (let index = 0; index < byteCharacters.length; index++) {
		byteNumbers[index] = byteCharacters.charCodeAt(index);
	}

	const file = new File(
		[byteNumbers],
		`AI-image-${crypto.randomUUID()}.png`,
		{type: mimeType}
	);

	const dataTransfer = new DataTransfer();

	dataTransfer.items.add(file);

	fileInput.files = dataTransfer.files;

	fileInput.dispatchEvent(new Event('change', {bubbles: true}));
}
