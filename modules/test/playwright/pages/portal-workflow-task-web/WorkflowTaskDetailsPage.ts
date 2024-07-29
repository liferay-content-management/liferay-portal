/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {waitForSuccessAlert} from '../../utils/waitForSuccessAlert';
import {WorkflowTasksPage} from './WorkflowTasksPage';

export class WorkflowTaskDetailsPage {
	readonly approveMenuItem: Locator;
	readonly assignToMeMenuItem: Locator;
	readonly assignToMenuItem: Locator;
	readonly assigneeDoneButton: Locator;
	readonly cancelButton: Locator;
	readonly commentsButton: Locator;
	readonly doneButton: Locator;
	readonly page: Page;
	readonly rejectMenuItem: Locator;
	readonly replyButtom: Locator;
	readonly reviewActionMenu: Locator;
	readonly reviewComment: Locator;
	readonly workflowCommentsTextbox: Locator;
	readonly workflowTasksPage: WorkflowTasksPage;

	constructor(page: Page) {
		this.approveMenuItem = page.getByRole('menuitem', {name: 'approve'});
		this.assignToMeMenuItem = page.getByRole('link', {
			name: 'Assign to Me',
		});
		this.assignToMenuItem = page.getByRole('link', {name: 'Assign to...'});
		this.assigneeDoneButton = page
			.frameLocator(
				'iframe[name="_com_liferay_portal_workflow_task_web_portlet_MyWorkflowTaskPortlet_assignToDialog_iframe_"]'
			)
			.getByRole('button', {name: 'Done'});
		this.cancelButton = page.getByRole('button', {name: 'Cancel'});
		this.commentsButton = page.getByRole('button', {name: 'Comments'});
		this.doneButton = page.getByRole('button', {name: 'Done'});
		this.page = page;
		this.rejectMenuItem = page.getByRole('menuitem', {name: 'reject'});
		this.replyButtom = page.getByRole('button', {name: 'Reply'});
		this.reviewActionMenu = page.locator(
			'[id="_com_liferay_portal_workflow_task_web_portlet_MyWorkflowTaskPortlet_kldx___menu"]'
		);
		this.reviewComment = page.getByRole('textbox', {name: 'Comment'});
		this.workflowCommentsTextbox = page
			.frameLocator('iframe')
			.getByRole('textbox');
		this.workflowTasksPage = new WorkflowTasksPage(page);
	}

	async addComment(comment: string) {
		await this.commentsButton.click();
		await this.workflowCommentsTextbox.fill(comment);
		await this.replyButtom.click();
	}

	async clickAssigneeDoneButton() {
		await this.assigneeDoneButton.click();

		await waitForSuccessAlert(this.page);
	}

	async clickDoneButton() {
		await this.doneButton.click();

		await waitForSuccessAlert(this.page);
	}

	async goTo(assetTitle: string) {
		await this.workflowTasksPage.goto();

		await this.selectAsset(assetTitle);
	}

	async goToMyRolesTab() {
		await this.workflowTasksPage.goto();

		await this.page
			.getByRole('link', {name: 'Assigned to My Roles'})
			.click();
	}

	async selectAsset(assetTitle: string) {
		const assetLink = this.page.getByRole('link', {name: assetTitle});
		await assetLink.click({force: true});
	}

	async selectAssignee(assignee: string) {
		await this.page
			.frameLocator(
				'iframe[name="_com_liferay_portal_workflow_task_web_portlet_MyWorkflowTaskPortlet_assignToDialog_iframe_"]'
			)
			.getByLabel('Assign to')
			.selectOption(assignee);
	}
}
