/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {ClayCheckbox} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import ClayMultiSelect from '@clayui/multi-select';
import React, {useEffect, useRef, useState} from 'react';

import {ChatContext} from '../api';

import '../chat.scss';

interface Result {
	fields?: Record<string, string>;
	targetLanguageId: string;
}

interface TranslationsMessageBalloonProps {
	availableLanguageIds?: string[];
	onSubmit: (context: ChatContext) => void;
	requestedLanguageIds?: string[];
	results?: Result[];
	sourceLanguageIdRef: React.MutableRefObject<string>;
}

const AUTO_TRANSLATABLE_TYPES = [
	{isHtml: false, type: 'text'},
	{isHtml: false, type: 'long-text'},
	{isHtml: true, type: 'html'},
];

function getPageContext(sourceLanguageId: string) {
	const fields: Record<string, string> = {};
	const html: Record<string, boolean> = {};

	for (const {isHtml, type} of AUTO_TRANSLATABLE_TYPES) {
		const inputs = document.querySelectorAll<HTMLInputElement>(
			`[data-localizable="true"][data-field-type="${type}"] [type="hidden"][name$="_${sourceLanguageId}"]`
		);

		for (const input of inputs) {
			const name = input.name.replace(/_[a-z]{2}_[A-Z]{2}$/, '');

			fields[name] = input.value;
			html[name] = isHtml;
		}
	}

	return {fields, html};
}

function getTranslatedLanguageIds(languageIds: string[]): string[] {
	return languageIds.filter((languageId) =>
		AUTO_TRANSLATABLE_TYPES.some(({type}) =>
			Array.from(
				document.querySelectorAll<HTMLInputElement>(
					`[data-localizable="true"][data-field-type="${type}"] [type="hidden"][name$="_${languageId}"]`
				)
			).some((input) => Boolean(input.value))
		)
	);
}

