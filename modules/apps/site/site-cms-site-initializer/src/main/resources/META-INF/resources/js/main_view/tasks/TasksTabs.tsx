/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayNavigationBar from '@clayui/navigation-bar';
import {useSessionState} from 'frontend-js-components-web';
import React, {useEffect} from 'react';

const TABS = [
	{label: Liferay.Language.get('all-tasks'), panelId: 'cmsAllTasksPanel'},
	{
		label: Liferay.Language.get('project-tasks'),
		panelId: 'cmsProjectTasksPanel',
	},
	{
		label: Liferay.Language.get('workflow-tasks'),
		panelId: 'cmsWorkflowTasksPanel',
	},
];

export default function TasksTabs() {
	const [selectedPanelId, setSelectedPanelId] = useSessionState<string>(
		'cmsTasksSelectedTab',
		TABS[0].panelId
	);

	const selectedTab =
		TABS.find(({panelId}) => panelId === selectedPanelId) ?? TABS[0];

	useEffect(() => {
		for (const {panelId} of TABS) {
			document
				.getElementById(panelId)
				?.toggleAttribute('hidden', panelId !== selectedTab.panelId);
		}
	}, [selectedTab]);

	const onSelectTab = (panelId: string) => {
		setSelectedPanelId(panelId);

		Liferay.fire('tabsFragment:activePanel', {
			panel: document.getElementById(panelId),
		});
	};

	return (
		<ClayNavigationBar
			className="cms-fds-fluid"
			fluidSize={false}
			triggerLabel={selectedTab.label}
		>
			{TABS.map(({label, panelId}) => (
				<ClayNavigationBar.Item
					active={panelId === selectedTab.panelId}
					key={panelId}
				>
					<ClayButton onClick={() => onSelectTab(panelId)}>
						{label}
					</ClayButton>
				</ClayNavigationBar.Item>
			))}
		</ClayNavigationBar>
	);
}
