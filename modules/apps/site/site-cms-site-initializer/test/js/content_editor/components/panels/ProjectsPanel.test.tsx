/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import React from 'react';

import ApiHelper from '../../../../../src/main/resources/META-INF/resources/js/common/services/ApiHelper';
import ProjectsPanel from '../../../../../src/main/resources/META-INF/resources/js/content_editor/components/panels/ProjectsPanel';

jest.mock(
	'../../../../../src/main/resources/META-INF/resources/js/common/services/ApiHelper'
);

const mockOpenToast = jest.fn();

jest.mock('frontend-js-components-web', () => ({
	openToast: (...args: unknown[]) => mockOpenToast(...args),
}));

jest.mock('@clayui/multi-select', () => {
	const MockMultiSelect = ({items, onItemsChange, sourceItems}: any) => (
		<div>
			<div data-testid="selected-items">
				{items.map((item: any) => (
					<button
						data-testid={`remove-${item.value}`}
						key={item.value}
						onClick={() =>
							onItemsChange(
								items.filter(
									(selectedItem: any) =>
										selectedItem.value !== item.value
								)
							)
						}
						type="button"
					>
						{item.label}
					</button>
				))}
			</div>

			<div data-testid="source-items">
				{sourceItems.map((item: any) => (
					<button
						data-testid={`add-${item.value}`}
						key={item.value}
						onClick={() => onItemsChange([...items, item])}
						type="button"
					>
						{item.label}
					</button>
				))}
			</div>
		</div>
	);

	MockMultiSelect.Item = ({children}: any) => <div>{children}</div>;

	return {
		__esModule: true,
		default: MockMultiSelect,
	};
});

const PROJECT_OBJECT_DEFINITION_ID = 101;
const PROJECT_ASSET_RELATIONSHIP_OBJECT_DEFINITION_ID = 202;

const ENTRY_CLASS_NAME = 'com.liferay.object.model.ObjectDefinition#ABCD';
const ENTRY_EXTERNAL_REFERENCE_CODE = 'ASSET-ERC';
const ENTRY_SCOPE_KEY = 'asset-scope';

const projectSearchItems = [
	{
		embedded: {
			externalReferenceCode: 'PROJECT-1-ERC',
			id: 1,
			scopeKey: 'project-1-scope',
			title: 'Project 1',
		},
	},
	{
		embedded: {
			externalReferenceCode: 'PROJECT-2-ERC',
			id: 2,
			scopeKey: 'project-2-scope',
			title: 'Project 2',
		},
	},
];

function mockSearch({linkItems = []}: {linkItems?: any[]} = {}) {
	(ApiHelper.get as jest.Mock).mockImplementation((url: string) => {
		if (
			url.includes(
				String(PROJECT_ASSET_RELATIONSHIP_OBJECT_DEFINITION_ID)
			)
		) {
			return Promise.resolve({
				data: {items: linkItems, lastPage: 1},
				error: null,
			});
		}

		if (url.includes(String(PROJECT_OBJECT_DEFINITION_ID))) {
			return Promise.resolve({
				data: {items: projectSearchItems, lastPage: 1},
				error: null,
			});
		}

		return Promise.resolve({data: {items: [], lastPage: 1}, error: null});
	});
}

function renderComponent(props = {}) {
	return render(
		<ProjectsPanel
			cmpProjectAssetRelationshipObjectDefinitionId={
				PROJECT_ASSET_RELATIONSHIP_OBJECT_DEFINITION_ID
			}
			cmpProjectObjectDefinitionId={PROJECT_OBJECT_DEFINITION_ID}
			entryClassName={ENTRY_CLASS_NAME}
			entryExternalReferenceCode={ENTRY_EXTERNAL_REFERENCE_CODE}
			entryScopeKey={ENTRY_SCOPE_KEY}
			{...props}
		/>
	);
}

