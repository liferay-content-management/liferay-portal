/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import {performUserSwitchViaApi} from '../../../utils/performLogin';
import {SITE_CMS_SPACE_NAME} from '../../setup/site-cms-site/constants/space';
import {cmsPagesTest} from './fixtures/cmsPagesTest';

const test = mergeTests(
	cmsPagesTest,
	featureFlagsTest({
		'LPD-17564': {enabled: true},
	}),
	loginTest()
);

test(
	'CMS Administrator can edit the Friendly URL of any space',
	{tag: '@LPD-88344'},
	async ({page, spaceSummaryPage}) => {
		await performUserSwitchViaApi(page, 'cms.admin');

		await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

		await page.getByRole('button', {name: 'More Actions'}).click();

		await page.getByRole('menuitem', {name: 'Space Settings'}).click();

		await expect(
			page.getByRole('textbox', {name: 'Friendly URL Required'})
		).toBeEditable();
	}
);

test(
	'Space Administrator can edit the Friendly URL of a space they administer',
	{tag: '@LPD-88344'},
	async ({page, spaceSummaryPage}) => {
		await performUserSwitchViaApi(page, 'cms.space.admin');

		await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

		await page.getByRole('button', {name: 'More Actions'}).click();

		await page.getByRole('menuitem', {name: 'Space Settings'}).click();

		await expect(
			page.getByRole('textbox', {name: 'Friendly URL Required'})
		).toBeEditable();
	}
);

test(
	'Space Content Reviewer cannot access Space Settings',
	{tag: '@LPD-88344'},
	async ({page, spaceSummaryPage}) => {
		await performUserSwitchViaApi(page, 'cms.space.content.reviewer');

		await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

		await page.getByRole('button', {name: 'More Actions'}).click();

		await expect(
			page.getByRole('menuitem', {name: 'Space Settings'})
		).toHaveCount(0);
	}
);

test(
	'Space Member cannot access Space Settings',
	{tag: '@LPD-88344'},
	async ({page, spaceSummaryPage}) => {
		await performUserSwitchViaApi(page, 'cms.space.member');

		await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

		await page.getByRole('button', {name: 'More Actions'}).click();

		await expect(
			page.getByRole('menuitem', {name: 'Space Settings'})
		).toHaveCount(0);
	}
);