function LanguageIdIcon({languageId}: {languageId: string}) {
	return (
		<ClayIcon
			className="mr-2"
			spritemap={Liferay.Icons.spritemap}
			symbol={languageId.replace(/_/g, '-').toLowerCase()}
		/>
	);
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

const TranslationsMessageBalloon: React.FC<TranslationsMessageBalloonProps> = ({
	availableLanguageIds,
	onSubmit,
	requestedLanguageIds,
	results,
	sourceLanguageIdRef,
}) => {
	const appliedRef = useRef<boolean>(false);

	const [selectedLanguageIds, setSelectedLanguageIds] = useState<string[]>(
		() =>
			(requestedLanguageIds ?? []).filter((languageId) =>
				(availableLanguageIds ?? []).includes(languageId)
			)
	);
	const [step, setStep] = useState<'confirm' | 'review' | 'select'>('select');
	const [submitted, setSubmitted] = useState<boolean>(false);
	const [translatedLanguageIds, setTranslatedLanguageIds] = useState<
		string[]
	>([]);
	const [value, setValue] = useState<string>('');

	const submit = (targetLanguageIds: string[]) => {
		setSubmitted(true);

		const sourceLanguageId = sourceLanguageIdRef.current;

		const {fields, html} = getPageContext(sourceLanguageId);

		onSubmit({
			fields: JSON.stringify(fields),
			html: JSON.stringify(html),
			sourceLanguageId,
			targetLanguageIds: JSON.stringify(targetLanguageIds),
		});
	};

	const toggleSelectedLanguageId = (languageId: string) => {
		setSelectedLanguageIds((previousSelectedLanguageIds) =>
			previousSelectedLanguageIds.includes(languageId)
				? previousSelectedLanguageIds.filter(
						(selectedLanguageId) =>
							selectedLanguageId !== languageId
					)
				: [...previousSelectedLanguageIds, languageId]
		);
	};

	const onTranslate = () => {
		const translatedLanguageIds =
			getTranslatedLanguageIds(selectedLanguageIds);

		if (!translatedLanguageIds.length) {
			submit(selectedLanguageIds);

			return;
		}

		setStep('confirm');
		setTranslatedLanguageIds(translatedLanguageIds);
	};

	useEffect(() => {
		if (appliedRef.current || !results?.length) {
			return;
		}

		appliedRef.current = true;

		for (const result of results) {
			const fields: Record<string, string> = {};

			for (const [name, value] of Object.entries(result.fields ?? {})) {
				fields[name] = Liferay.Util.unescapeHTML(value);
			}

			if (!Object.keys(fields).length) {
				continue;
			}

			Liferay.fire('localizationSelect:autoTranslate', {
				fields,
				languageId: result.targetLanguageId,
			});
		}
	}, [results]);

	if (results?.length) {
		return (
			<MessageBalloon>
				<MessageHeader
					message={Liferay.Language.get(
						'the-content-has-been-translated'
					)}
				/>

				<ul className="list-unstyled m-2">
					{results.map(({targetLanguageId}) => (
						<li
							className="align-items-center d-flex mb-1"
							key={targetLanguageId}
						>
							<LanguageIdIcon languageId={targetLanguageId} />

							<span className="flex-grow-1">
								{targetLanguageId}
							</span>

							<ClayLabel displayType="success">
								{Liferay.Language.get('translated')}
							</ClayLabel>
						</li>
					))}
				</ul>
			</MessageBalloon>
		);
	}

	if (step === 'confirm') {
		return (
			<MessageBalloon>
				<MessageHeader
					message={Liferay.Language.get(
						'some-of-the-selected-languages-already-have-a-translation.-what-do-you-want-to-do'
					)}
				/>

				<div className="c-gap-2 d-flex flex-row m-2">
					<ClayButton
						disabled={submitted}
						displayType="primary"
						onClick={() => submit(selectedLanguageIds)}
						size="sm"
					>
						{Liferay.Language.get('overwrite-all')}
					</ClayButton>

					<ClayButton
						disabled={submitted}
						displayType="secondary"
						onClick={() => setStep('review')}
						size="sm"
					>
						{Liferay.Language.get('review')}
					</ClayButton>
				</div>
			</MessageBalloon>
		);
	}

	if (step === 'review') {
		return (
			<MessageBalloon>
				<MessageHeader
					message={Liferay.Language.get(
						'select-the-translations-you-want-to-overwrite'
					)}
				/>

				<div className="c-gap-2 d-flex flex-column m-2">
					{translatedLanguageIds.map((languageId) => (
						<ClayCheckbox
							checked={selectedLanguageIds.includes(languageId)}
							disabled={submitted}
							key={languageId}
							label={languageId}
							onChange={() =>
								toggleSelectedLanguageId(languageId)
							}
						/>
					))}

					<ClayButton
						disabled={submitted || !selectedLanguageIds.length}
						displayType="primary"
						onClick={() => submit(selectedLanguageIds)}
						size="sm"
					>
						{Liferay.Language.get('overwrite')}
					</ClayButton>
				</div>
			</MessageBalloon>
		);
	}

	return (
		<MessageBalloon>
			<MessageHeader
				message={Liferay.Language.get(
					'which-languages-would-you-like-to-translate-into'
				)}
			/>

			<div
				className="ai-assistant-chat__language-select align-items-start c-gap-2 d-flex flex-column m-2 w-100"
				style={{maxWidth: '18rem'}}
			>
				<ClayMultiSelect
					disabled={submitted}
					items={selectedLanguageIds.map((languageId) => ({
						label: languageId,
						value: languageId,
					}))}
					onChange={setValue}
					onItemsChange={(newItems) =>
						setSelectedLanguageIds(
							newItems.map((item) => item.value)
						)
					}
					placeholder={Liferay.Language.get('select-languages')}
					sourceItems={(availableLanguageIds ?? []).map(
						(languageId) => ({
							label: languageId,
							value: languageId,
						})
					)}
					spritemap={Liferay.Icons.spritemap}
					value={value}
				>
					{(item) => (
						<ClayMultiSelect.Item
							key={item.value}
							onClick={(event) => {
								event.preventDefault();

								toggleSelectedLanguageId(item.value);

								setValue('');
							}}
							style={{cursor: 'pointer'}}
							textValue={item.label}
						>
							<LanguageIdIcon languageId={item.value} />

							{item.label}
						</ClayMultiSelect.Item>
					)}
				</ClayMultiSelect>

				<ClayButton
					disabled={!selectedLanguageIds.length || submitted}
					displayType="primary"
					onClick={onTranslate}
					size="sm"
				>
					{Liferay.Language.get('translate')}
				</ClayButton>
			</div>
		</MessageBalloon>
	);
};

export default TranslationsMessageBalloon;
