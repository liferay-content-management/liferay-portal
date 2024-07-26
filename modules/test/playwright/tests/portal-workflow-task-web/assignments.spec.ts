/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import {readFileSync} from 'fs';
import path from 'path';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {loginTest} from '../../fixtures/loginTest';
import {messageBoardsPagesTest} from '../../fixtures/messageBoardsTest';
import {userPersonalBarPagesTest} from '../../fixtures/userPersonalBarPagesTest';
import {workflowPagesTest} from '../../fixtures/workflowPagesTest';
import {getRandomInt} from '../../utils/getRandomInt';
import getRandomString from '../../utils/getRandomString';
import performLogin, {
	performLogout,
	performUserSwitch,
} from '../../utils/performLogin';
import {blogsPagesTest} from '../blogs-web/fixtures/blogsPagesTest';

export const test = mergeTests(
	apiHelpersTest,
	blogsPagesTest,
	loginTest(),
	workflowPagesTest,
	messageBoardsPagesTest,
	userPersonalBarPagesTest
);

let assetType: string;
let blogTitle: string;
let demoUserId: number;
let layoutId: string;
let messageBoardTitle: string;
let roleId: number;
let workflowDefinitionId: number;
let workflowDefinitionName: string;
let workflowXMLDefinition: string;

test.afterEach(
	async ({
		apiHelpers,
		blogsPage,
		configurationTabPage,
		messageBoardsPage,
		processBuilderPage,
	}) => {
		if (assetType && workflowDefinitionName) {
			await processBuilderPage.goto();

			await configurationTabPage.goTo();

			await configurationTabPage.unassignWorkflowFromAssetType(assetType);
		}

		if (blogTitle) {
			await blogsPage.goto();
			await blogsPage.deleteAllBlogEntries();
		}

		if (layoutId) {
			await apiHelpers.jsonWebServicesLayout.deleteLayout(layoutId);
		}

		if (messageBoardTitle) {
			await messageBoardsPage.goto();
			await messageBoardsPage.deleteAllMBEntries();
		}

		if (roleId && demoUserId) {
			await apiHelpers.headlessAdminUser.deleteRoleUserAccountAssociation(
				roleId,
				demoUserId
			);
		}

		if (workflowDefinitionId) {
			await apiHelpers.headlessAdminWorkflow.deleteWorkflowDefinition(
				workflowDefinitionId
			);
		}

		assetType = null;
		blogTitle = null;
		demoUserId = null;
		layoutId = null;
		messageBoardTitle = null;
		roleId = null;
		workflowDefinitionId = null;
		workflowDefinitionName = null;
		workflowXMLDefinition = null;
	}
);

test('send user back to my workflow tasks page after assign another user to review', async ({
	apiHelpers,
	blogsEditBlogEntryPage,
	blogsPage,
	configurationTabPage,
	diagramViewPage,
	page,
	processBuilderPage,
	workflowTaskDetailsPage,
	workflowTasksPage,
}) => {
	workflowDefinitionName = 'Workflow Definition' + getRandomString();

	workflowXMLDefinition = readFileSync(
		__dirname +
			'/dependencies/administrator-role-assignments-workflow-definition.xml',
		'utf-8'
	);

	const workflowDefinition =
		await apiHelpers.headlessAdminWorkflow.postWorkflowDefinitionSave(
			workflowDefinitionName,
			{content: workflowXMLDefinition}
		);

	workflowDefinitionId = workflowDefinition.id;

	const user =
		await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
			'demo.company.admin@liferay.com'
		);

	await processBuilderPage.goto();

	await processBuilderPage.clickWorkflowDefinitionName(
		workflowDefinitionName
	);

	await diagramViewPage.publishWorkflowDefinition();

	await diagramViewPage.goBack();

	await configurationTabPage.goTo();

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

	await performLogout(page);

	await performLogin(page, user.alternateName);

	await workflowTasksPage.goToAssignedToMyRoles();

	await workflowTaskDetailsPage.selectAsset(blogTitle);

	await page.waitForTimeout(3000);

	await workflowTaskDetailsPage.reviewActionMenu.click();

	await workflowTaskDetailsPage.assignToMenuItem.click();

	await page.waitForLoadState('networkidle');

	const user2 =
		await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
			'test@liferay.com'
		);

	await workflowTaskDetailsPage.selectAssignee(user2.id.toString());

	await workflowTaskDetailsPage.doneAssigneeButton.click();

	await expect(workflowTasksPage.assignedToMyRolesLink).toBeVisible();
});

