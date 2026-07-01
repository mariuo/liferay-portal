/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayIcon from '@clayui/icon';
import {AIAssistantChat} from '@liferay/ai-hub-cell-js-components-web';
import {openToast} from 'frontend-js-components-web';
import {sub} from 'frontend-js-web';
import React, {useCallback, useEffect, useRef, useState} from 'react';

import ApiHelper from '../../common/services/ApiHelper';
import {AssetLibrary} from '../../common/types/AssetLibrary';
import {openCMSModal} from '../../common/utils/openCMSModal';
import CreationModalContent from '../modal/CreationModalContent';

const DEFAULT_FILES_FOLDER_EXTERNAL_REFERENCE_CODE = 'L_FILES';

export default function FilesAIAssistant({
	filesDataSetId,
	spaceGroupId,
	spaces,
}: {
	filesDataSetId: string;
	spaceGroupId: number;
	spaces: AssetLibrary[];
}) {
	const [autoStartFlow, setAutoStartFlow] = useState(false);
	const [open, setOpen] = useState(false);
	const destinationRef = useRef<string>('');

	useEffect(() => {
		const handleOpen = (event?: {
			objectEntryFolderExternalReferenceCode?: string;
		}) => {
			destinationRef.current =
				event?.objectEntryFolderExternalReferenceCode ?? '';

			setAutoStartFlow(true);

			setOpen(true);
		};

		Liferay.on('openAIAssistantGenerateImageFlow', handleOpen);

		return () => {
			Liferay.detach('openAIAssistantGenerateImageFlow', handleOpen);
		};
	}, []);

	const saveToScope = useCallback(
		async (groupId: number, imageDataURL: string) => {
			try {
				const name = 'AI-image.png';

				const fileBase64 = imageDataURL.split(',')[1];

				if (!fileBase64) {
					throw new Error('The generated image has no data.');
				}

				const {error} = await ApiHelper.post(
					`/o/cms/basic-documents/scopes/${groupId}`,
					{
						file: {fileBase64, name},
						keywords: ['AI-generated'],
						objectEntryFolderExternalReferenceCode:
							destinationRef.current ||
							DEFAULT_FILES_FOLDER_EXTERNAL_REFERENCE_CODE,
						title: name,
					}
				);

				if (error) {
					throw new Error(error);
				}

				openToast({
					message: sub(
						Liferay.Language.get('x-was-created-successfully'),
						name
					),
					type: 'success',
				});

				Liferay.fire('fds-update-display', {id: filesDataSetId});

				// The Documents & Media thumbnail is generated asynchronously, so
				// refresh again shortly after to pick up the new preview.

				setTimeout(
					() =>
						Liferay.fire('fds-update-display', {
							id: filesDataSetId,
						}),
					2500
				);
			}
			catch (error) {
				openToast({
					message: 'Something went wrong saving the image.',
					type: 'danger',
				});
			}
		},
		[filesDataSetId]
	);

	const handleSaveImage = useCallback(
		(imageDataURL: string) => {
			if (spaces.length > 1) {
				openCMSModal({
					center: true,
					contentComponent: ({
						closeModal,
					}: {
						closeModal: () => void;
					}) =>
						CreationModalContent({
							action: 'createAsset',
							assetLibraries: spaces,
							closeModal,
							onSubmit: async ({groupId}) => {
								await saveToScope(groupId, imageDataURL);

								closeModal();
							},
							title: Liferay.Language.get('image'),
						}),
					size: 'sm',
				});

				return;
			}

			saveToScope(
				spaces.length === 1 ? spaces[0].groupId : spaceGroupId,
				imageDataURL
			);
		},
		[saveToScope, spaceGroupId, spaces]
	);

	if (!Liferay.FeatureFlags['LPD-62272']) {
		return null;
	}

	return (
		<>
			<ClayButton
				borderless
				className="text-primary"
				displayType="secondary"
				onClick={() => {
					setAutoStartFlow(false);
					setOpen(true);
				}}
			>
				<ClayIcon
					className="mr-2"
					spritemap={Liferay.Icons.spritemap}
					symbol="stars"
				/>

				{Liferay.Language.get('ai-assistant')}
			</ClayButton>

			{open && (
				<div
					className="bg-white border-left d-flex flex-column position-fixed shadow"
					style={{
						bottom: 0,
						right: 0,
						top: 56,
						width: 420,
						zIndex: 2000,
					}}
				>
					<div className="align-items-center border-bottom d-flex flex-shrink-0 justify-content-between px-3 py-2">
						<span className="font-weight-semi-bold">
							{Liferay.Language.get('ai-assistant')}
						</span>

						<ClayButton
							aria-label={Liferay.Language.get('close')}
							borderless
							displayType="unstyled"
							onClick={() => setOpen(false)}
						>
							<ClayIcon
								spritemap={Liferay.Icons.spritemap}
								symbol="times"
							/>
						</ClayButton>
					</div>

					<div className="d-flex flex-column flex-grow-1 overflow-hidden">
						<AIAssistantChat
							autoStartGenerateImageFlow={autoStartFlow}
							embedded
							getContext={() => ({})}
							instructionDefinitionScope="cms"
							onSaveImage={handleSaveImage}
						/>
					</div>
				</div>
			)}
		</>
	);
}
