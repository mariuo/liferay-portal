/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import ClayCard from '@clayui/card';
import {ClayCheckbox} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import React, {useState} from 'react';

import {saveGeneratedImages} from '../services/saveGeneratedImages';

import '../chat.scss';
import renderAIAssistantMessageMarkdown from '../utils/renderAIAssistantMessageMarkdown';

interface ImageMessageBalloonProps {
	folderId?: number | string;
	images: string[];
	message?: string;
	onRegenerate?: () => void;
	siteId?: number | string;
}

const ImageMessageBalloon: React.FC<ImageMessageBalloonProps> = ({
	folderId,
	images,
	message,
	onRegenerate,
	siteId,
}) => {
	const multiple = images.length > 1;

	const [selectedIndexes, setSelectedIndexes] = useState<Set<number>>(
		() => new Set(images.map((_, index) => index))
	);
	const [saving, setSaving] = useState<boolean>(false);

	function toggleSelected(index: number) {
		setSelectedIndexes((previousSelectedIndexes) => {
			const nextSelectedIndexes = new Set(previousSelectedIndexes);

			if (nextSelectedIndexes.has(index)) {
				nextSelectedIndexes.delete(index);
			}
			else {
				nextSelectedIndexes.add(index);
			}

			return nextSelectedIndexes;
		});
	}

	const selectedImages = images.filter((_, index) =>
		selectedIndexes.has(index)
	);

	async function handleSave() {
		if (!selectedImages.length) {
			return;
		}

		setSaving(true);

		try {
			await saveGeneratedImages(selectedImages, {folderId, siteId});
		}
		finally {
			setSaving(false);
		}
	}

	return (
		<div className="ai-assistant-chat__ai-assistant-message-balloon d-flex flex-column mb-2 p-2 rounded">
			{message && (
				<div className="d-flex flex-row font-weight-semi-bold">
					<div className="align-items-start d-inline-block ml-2 mt-2 text-2 text-primary">
						<ClayIcon
							spritemap={Liferay.Icons.spritemap}
							symbol="stars"
						/>
					</div>

					<div
						className="m-2"
						dangerouslySetInnerHTML={{
							__html: renderAIAssistantMessageMarkdown(message),
						}}
					/>
				</div>
			)}

			<ul className="card-page card-page-equal-height px-2">
				{images.map((image, index) => (
					<li
						className="card-page-item card-page-item-asset"
						key={index}
					>
						<ClayCard displayType="image" selectable={multiple}>
							{multiple ? (
								<ClayCheckbox
									checked={selectedIndexes.has(index)}
									disabled={saving}
									onChange={() => toggleSelected(index)}
								>
									<ClayCard.AspectRatio className="card-item-first card-item-last">
										<img
											alt={Liferay.Language.get(
												'generated-image'
											)}
											className="aspect-ratio-item-center-middle aspect-ratio-item-fluid"
											src={image}
										/>
									</ClayCard.AspectRatio>
								</ClayCheckbox>
							) : (
								<ClayCard.AspectRatio className="card-item-first card-item-last">
									<img
										alt={Liferay.Language.get(
											'generated-image'
										)}
										className="aspect-ratio-item-center-middle aspect-ratio-item-fluid"
										src={image}
									/>
								</ClayCard.AspectRatio>
							)}
						</ClayCard>
					</li>
				))}
			</ul>

			<div className="align-items-center d-flex justify-content-end mt-2">
				{onRegenerate && (
					<ClayButtonWithIcon
						aria-label={Liferay.Language.get('regenerate')}
						className="mr-2"
						disabled={saving}
						displayType="secondary"
						onClick={onRegenerate}
						outline
						spritemap={Liferay.Icons.spritemap}
						symbol="reload"
						title={Liferay.Language.get('regenerate')}
					/>
				)}

				<ClayButton
					disabled={saving || !selectedImages.length}
					displayType="primary"
					onClick={handleSave}
				>
					{saving ? (
						<>
							<ClayLoadingIndicator
								className="d-inline-block mr-2"
								size="sm"
							/>

							{Liferay.Language.get('saving')}
						</>
					) : selectedImages.length > 1 ? (
						Liferay.Language.get('save-images')
					) : (
						Liferay.Language.get('save-image')
					)}
				</ClayButton>
			</div>
		</div>
	);
};

export default ImageMessageBalloon;
