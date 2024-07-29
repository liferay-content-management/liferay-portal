/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {loginTest} from '../../fixtures/loginTest';
import {notificationPagesTest} from '../../fixtures/notificationPagesTest';
import {workflowPagesTest} from '../../fixtures/workflowPagesTest';
import {blogsPagesTest} from '../../tests/blogs-web/fixtures/blogsPagesTest';
import {getRandomInt} from '../../utils/getRandomInt';

export const test = mergeTests(
	apiHelpersTest,
	blogsPagesTest,
	loginTest(),
	notificationPagesTest,
	workflowPagesTest
);

let assetType: string;
let blogTitle: string;
let workflowDefinitionName: string;

test.afterEach(async ({blogsPage, configurationTabPage}) => {
	if (assetType && workflowDefinitionName) {
		await configurationTabPage.goTo();

		await configurationTabPage.unassignWorkflowFromAssetType(assetType);
	}

	if (blogTitle) {
		await blogsPage.goto();
		await blogsPage.deleteAllBlogEntries();
	}

	assetType = null;
	blogTitle = null;
	workflowDefinitionName = null;
});

test('approve or reject modal appear even after doing a comment on the comments section', async ({
	blogsEditBlogEntryPage,
	blogsPage,
	configurationTabPage,
	page,
	workflowTaskDetailsPage,
}) => {
	await configurationTabPage.goTo();

	workflowDefinitionName = 'Single Approver';

	assetType = 'Blogs Entry';

	await configurationTabPage.assignWorkflowToAssetType(
		workflowDefinitionName,
		assetType
	);

	await blogsPage.goto();

	await blogsPage.goToCreateBlogEntry();

	blogTitle = 'Blog Title' + getRandomInt();

	await blogsEditBlogEntryPage.editBlogEntry({
		content: 'Blog content.',
		submitToWorkflow: true,
		title: blogTitle,
	});

	await workflowTaskDetailsPage.goToMyRolesTab();

	await workflowTaskDetailsPage.selectAsset(blogTitle);

	await page.waitForLoadState('networkidle');

	await workflowTaskDetailsPage.reviewActionMenu.click();

	await workflowTaskDetailsPage.assignToMeMenuItem.click();

	await workflowTaskDetailsPage.assigneeDoneButton.click();

	await workflowTaskDetailsPage.addComment('This is a comment');

	await workflowTaskDetailsPage.reviewActionMenu.click();

	await workflowTaskDetailsPage.approveMenuItem.click();

	await expect(page.getByRole('heading', {name: 'Approve'})).toBeVisible();

	await workflowTaskDetailsPage.cancelButton.click();

	await workflowTaskDetailsPage.reviewActionMenu.click();

	await workflowTaskDetailsPage.rejectMenuItem.click();

	await expect(page.getByRole('heading', {name: 'Reject'})).toBeVisible();
});