describe('ProjectsPanel', () => {
	beforeEach(() => {
		(global as any).Liferay = {
			Language: {
				get: jest.fn((key: string) => key),
			},
		};
	});

	afterEach(() => {
		jest.resetAllMocks();
	});

	it('renders every project as a selectable option', async () => {
		mockSearch();

		renderComponent();

		await waitFor(() =>
			expect(screen.getByTestId('add-1')).toBeInTheDocument()
		);

		expect(screen.getByTestId('add-2')).toBeInTheDocument();
		expect(screen.getByText('Project 1')).toBeInTheDocument();
		expect(screen.getByText('Project 2')).toBeInTheDocument();
	});

	it('renders nothing when the project object definition is absent', () => {
		mockSearch();

		renderComponent({cmpProjectObjectDefinitionId: null});

		expect(screen.queryByTestId('add-1')).not.toBeInTheDocument();
		expect(ApiHelper.get).not.toHaveBeenCalled();
	});

	it('preselects the projects the asset is already linked to', async () => {
		mockSearch({
			linkItems: [
				{
					embedded: {
						classExternalReferenceCode:
							ENTRY_EXTERNAL_REFERENCE_CODE,
						className: ENTRY_CLASS_NAME,
						id: 500,
						r_cmpProjectToCMPProjectAssetRelationships_c_cmpProjectId: 1,
						scopeKey: ENTRY_SCOPE_KEY,
					},
				},
			],
		});

		renderComponent();

		await waitFor(() =>
			expect(screen.getByTestId('remove-1')).toBeInTheDocument()
		);

		expect(screen.queryByTestId('remove-2')).not.toBeInTheDocument();
	});

	it('ignores links that belong to a different asset', async () => {
		mockSearch({
			linkItems: [
				{
					embedded: {
						classExternalReferenceCode: 'OTHER-ASSET-ERC',
						className: ENTRY_CLASS_NAME,
						id: 500,
						r_cmpProjectToCMPProjectAssetRelationships_c_cmpProjectId: 1,
						scopeKey: ENTRY_SCOPE_KEY,
					},
				},
			],
		});

		renderComponent();

		await waitFor(() => expect(ApiHelper.get).toHaveBeenCalledTimes(2));

		await waitFor(() =>
			expect(screen.getByTestId('add-1')).toBeInTheDocument()
		);

		expect(screen.queryByTestId('remove-1')).not.toBeInTheDocument();
	});

	it('pages through every result when the search is truncated', async () => {
		(ApiHelper.get as jest.Mock).mockImplementation((url: string) => {
			if (url.includes(String(PROJECT_OBJECT_DEFINITION_ID))) {
				return Promise.resolve({
					data: {items: projectSearchItems, lastPage: 1},
					error: null,
				});
			}

			// The link that matches the asset is only on the second page.

			if (url.includes('page=1')) {
				return Promise.resolve({
					data: {items: [], lastPage: 2},
					error: null,
				});
			}

			return Promise.resolve({
				data: {
					items: [
						{
							embedded: {
								classExternalReferenceCode:
									ENTRY_EXTERNAL_REFERENCE_CODE,
								className: ENTRY_CLASS_NAME,
								id: 500,
								r_cmpProjectToCMPProjectAssetRelationships_c_cmpProjectId: 1,
								scopeKey: ENTRY_SCOPE_KEY,
							},
						},
					],
					lastPage: 2,
				},
				error: null,
			});
		});

		renderComponent();

		await waitFor(() =>
			expect(screen.getByTestId('remove-1')).toBeInTheDocument()
		);
	});

	it('creates a link when a project is selected', async () => {
		mockSearch();

		(ApiHelper.post as jest.Mock).mockResolvedValue({
			data: {externalReferenceCode: 'LINK-2-ERC', id: 600},
			error: null,
		});

		renderComponent();

		await waitFor(() =>
			expect(screen.getByTestId('add-2')).toBeInTheDocument()
		);

		fireEvent.click(screen.getByTestId('add-2'));

		await waitFor(() =>
			expect(ApiHelper.post).toHaveBeenCalledWith(
				'/o/cmp/project-asset-relationships/scopes/project-2-scope',
				{
					classExternalReferenceCode: ENTRY_EXTERNAL_REFERENCE_CODE,
					className: ENTRY_CLASS_NAME,
					r_cmpProjectToCMPProjectAssetRelationships_c_cmpProjectId: 2,
					scopeKey: ENTRY_SCOPE_KEY,
				}
			)
		);

		await waitFor(() =>
			expect(mockOpenToast).toHaveBeenCalledWith(
				expect.objectContaining({type: 'success'})
			)
		);
	});

	it('deletes the link when a project is deselected', async () => {
		mockSearch({
			linkItems: [
				{
					embedded: {
						classExternalReferenceCode:
							ENTRY_EXTERNAL_REFERENCE_CODE,
						className: ENTRY_CLASS_NAME,
						id: 500,
						r_cmpProjectToCMPProjectAssetRelationships_c_cmpProjectId: 1,
						scopeKey: ENTRY_SCOPE_KEY,
					},
				},
			],
		});

		(ApiHelper.delete as jest.Mock).mockResolvedValue({
			data: {},
			error: null,
		});

		renderComponent();

		await waitFor(() =>
			expect(screen.getByTestId('remove-1')).toBeInTheDocument()
		);

		fireEvent.click(screen.getByTestId('remove-1'));

		await waitFor(() =>
			expect(ApiHelper.delete).toHaveBeenCalledWith(
				'/o/cmp/project-asset-relationships/500'
			)
		);
	});
});