test('logged user must be able to see workflow task at least from a read-only perspective', async ({
	apiHelpers,
	configurationTabPage,
	diagramViewPage,
	messageBoardsEditThreadPage,
	messageBoardsPage,
	messageBoardsWidgetPage,
	page,
	processBuilderPage,
	userPersonalBarPage,
	workflowTaskDetailsPage,
	workflowTasksPage,
}) => {
	const user =
		await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
			'demo.unprivileged@liferay.com'
		);

	demoUserId = user.id;

	const defaultUser =
		await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
			'test@liferay.com'
		);

	const rolesPermissionsJSON = await JSON.parse(
		readFileSync(
			path.join(
				__dirname,
				'../message-boards-web/dependencies/message-board-permissions.json'
			),
			'utf-8'
		)
	);

	const role = await apiHelpers.headlessAdminUser.postRole({
		name: 'MessageBoardAdmin' + getRandomInt(),
		rolePermissions: rolesPermissionsJSON,
		roleType: 'regular',
	});

	roleId = role.id;

	await apiHelpers.headlessAdminUser.assignUserToRole(role.name, user.id);

	const site = await apiHelpers.headlessSite.getSiteByERC('L_GUEST');

	const layout = await messageBoardsWidgetPage.addMessageBoardsPortlet(site);

	layoutId = layout.plid;

	await messageBoardsPage.setRoleCategoryPermissions(
		role.name.toLowerCase(),
		site.friendlyUrlPath
	);

	workflowDefinitionName = 'MBWorkflowDefinition' + getRandomInt();
	workflowXMLDefinition = readFileSync(
		__dirname +
			'/dependencies/administrator-role-assignments-workflow-definition.xml',
		'utf-8'
	);

	await apiHelpers.headlessAdminWorkflow.postWorkflowDefinitionSave(
		workflowDefinitionName,
		{content: workflowXMLDefinition}
	);

	await processBuilderPage.goto();

	await processBuilderPage.clickWorkflowDefinitionName(
		workflowDefinitionName
	);

	await diagramViewPage.publishWorkflowDefinition();

	await configurationTabPage.goTo();

	await configurationTabPage.assignWorkflowToAssetType(
		workflowDefinitionName,
		'Message Boards Message'
	);

	await performUserSwitch(page, user.alternateName);

	await page.goto(layout.titleCurrentValue);

	const threadTitle = 'ThreadTitle' + getRandomInt();

	messageBoardTitle = threadTitle;

	await messageBoardsEditThreadPage.publishNewThreadForWorkflow(
		threadTitle,
		'ThreadContent' + getRandomInt()
	);

	await performUserSwitch(page, defaultUser.alternateName);

	await workflowTasksPage.goToAssignedToMyRoles();

	await workflowTasksPage.assignToMe(threadTitle);

	await workflowTasksPage.reject(threadTitle);

	await performUserSwitch(page, user.alternateName);

	await userPersonalBarPage.notificationBadge.click();

	await page
		.getByRole('link', {
			name: `Your submission was rejected by ${defaultUser.name}, please modify and resubmit.`,
		})
		.first()
		.click();

	await workflowTaskDetailsPage.commentSectionButton.click();

	await workflowTaskDetailsPage.subscribeButton.click();

	await performUserSwitch(page, defaultUser.alternateName);

	await workflowTasksPage.goto();

	await workflowTaskDetailsPage.writeTaskComment(
		threadTitle,
		getRandomString()
	);

	await performUserSwitch(page, user.alternateName);

	await userPersonalBarPage.notificationBadge.click();

	await page
		.getByRole('link', {
			name: `${defaultUser.name} added a new comment to ${threadTitle}.`,
		})
		.click();

	await expect(workflowTaskDetailsPage.activitiesButton).toBeVisible();
	await expect(workflowTaskDetailsPage.previewMessageBoards).toBeVisible();
	await expect(workflowTaskDetailsPage.reviewActionMenu).toBeHidden();
	await expect(workflowTaskDetailsPage.viewButton).toBeHidden();
	await expect(workflowTaskDetailsPage.viewUsagesButton).toBeHidden();

	await performUserSwitch(page, defaultUser.alternateName);
});
