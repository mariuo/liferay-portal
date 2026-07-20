/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {NetworkStatus} from '@clayui/data-provider';
import ClayMultiSelect from '@clayui/multi-select';
import React, {useEffect, useState} from 'react';

import ApiHelper from '../../../common/services/ApiHelper';
import {Space as Project} from '../../../common/types/Space';

export type ProjectLink = {
	name: string;
	scopeKey: string;
};

type ProjectItem = {
	label: string;
	value: string;
};

type Props = {
	onUpdateProjectLinks: (projectLinks: ProjectLink[]) => void;
	projectLinks: ProjectLink[];
};

export default function ProjectsPanel({
	onUpdateProjectLinks,
	projectLinks,
}: Props) {
	const [query, setQuery] = useState('');
	const [sourceProjects, setSourceProjects] = useState<ProjectLink[]>([]);

	useEffect(() => {
		ApiHelper.getAll<Project>({
			filter: "type eq 'Project'",
			url: '/o/headless-asset-library/v1.0/asset-libraries',
		})
			.then((response) =>
				setSourceProjects(
					response.map((project) => ({
						name: project.name,
						scopeKey: project.assetLibraryKey,
					}))
				)
			)
			.catch((error) => console.error(error));
	}, []);

	const sourceItems: ProjectItem[] = sourceProjects.map((project) => ({
		label: project.name,
		value: project.scopeKey,
	}));

	const selectedItems: ProjectItem[] = projectLinks.map((projectLink) => ({
		label: projectLink.name,
		value: projectLink.scopeKey,
	}));

	return (
		<div className="p-3">
			<label htmlFor="cmpProjectsMultiSelect">
				{Liferay.Language.get('project')}
			</label>

			<ClayMultiSelect
				aria-label={Liferay.Language.get('projects')}
				id="cmpProjectsMultiSelect"
				items={selectedItems}
				key={sourceItems.length ? 'loaded' : 'empty'}
				loadingState={
					sourceItems.length ? undefined : NetworkStatus.Polling
				}
				onChange={setQuery}
				onItemsChange={(items: ProjectItem[]) =>
					onUpdateProjectLinks(
						sourceProjects.filter((project) =>
							items.some(
								(item) => item.value === project.scopeKey
							)
						)
					)
				}
				sourceItems={sourceItems}
				value={query}
			>
				{(item: ProjectItem) => (
					<ClayMultiSelect.Item
						key={item.value}
						textValue={item.label}
					>
						{item.label}
					</ClayMultiSelect.Item>
				)}
			</ClayMultiSelect>
		</div>
	);
}
