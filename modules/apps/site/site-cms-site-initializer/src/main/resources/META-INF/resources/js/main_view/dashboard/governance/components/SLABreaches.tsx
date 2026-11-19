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

const LIST_ID = 'slaBreaches';

function getDescription() {
	return Liferay.Language.get(
		'these-are-workflow-tasks-that-exceeded-the-time-allowed-by-their-service-level-agreement'
	);
}

function getSLABreachStatus(task: WorkflowTask) {
	const slaOverdueDate = task.embedded?.slaOverdueDate;

	if (!slaOverdueDate) {
		return '';
	}

	return formatDaysSince(
		slaOverdueDate,
		Liferay.Language.get('x-day-past-sla'),
		Liferay.Language.get('x-days-past-sla')
	);
}

export function SLABreachesCard({
	expanded,
	onClick,
}: {
	expanded: boolean;
	onClick: () => void;
}) {
	const {loadingStatistics, statistics} = useContext(GovernanceContext);

	const count = statistics?.slaBreachedWorkflowTasksCount ?? 0;

	return (
		<InteractiveCard
			active={expanded}
			aria-controls={getGovernanceAssetListPanelId(LIST_ID)}
			aria-expanded={expanded}
			color="red"
			description={getDescription()}
			icon="exclamation-circle"
			loading={loadingStatistics}
			onClick={onClick}
			title={Liferay.Language.get('sla-breaches')}
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

export function SLABreachesList({
	additionalProps,
}: {
	additionalProps: GovernanceAdditionalProps;
}) {
	const {selectedSpaceGroupIds, space, statistics} =
		useContext(GovernanceContext);

	const apiURL = useMemo(
		() =>
			selectedSpaceGroupIds.length
				? GovernanceService.getSLABreachedTasksURL(
						selectedSpaceGroupIds
					)
				: null,
		[selectedSpaceGroupIds]
	);

	return (
		<WorkflowTasksList
			additionalProps={additionalProps}
			apiURL={apiURL}
			count={statistics?.slaBreachedWorkflowTasksCount ?? 0}
			description={getDescription()}
			emptyStateDescription={Liferay.Language.get(
				'all-workflow-tasks-are-within-their-sla'
			)}
			emptyStateTitle={Liferay.Language.get('no-sla-breaches')}
			getStatus={getSLABreachStatus}
			id={LIST_ID}
			title={Liferay.Language.get('sla-breaches')}
			viewAllHref={getSectionHref(
				'tasks',
				additionalProps.tasksSectionFDSName,
				{
					filters: [
						...toFDSConfigFilters(
							TASK_QUICK_FILTER_UPDATES.pending()
						),
						...getSpaceFilters(space),
					],
				}
			)}
		/>
	);
}
