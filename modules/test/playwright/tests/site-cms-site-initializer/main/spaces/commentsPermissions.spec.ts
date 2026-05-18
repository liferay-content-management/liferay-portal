/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../../fixtures/loginTest';
import getRandomString from '../../../../utils/getRandomString';
import {cmsPagesTest} from '../fixtures/cmsPagesTest';
import {addRoleMemberAndSwitch} from './helpers/roleMembership';

const test = mergeTests(
	cmsPagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPD-11235': {enabled: false},
		'LPD-17564': {enabled: true},
	}),
	loginTest()
);

test(
	'A Space Administrator cannot edit or delete comments authored by a System Administrator',
	{tag: '@LPD-90003'},
	async ({apiHelpers, contentsPage, page, spaceSummaryPage}) => {
		const spaceName = `Space ${getRandomString()}`;
		const commentBody = `Admin comment ${getRandomString()}`;
		const contentTitle = `Untitled Asset`;

		await apiHelpers.headlessAssetLibrary.createAssetLibrary({
			name: spaceName,
			settings: {},
			type: 'Space',
		});

		await contentsPage.goto();

		await contentsPage.createContent('Blog', spaceName);

		await contentsPage.openSidePanel('Comments');

		const editor = page.getByLabel('Add Comment.');

		await expect(editor).toBeVisible();

		await editor.scrollIntoViewIfNeeded();
		await editor.click();
		await page.keyboard.type(commentBody);

		await page
			.getByRole('button', {exact: true, name: 'Save'})
			.first()
			.click();

		const adminComment = page
			.locator('article')
			.filter({hasText: commentBody});

		await expect(adminComment).toBeVisible();
		await expect(adminComment.getByTitle('actions')).toBeVisible();

		await addRoleMemberAndSwitch({
			apiHelpers,
			page,
			role: 'Space Administrator',
			spaceName,
			spaceSummaryPage,
		});

		await contentsPage.goto();

		const row = page.locator('.fds table tbody tr', {
			hasText: contentTitle,
		});

		await row.getByRole('link', {name: contentTitle}).click();

		await contentsPage.openSidePanel('Comments');

		const reloadedAdminComment = page
			.locator('article')
			.filter({hasText: commentBody});

		await expect(reloadedAdminComment).toBeVisible();

		await expect(reloadedAdminComment.getByTitle('actions')).toBeHidden();
	}
);
