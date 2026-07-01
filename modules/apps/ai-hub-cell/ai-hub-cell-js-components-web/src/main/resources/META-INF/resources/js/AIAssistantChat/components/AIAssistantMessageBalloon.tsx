/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import ClayIcon from '@clayui/icon';
import React from 'react';

import FeedbackActionsRow from '../../ReportFeedback/FeedbackActionsRow';

import '../chat.scss';
import renderAIAssistantMessageMarkdown from '../utils/renderAIAssistantMessageMarkdown';

interface AssistantMessageBalloonProps {
	error: boolean;
	feedbackGiven?: boolean;
	message: string;
	onCancelImage?: () => void;
	onRegenerateImage?: () => void;
	onReport?: () => void;
	onSaveImage?: () => void;
	onThumbsUp?: () => void;
}

const AssistantMessageBalloon: React.FC<AssistantMessageBalloonProps> = ({
	error,
	feedbackGiven,
	message,
	onCancelImage,
	onRegenerateImage,
	onReport,
	onSaveImage,
	onThumbsUp,
}) => {
	const isImage = !error && message.startsWith('data:image/');

	return (
		<div
			className={`d-flex flex-column mb-2 rounded ${error ? 'ai-assistant-chat__ai-assistant-error-message-balloon' : 'ai-assistant-chat__ai-assistant-message-balloon'}`}
		>
			<div className="d-flex flex-row font-weight-semi-bold">
				<div
					className={`align-items-start d-inline-block ml-2 mt-2 text-2 ${error ? 'text-danger' : 'text-primary'}`}
				>
					<ClayIcon
						spritemap={Liferay.Icons.spritemap}
						symbol={error ? 'exclamation-full' : 'stars'}
					/>
				</div>

				{error ? (
					<span className="m-2">
						{message ||
							Liferay.Language.get('generating-content-failed')}
					</span>
				) : isImage ? (
					<img
						alt="Generated image"
						className="ai-assistant-chat__generated-image img-fluid m-2 rounded"
						src={message}
					/>
				) : (
					<div
						className="m-2"
						dangerouslySetInnerHTML={{
							__html: renderAIAssistantMessageMarkdown(message),
						}}
					/>
				)}
			</div>

			{isImage && (onSaveImage || onRegenerateImage || onCancelImage) && (
				<div className="align-items-center d-flex justify-content-end mb-2 mr-2">
					{onCancelImage && (
						<ClayButton
							borderless
							displayType="secondary"
							onClick={onCancelImage}
							size="sm"
						>
							{Liferay.Language.get('cancel')}
						</ClayButton>
					)}

					{onRegenerateImage && (
						<ClayButtonWithIcon
							aria-label="Regenerate"
							borderless
							className="ml-1"
							displayType="secondary"
							onClick={onRegenerateImage}
							size="sm"
							spritemap={Liferay.Icons.spritemap}
							symbol="reload"
							title="Regenerate"
						/>
					)}

					{onSaveImage && (
						<ClayButton
							className="ml-1"
							displayType="primary"
							onClick={onSaveImage}
							size="sm"
						>
							{Liferay.Language.get('save')}
						</ClayButton>
					)}
				</div>
			)}

			{!isImage && onReport && !error && (
				<FeedbackActionsRow
					className="mb-1 ml-2"
					feedbackGiven={feedbackGiven}
					onReport={onReport}
					onThumbsUp={onThumbsUp}
				/>
			)}
		</div>
	);
};

export default AssistantMessageBalloon;
