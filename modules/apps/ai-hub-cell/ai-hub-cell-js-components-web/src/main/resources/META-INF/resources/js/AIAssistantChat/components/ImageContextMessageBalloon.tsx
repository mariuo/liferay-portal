/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {ClaySelectWithOption} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import React, {useState} from 'react';

import {ChatContext} from '../api';

import '../chat.scss';

interface ImageContextMessageBalloonProps {
	availableStyles?: string[];
	onSubmit: (context: ChatContext) => void;
}

function MessageBalloon({children}: {children: React.ReactNode}) {
	return (
		<div className="ai-assistant-chat__ai-assistant-message-balloon d-flex flex-column mb-2 rounded">
			{children}
		</div>
	);
}

function MessageHeader({message}: {message: string}) {
	return (
		<div className="d-flex flex-row font-weight-semi-bold">
			<div className="align-items-start d-inline-block ml-2 mt-2 text-2 text-primary">
				<ClayIcon spritemap={Liferay.Icons.spritemap} symbol="stars" />
			</div>

			<div className="m-2">{message}</div>
		</div>
	);
}

const COUNT_OPTIONS = [1, 2, 3, 4];

const ImageContextMessageBalloon: React.FC<ImageContextMessageBalloonProps> = ({
	availableStyles,
	onSubmit,
}) => {
	const styles = availableStyles?.length
		? availableStyles
		: ['Photorealistic', 'Illustration', 'Digital Art', 'Watercolor'];

	const [count, setCount] = useState<number>(1);
	const [style, setStyle] = useState<string>(styles[0]);
	const [submitted, setSubmitted] = useState<boolean>(false);

	const submit = () => {
		setSubmitted(true);

		onSubmit({
			count: String(count),
			style,
		});
	};

	return (
		<MessageBalloon>
			<MessageHeader
				message={Liferay.Language.get(
					'how-many-images-and-in-which-style'
				)}
			/>

			<div
				className="align-items-start c-gap-2 d-flex flex-column m-2 w-100"
				style={{maxWidth: '18rem'}}
			>
				<ClaySelectWithOption
					aria-label={Liferay.Language.get('number-of-images')}
					disabled={submitted}
					onChange={(event) => setCount(Number(event.target.value))}
					options={COUNT_OPTIONS.map((value) => ({
						label: String(value),
						value: String(value),
					}))}
					value={String(count)}
				/>

				<ClaySelectWithOption
					aria-label={Liferay.Language.get('style')}
					disabled={submitted}
					onChange={(event) => setStyle(event.target.value)}
					options={styles.map((value) => ({
						label: value,
						value,
					}))}
					value={style}
				/>

				<ClayButton
					disabled={submitted}
					displayType="primary"
					onClick={submit}
					size="sm"
				>
					{Liferay.Language.get('generate')}
				</ClayButton>
			</div>
		</MessageBalloon>
	);
};

export default ImageContextMessageBalloon;
