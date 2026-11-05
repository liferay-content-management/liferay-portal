/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../fixtures/loginTest';
import getRandomString from '../../../utils/getRandomString';
import {cmsPagesTest} from './fixtures/cmsPagesTest';

const test = mergeTests(cmsPagesTest, dataApiHelpersTest, loginTest());

function getQuickFilterButton(page: Page, label: string) {
	return page.getByRole('button', {name: new RegExp(`^\\d+\\s*${label}$`)});
}

async function getQuickFilterCount(page: Page, label: string) {
	const text = await getQuickFilterButton(page, label).innerText();

	return Number(text.match(/\d+/)?.[0]);
}

async function expectResultsMatchQuickFilter(page: Page, label: string) {
	const count = await getQuickFilterCount(page, label);

	await expect(
		page.getByText(new RegExp(`^${count} Results? Found for`))
	).toBeVisible();
}

test(
	'Filters the tasks with the Pending, Overdue and Completed cards',
	{tag: '@LPD-113940'},
	async ({apiHelpers, page, viewTasksPage}) => {
		const space = await apiHelpers.headlessAssetLibrary.createSpace();

		const objectDefinition =
			await apiHelpers.objectAdmin.getObjectDefinitionByName(
				'CMSBasicWebContent'
			);

		const workflowDefinition =
			await apiHelpers.headlessAdminWorkflow.getWorkflowDefinitionByName(
				'Single Approver'
			);

		await apiHelpers.headlessAdminWorkflow.postWorkflowDefinitionLink(
			objectDefinition.className,
			space.siteId,
			workflowDefinition.id,
			workflowDefinition.name,
			Number(workflowDefinition.version)
		);

		const completedTitle = `Completed ${getRandomString()}`;
		const overdueTitle = `Overdue ${getRandomString()}`;
		const pendingTitle = `Pending ${getRandomString()}`;

		await apiHelpers.objectEntry.postObjectEntry(
			{
				objectEntryFolderExternalReferenceCode: 'L_CONTENTS',
				title: pendingTitle,
			},
			'cms/basic-web-contents',
			space.name
		);

		const completedObjectEntry =
			await apiHelpers.objectEntry.postObjectEntry(
				{
					objectEntryFolderExternalReferenceCode: 'L_CONTENTS',
					title: completedTitle,
				},
				'cms/basic-web-contents',
				space.name
			);

		const overdueObjectEntry = await apiHelpers.objectEntry.postObjectEntry(
			{
				objectEntryFolderExternalReferenceCode: 'L_CONTENTS',
				title: overdueTitle,
			},
			'cms/basic-web-contents',
			space.name
		);

		const userAccount =
			await apiHelpers.headlessAdminUser.getMyUserAccount();

		async function getWorkflowTask(objectEntryId: number) {
			let workflowTask;

			await expect(async () => {
				workflowTask =
					await apiHelpers.headlessAdminWorkflow.getWorkflowTaskByAsset(
						objectDefinition.className,
						String(objectEntryId)
					);

				expect(workflowTask).toBeTruthy();
			}).toPass();

			await apiHelpers.headlessAdminWorkflow.postAssignTaskToUser(
				workflowTask.id,
				userAccount.id
			);

			return workflowTask;
		}

		const completedWorkflowTask = await getWorkflowTask(
			completedObjectEntry.id
		);

		await apiHelpers.headlessAdminWorkflow.postWorkflowTaskChangeTransition(
			completedWorkflowTask.id,
			'approve'
		);

		const overdueWorkflowTask = await getWorkflowTask(
			overdueObjectEntry.id
		);

		await apiHelpers.headlessAdminWorkflow.postWorkflowTaskUpdateDueDate(
			overdueWorkflowTask.id,
			new Date(Date.parse(overdueWorkflowTask.dateCreated) + 1000)
		);

		await page.clock.install({time: Date.now() + 2 * 60 * 1000});

		await viewTasksPage.goto();

		const completedRow = page
			.getByRole('row')
			.filter({hasText: completedTitle});
		const overdueRow = page
			.getByRole('row')
			.filter({hasText: overdueTitle});
		const pendingRow = page
			.getByRole('row')
			.filter({hasText: pendingTitle});

		await test.step('Show the due date with its time', async () => {
			await expect(overdueRow).toContainText(/\d{1,2}:\d{2}/);
		});

		await test.step('Filter by the Pending card', async () => {
			await getQuickFilterButton(page, 'Pending').click();

			await expect(getQuickFilterButton(page, 'Pending')).toHaveAttribute(
				'aria-pressed',
				'true'
			);

			await expect(pendingRow).toBeVisible();
			await expect(overdueRow).toBeVisible();
			await expect(completedRow).toBeHidden();

			await expectResultsMatchQuickFilter(page, 'Pending');
		});

		await test.step('Filter by the Completed card', async () => {
			await getQuickFilterButton(page, 'Completed').click();

			await expect(
				getQuickFilterButton(page, 'Completed')
			).toHaveAttribute('aria-pressed', 'true');
			await expect(getQuickFilterButton(page, 'Pending')).toHaveAttribute(
				'aria-pressed',
				'false'
			);

			await expect(completedRow).toBeVisible();
			await expect(overdueRow).toBeHidden();
			await expect(pendingRow).toBeHidden();

			await expectResultsMatchQuickFilter(page, 'Completed');
		});

		await test.step('Filter by the Overdue card', async () => {
			await getQuickFilterButton(page, 'Overdue').click();

			await expect(getQuickFilterButton(page, 'Overdue')).toHaveAttribute(
				'aria-pressed',
				'true'
			);

			await expect(overdueRow).toBeVisible();
			await expect(completedRow).toBeHidden();
			await expect(pendingRow).toBeHidden();

			await expectResultsMatchQuickFilter(page, 'Overdue');
		});

		await test.step('Clear the filters', async () => {
			await page.getByRole('button', {name: 'Clear'}).click();

			for (const label of ['Completed', 'Overdue', 'Pending']) {
				await expect(getQuickFilterButton(page, label)).toHaveAttribute(
					'aria-pressed',
					'false'
				);
			}

			await expect(completedRow).toBeVisible();
			await expect(overdueRow).toBeVisible();
			await expect(pendingRow).toBeVisible();
		});
	}
);
