/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ObjectRelationship} from '../../common/types/ObjectDefinition';
import {RelatedContent, RepeatableGroup, Structure} from '../types/Structure';
import getPriorities from './getPriorities';
import isRepeatableGroup from './isRepeatableGroup';

export default function buildObjectRelationships({
	children,
	structureERC,
}: {
	children: Structure['children'];
	structureERC: Structure['erc'];
}): ObjectRelationship[] {
	return getRelatedContents(children, structureERC)
		.filter(({relatedContent}) => !relatedContent.multiselection)
		.map(({parentERC, priority, relatedContent}) => {
			return {
				deletionType: 'disassociate',
				externalReferenceCode: relatedContent.erc,
				label: relatedContent.label,
				name: relatedContent.name,
				objectDefinitionExternalReferenceCode1:
					relatedContent.relatedStructureERC!,
				objectDefinitionExternalReferenceCode2: parentERC,
				type: 'oneToMany',
				...(priority === undefined ? {} : {objectField: {priority}}),
			};
		});
}

function getRelatedContents(
	children: Structure['children'] | RepeatableGroup['children'],
	parentERC: Structure['erc'] | RepeatableGroup['erc']
): {
	parentERC: Structure['erc'] | RepeatableGroup['erc'];
	priority?: number;
	relatedContent: RelatedContent;
}[] {
	const priorities = getPriorities(children);

	const relatedContents = [];

	for (const child of children.values()) {
		if (isRepeatableGroup(child)) {
			relatedContents.push(
				...getRelatedContents(child.children, child.erc)
			);
		}
		else if (child.type === 'related-content') {
			relatedContents.push({
				parentERC,
				priority: priorities?.get(child.uuid),
				relatedContent: child,
			});
		}
	}

	return relatedContents;
}
