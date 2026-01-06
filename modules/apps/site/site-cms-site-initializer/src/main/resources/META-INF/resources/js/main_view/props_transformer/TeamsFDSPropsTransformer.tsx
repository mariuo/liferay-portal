/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ACTIONS from './actions/creationMenuActions';
import deleteItemAction from './actions/deleteItemAction';
import addOnClickToCreationMenuItems from './utils/addOnClickToCreationMenuItems';

export default function TeamsFDSPropsTransformer({
	creationMenu,
	...otherProps
}: {
	creationMenu: any;
}) {
	return {
		...otherProps,
		creationMenu: {
			...creationMenu,
			primaryItems: addOnClickToCreationMenuItems(
				creationMenu.primaryItems,
				ACTIONS
			),
		},
		async onActionDropdownItemClick({
			action,
			itemData,
			loadData,
		}: {
			action: any;
			itemData: ItemData;
			loadData: () => {};
		}) {
			if (action?.data?.id === 'delete') {
				await deleteItemAction(itemData, loadData);
			}
		},
	};
}