/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import '@testing-library/jest-dom';

// eslint-disable-next-line @liferay/portal/no-cross-module-deep-import
import {checkAccessibility} from '@liferay/layout-js-components-web/test/__lib__/index';

import TasksTabs from '../../../../src/main/resources/META-INF/resources/js/main_view/tasks/TasksTabs';

function getTab(label: string) {
	return screen.getByRole('button', {name: label});
}

function renderTasksTabs() {
	document.body.innerHTML = `
		<div hidden id="cmsAllTasksPanel">All tasks content</div>
		<div hidden id="cmsProjectTasksPanel">Project tasks content</div>
		<div hidden id="cmsWorkflowTasksPanel">Workflow tasks content</div>
	`;

	return render(<TasksTabs />);
}

describe('TasksTabs', () => {
	afterEach(() => {
		jest.restoreAllMocks();

		window.sessionStorage.clear();
	});

	it('shows the first tab by default', () => {
		renderTasksTabs();

		expect(getTab('all-tasks')).toHaveAttribute('aria-current', 'page');
		expect(screen.getByText('All tasks content')).toBeVisible();
		expect(screen.getByText('Project tasks content')).not.toBeVisible();
		expect(screen.getByText('Workflow tasks content')).not.toBeVisible();
	});

	it('shows the clicked tab and notifies which panel is active', async () => {
		const fire = jest.spyOn(Liferay, 'fire');

		renderTasksTabs();

		await userEvent.click(getTab('project-tasks'));

		expect(getTab('project-tasks')).toHaveAttribute('aria-current', 'page');
		expect(getTab('all-tasks')).not.toHaveAttribute('aria-current');
		expect(screen.getByText('Project tasks content')).toBeVisible();
		expect(screen.getByText('All tasks content')).not.toBeVisible();
		expect(fire).toHaveBeenCalledWith('tabsFragment:activePanel', {
			panel: screen.getByText('Project tasks content'),
		});
	});

	it('keeps the selected tab when the page is rendered again', async () => {
		const {unmount} = renderTasksTabs();

		await userEvent.click(getTab('workflow-tasks'));

		unmount();

		renderTasksTabs();

		expect(getTab('workflow-tasks')).toHaveAttribute(
			'aria-current',
			'page'
		);
		expect(screen.getByText('Workflow tasks content')).toBeVisible();
	});

	it('has no accessibility violations', async () => {
		const {container} = renderTasksTabs();

		await checkAccessibility({bestPractices: true, context: container});
	});
});
