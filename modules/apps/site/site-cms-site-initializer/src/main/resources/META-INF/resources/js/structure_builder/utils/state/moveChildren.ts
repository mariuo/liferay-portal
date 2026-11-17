/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Group, Structure, StructureChild} from '../../types/Structure';
import {Uuid} from '../../types/Uuid';
import sortChildren from './sortChildren';

export default function moveChildren({
	index,
	items,
	root,
	targetUuid,
}: {
	index?: number;
	items: StructureChild[];
	root: Structure | Group;
	targetUuid: Uuid;
}): Structure['children'] | Group['children'] {
	const children = new Map();

	// Iterate over children

	for (const rootChild of root.children.values()) {

		// Skip if this child is one of the items to move

		if (items.some(({uuid}) => uuid === rootChild.uuid)) {
			continue;
		}

		// If it's a repeatable group, build it with recursive call

		if (rootChild.type === 'group') {
			const group: Group = {
				...rootChild,
				children: moveChildren({
					index,
					items,
					root: rootChild,
					targetUuid,
				}),
			};

			children.set(group.uuid, group);
		}
		else {
			children.set(rootChild.uuid, rootChild);
		}
	}

	if (root.uuid !== targetUuid) {
		return sortChildren(children);
	}

	// Insert items at the end when no position was given

	if (index === undefined) {
		for (const item of items) {
			children.set(item.uuid, {...item, priority: undefined});
		}

		return sortChildren(children);
	}

	// Insert items at the given position and renumber the siblings

	const position = Array.from(root.children.values())
		.slice(0, index)
		.filter(
			(rootChild) => !items.some(({uuid}) => uuid === rootChild.uuid)
		).length;

	const orderedChildren = Array.from(children.values());

	orderedChildren.splice(position, 0, ...items);

	return new Map(
		orderedChildren.map((child, childIndex) => [
			child.uuid,
			{...child, priority: childIndex + 1},
		])
	);
}
