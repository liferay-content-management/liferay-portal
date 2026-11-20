/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLink from '@clayui/link';
import {
	IInternalRenderer,
	findAction,
	replaceTokens,
} from '@liferay/frontend-data-set-web';
import {sub} from 'frontend-js-web';
import React, {useMemo} from 'react';

import {onWorkflowTaskActionClick} from '../../../tasks/onWorkflowTaskActionClick';
import {MILLISECONDS_PER_DAY} from '../GovernanceService';
import {GovernanceAdditionalProps} from '../types';
import {
	GovernanceListPanel,
	getGovernanceAssetListViews,
	useReloadStatisticsOnListRefresh,
} from './GovernanceAssetList';

export type WorkflowTask = {
	embedded?: {
		assigneePerson?: {name: string};
		assigneeRoles?: Array<{name: string}>;
		dateDue?: string;
		label?: string;
		objectReviewed?: {assetTitle?: string};
		slaResults?: Array<{breached?: boolean; dateOverdue?: string}>;
	};
};

export function formatDaysSince(
	date: string,
	singularTemplate: string,
	pluralTemplate: string
) {
	const days = Math.floor(
		(Date.now() - new Date(date).getTime()) / MILLISECONDS_PER_DAY
	);

	return sub(days === 1 ? singularTemplate : pluralTemplate, days);
}

function getWorkflowTaskSubtitle(task: WorkflowTask, status: string) {
	const {assigneePerson, assigneeRoles = [], label} = task.embedded ?? {};

	return [
		label,
		assigneePerson?.name ??
			assigneeRoles.map((assigneeRole) => assigneeRole.name).join(', '),
		status,
	]
		.filter(Boolean)
		.join(' · ');
}

function getWorkflowTaskRenderer(getStatus: (task: WorkflowTask) => string) {
	return function WorkflowTaskRenderer({
		actions,
		itemData,
	}: {
		actions: Array<{data: {id: string}; href?: string}>;
		itemData: WorkflowTask;
	}) {
		const assetTitle = itemData.embedded?.objectReviewed?.assetTitle;
		const href = findAction(actions, 'actionLinkWorkflowTask')?.href;

		return (
			<div>
				<div className="table-list-title">
					{href ? (
						<ClayLink
							className="text-decoration-underline"
							data-senna-off
							href={replaceTokens(href, itemData)}
						>
							{assetTitle}
						</ClayLink>
					) : (
						assetTitle
					)}
				</div>

				<div className="text-3 text-secondary">
					{getWorkflowTaskSubtitle(itemData, getStatus(itemData))}
				</div>
			</div>
		);
	};
}

export function WorkflowTasksList({
	additionalProps,
	apiURL,
	count,
	description,
	emptyStateDescription,
	emptyStateTitle,
	getStatus,
	id,
	title,
	viewAllHref,
}: {
	additionalProps: GovernanceAdditionalProps;
	apiURL: string | null;
	count: number;
	description: string;
	emptyStateDescription: string;
	emptyStateTitle: string;
	getStatus: (task: WorkflowTask) => string;
	id: string;
	title: string;
	viewAllHref: string;
}) {
	const reloadStatistics = useReloadStatisticsOnListRefresh(id);

	const {workflowTaskFDSActionDropdownItems} = additionalProps;

	const fdsProps = useMemo(() => {
		const rendererName = `${id}Renderer`;

		return {
			...(apiURL ? {apiURL} : {items: []}),
			customRenderers: {
				tableCell: [
					{
						component: getWorkflowTaskRenderer(getStatus),
						name: rendererName,
						type: 'internal',
					} as IInternalRenderer,
				],
			},
			itemsActions: workflowTaskFDSActionDropdownItems,
			onActionDropdownItemClick: ({
				loadData,
				...otherProps
			}: Parameters<typeof onWorkflowTaskActionClick>[0]) =>
				onWorkflowTaskActionClick({
					...otherProps,
					loadData: () => loadData().then(reloadStatistics),
				}),
			views: getGovernanceAssetListViews({
				actionId: 'actionLinkWorkflowTask',
				contentRenderer: rendererName,
				fieldName: 'embedded.objectReviewed.assetTitle',
			}),
		};
	}, [
		apiURL,
		getStatus,
		id,
		reloadStatistics,
		workflowTaskFDSActionDropdownItems,
	]);

	return (
		<GovernanceListPanel
			count={count}
			description={description}
			emptyStateDescription={emptyStateDescription}
			emptyStateTitle={emptyStateTitle}
			fdsProps={fdsProps}
			id={id}
			title={title}
			viewAllHref={viewAllHref}
		/>
	);
}
