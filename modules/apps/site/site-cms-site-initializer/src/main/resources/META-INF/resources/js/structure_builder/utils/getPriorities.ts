/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	NonRepeatableGroup,
	Structure,
	StructureChild,
} from '../types/Structure';
import {Uuid} from '../types/Uuid';

export default function getPriorities(
	children: Structure['children']
): Map<Uuid, number> | null {
	if (!isOrdered(children)) {
		return null;
	}

	const priorities = new Map<Uuid, number>();

	const addPriorities = (children: Structure['children']) => {
		for (const child of children.values()) {
			if (isPlainGroup(child)) {
				addPriorities(child.children);
			}
			else {
				priorities.set(child.uuid, priorities.size + 1);
			}
		}
	};

	addPriorities(children);

	return priorities;
}

function isOrdered(children: Structure['children']): boolean {
	return Array.from(children.values()).some(
		(child) =>
			(child.priority ?? 0) > 0 ||
			(isPlainGroup(child) && isOrdered(child.children))
	);
}

function isPlainGroup(child: StructureChild): child is NonRepeatableGroup {
	return child.type === 'group' && !child.isRepeatable;
}
