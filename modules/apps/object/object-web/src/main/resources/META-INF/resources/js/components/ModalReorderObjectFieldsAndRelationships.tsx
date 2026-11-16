/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import ClayLabel from '@clayui/label';
import ClayList from '@clayui/list';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import ClayModal, {useModal} from '@clayui/modal';
import {openToast} from 'frontend-js-components-web';
import {fetch, sub} from 'frontend-js-web';
import React, {useEffect, useRef, useState} from 'react';
import {DndProvider, useDrag, useDrop} from 'react-dnd';
import {HTML5Backend} from 'react-dnd-html5-backend';

import {getObjectFieldBusinessTypeLabel} from '../utils/getObjectFieldBusinessTypeLabel';

type ReorderItemKind = 'objectField' | 'objectRelationship';

interface DragItem {
	index: number;
	type: string;
}

interface ReorderItem {
	data: {
		actions?: unknown;
		id: number;
		priority?: number;
	};
	kind: ReorderItemKind;
	label: string;
	typeLabel: string;
}

interface ReorderListItemProps {
	index: number;
	item: ReorderItem;
	onMove: (draggedIndex: number, targetIndex: number) => void;
}

interface ModalReorderObjectFieldsAndRelationshipsProps {
	objectDefinitionExternalReferenceCode: string;
	onClose: () => void;
}

const DRAG_TYPE = 'OBJECT_FIELD_OR_OBJECT_RELATIONSHIP';

const HEADERS = {
	'Accept': 'application/json',
	'Content-Type': 'application/json',
};

function getLabel(
	labels: LocalizedValue<string> | undefined,
	fallbackLabel: string
) {
	const languageId = Liferay.ThemeDisplay.getLanguageId();

	return (
		labels?.[languageId as Liferay.Language.Locale] ??
		Object.values(labels ?? {})[0] ??
		fallbackLabel
	);
}

async function getItems(url: string) {
	const response = await fetch(`${url}?pageSize=500`, {headers: HEADERS});

	const {items} = await response.json();

	return items as any[];
}

async function getReorderItems(objectDefinitionExternalReferenceCode: string) {
	const baseURL = `/o/object-admin/v1.0/object-definitions/by-external-reference-code/${objectDefinitionExternalReferenceCode}`;

	const [objectFields, objectRelationships] = await Promise.all([
		getItems(`${baseURL}/object-fields`),
		getItems(`${baseURL}/object-relationships`),
	]);

	const reorderItems: ReorderItem[] = [
		...objectFields.map((objectField) => ({
			data: objectField,
			kind: 'objectField' as ReorderItemKind,
			label: getLabel(objectField.label, objectField.name),
			typeLabel: getObjectFieldBusinessTypeLabel(
				objectField.businessType
			),
		})),
		...objectRelationships.map((objectRelationship) => ({
			data: objectRelationship,
			kind: 'objectRelationship' as ReorderItemKind,
			label: getLabel(objectRelationship.label, objectRelationship.name),
			typeLabel: objectRelationship.objectDefinitionName2,
		})),
	];

	return reorderItems
		.map((reorderItem, index) => ({index, reorderItem}))
		.sort(
			(a, b) =>
				(a.reorderItem.data.priority ?? 0) -
					(b.reorderItem.data.priority ?? 0) || a.index - b.index
		)
		.map(({reorderItem}) => reorderItem);
}

async function savePriority(reorderItem: ReorderItem, priority: number) {
	if (reorderItem.kind === 'objectField') {
		return fetch(
			`/o/object-admin/v1.0/object-fields/${reorderItem.data.id}`,
			{
				body: JSON.stringify({priority}),
				headers: HEADERS,
				method: 'PATCH',
			}
		);
	}

	const objectRelationship = {...reorderItem.data, priority};

	delete objectRelationship.actions;

	return fetch(
		`/o/object-admin/v1.0/object-relationships/${reorderItem.data.id}`,
		{
			body: JSON.stringify(objectRelationship),
			headers: HEADERS,
			method: 'PUT',
		}
	);
}

