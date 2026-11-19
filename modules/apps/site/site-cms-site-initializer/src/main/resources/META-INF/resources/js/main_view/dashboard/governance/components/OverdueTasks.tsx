/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React, {useContext, useMemo} from 'react';

import {TASK_QUICK_FILTER_UPDATES} from '../../../tasks/taskQuickFilterUpdates';
import InteractiveCard from '../../performance/components/InteractiveCard';
import {GovernanceContext} from '../GovernanceContext';
import GovernanceService from '../GovernanceService';
import getSectionHref, {
	getSpaceFilters,
	toFDSConfigFilters,
} from '../getSectionHref';
import {GovernanceAdditionalProps} from '../types';
import {getGovernanceAssetListPanelId} from './GovernanceAssetList';
import {PercentageOfTotal} from './PercentageOfTotal';
import {
	WorkflowTask,
	WorkflowTasksList,
	formatDaysSince,
} from './WorkflowTasksList';

const LIST_ID = 'overdueTasks';

function getDescription() {
	return Liferay.Language.get(
		'these-are-workflow-tasks-that-have-passed-their-due-date'
	);
}

function getOverdueStatus(task: WorkflowTask) {
	return formatDaysSince(
		task.embedded?.dateDue ?? '',
		Liferay.Language.get('x-day-overdue'),
		Liferay.Language.get('x-days-overdue')
	);
}

export function OverdueTasksCard({
	expanded,
	onClick,
}: {
	expanded: boolean;
	onClick: () => void;
}) {
	const {loadingStatistics, statistics} = useContext(GovernanceContext);

	const count = statistics?.overdueWorkflowTasksCount ?? 0;

	return (
		<InteractiveCard
			active={expanded}
			aria-controls={getGovernanceAssetListPanelId(LIST_ID)}
			aria-expanded={expanded}
			color="orange"
			description={getDescription()}
			icon="time"
			loading={loadingStatistics}
			onClick={onClick}
			title={Liferay.Language.get('overdue-tasks')}
			value={count}
		>
			<PercentageOfTotal
				count={count}
				template={Liferay.Language.get('x-of-x-workflow-tasks')}
				total={statistics?.workflowTasksCount ?? 0}
			/>
		</InteractiveCard>
	);
}

export function OverdueTasksList({
	additionalProps,
}: {
	additionalProps: GovernanceAdditionalProps;
}) {
	const {selectedSpaceGroupIds, space, statistics} =
		useContext(GovernanceContext);

	const apiURL = useMemo(
		() =>
			selectedSpaceGroupIds.length
				? GovernanceService.getOverdueTasksURL(selectedSpaceGroupIds)
				: null,
		[selectedSpaceGroupIds]
	);

	return (
		<WorkflowTasksList
			additionalProps={additionalProps}
			apiURL={apiURL}
			count={statistics?.overdueWorkflowTasksCount ?? 0}
			description={getDescription()}
			emptyStateDescription={Liferay.Language.get(
				'all-workflow-tasks-are-within-their-due-date'
			)}
			emptyStateTitle={Liferay.Language.get('no-overdue-tasks')}
			getStatus={getOverdueStatus}
			id={LIST_ID}
			title={Liferay.Language.get('overdue-tasks')}
			viewAllHref={getSectionHref(
				'tasks',
				additionalProps.tasksSectionFDSName,
				{
					filters: [
						...toFDSConfigFilters(
							TASK_QUICK_FILTER_UPDATES.overdue()
						),
						...getSpaceFilters(space),
					],
				}
			)}
		/>
	);
}
