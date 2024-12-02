/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {isolatedSiteTest} from '../../fixtures/isolatedSiteTest';
import {loginTest} from '../../fixtures/loginTest';
import {journalPagesTest} from '../journal-web/fixtures/journalPagesTest';

export const test = mergeTests(
	apiHelpersTest,
	isolatedSiteTest,
	journalPagesTest,
	loginTest()
);

test(
	'Tooltip should be translated correctly',
	{
		tag: '@LPD-43309',
	},
	async ({apiHelpers, journalPage, page, site}) => {
		await test.step('Create a web content folder and access to edit it', async () => {
			const webContentFolder =
				await apiHelpers.jsonWebServicesJournal.addFolder({
					groupId: site.id,
				});

			await journalPage.goto(site.friendlyUrlPath);

			await journalPage.changeView('cards');

			await page
				.locator(`.card-body:has-text('${webContentFolder.name}')`)
				.getByLabel('More actions')
				.click();

			await page.getByRole('menuitem', {name: 'Edit'}).click();
		});

		await test.step('Check tooltip is translated', async () => {
			const svgElement = page.locator(
				'svg[aria-label="Folders can be restricted to only allow certain structures."]'
			);

			await expect(svgElement).toBeHidden();
		});
	}
);