function ReorderListItem({index, item, onMove}: ReorderListItemProps) {
	const ref = useRef<HTMLLIElement>(null);

	const [{isDragging}, dragRef] = useDrag({
		collect: (monitor) => ({
			isDragging: monitor.isDragging(),
		}),
		item: {index, type: DRAG_TYPE},
	});

	const [, dropRef] = useDrop({
		accept: DRAG_TYPE,
		hover(dragItem: DragItem, monitor) {
			if (!ref.current || dragItem.index === index) {
				return;
			}

			const targetSize = ref.current.getBoundingClientRect();

			const draggedOffset = monitor.getClientOffset();

			if (!draggedOffset) {
				return;
			}

			const draggedTop = draggedOffset.y - targetSize.top;
			const targetCenter = (targetSize.bottom - targetSize.top) / 2;

			if (
				(dragItem.index < index && draggedTop < targetCenter) ||
				(dragItem.index > index && draggedTop > targetCenter)
			) {
				return;
			}

			onMove(dragItem.index, index);

			dragItem.index = index;
		},
	});

	dragRef(dropRef(ref));

	return (
		<ClayList.Item
			flex
			ref={ref}
			style={{cursor: 'grab', opacity: isDragging ? 0.5 : 1}}
		>
			<ClayList.ItemField>
				<ClayButtonWithIcon
					aria-label={sub(Liferay.Language.get('drag-x'), item.label)}
					displayType={null}
					symbol="drag"
				/>
			</ClayList.ItemField>

			<ClayList.ItemField expand>
				<ClayList.ItemTitle>{item.label}</ClayList.ItemTitle>

				<ClayList.ItemText>{item.typeLabel}</ClayList.ItemText>
			</ClayList.ItemField>

			<ClayList.ItemField>
				<ClayLabel
					displayType={
						item.kind === 'objectField' ? 'info' : 'warning'
					}
				>
					{item.kind === 'objectField'
						? Liferay.Language.get('field')
						: Liferay.Language.get('relationship')}
				</ClayLabel>
			</ClayList.ItemField>
		</ClayList.Item>
	);
}

export default function ModalReorderObjectFieldsAndRelationships({
	objectDefinitionExternalReferenceCode,
	onClose,
}: ModalReorderObjectFieldsAndRelationshipsProps) {
	const [items, setItems] = useState<ReorderItem[] | null>(null);
	const [saving, setSaving] = useState(false);

	const {observer, onClose: handleClose} = useModal({onClose});

	useEffect(() => {
		getReorderItems(objectDefinitionExternalReferenceCode)
			.then(setItems)
			.catch(() => {
				openToast({
					message: Liferay.Language.get(
						'an-unexpected-error-occurred'
					),
					type: 'danger',
				});

				setItems([]);
			});
	}, [objectDefinitionExternalReferenceCode]);

	const handleMove = (draggedIndex: number, targetIndex: number) => {
		setItems((items) => {
			if (!items) {
				return items;
			}

			const nextItems = [...items];

			const [draggedItem] = nextItems.splice(draggedIndex, 1);

			nextItems.splice(targetIndex, 0, draggedItem);

			return nextItems;
		});
	};

	const handleSave = async () => {
		if (!items) {
			return;
		}

		setSaving(true);

		try {
			for (const [index, item] of items.entries()) {
				const priority = index + 1;

				if (item.data.priority === priority) {
					continue;
				}

				const response = await savePriority(item, priority);

				if (!response.ok) {
					throw new Error(item.label);
				}
			}

			window.location.reload();
		}
		catch (error) {
			openToast({
				message: Liferay.Language.get('an-unexpected-error-occurred'),
				type: 'danger',
			});

			setSaving(false);
		}
	};

	return (
		<ClayModal observer={observer}>
			<ClayModal.Header>
				{Liferay.Language.get('reorder')}
			</ClayModal.Header>

			<ClayModal.Body scrollable>
				{items ? (

					// @ts-ignore

					<DndProvider backend={HTML5Backend} context={window}>
						<ClayList>
							{items.map((item, index) => (
								<ReorderListItem
									index={index}
									item={item}
									key={`${item.kind}-${item.data.id}`}
									onMove={handleMove}
								/>
							))}
						</ClayList>
					</DndProvider>
				) : (
					<ClayLoadingIndicator />
				)}
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group key={1} spaced>
						<ClayButton
							displayType="secondary"
							onClick={handleClose}
						>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton
							disabled={saving || !items}
							onClick={handleSave}
						>
							{Liferay.Language.get('save')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</ClayModal>
	);
}
