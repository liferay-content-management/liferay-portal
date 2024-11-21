/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {mergeTests} from '@playwright/test';

import {isolatedSiteTest} from '../../fixtures/isolatedSiteTest';
import {loginTest} from '../../fixtures/loginTest';
import {assetCategoriesPagesTest} from './fixtures/assetCategoriesAdminPagesTest';

const test = mergeTests(
	assetCategoriesPagesTest,
	isolatedSiteTest,
	loginTest()
);

test('Add a vocabulary', async ({assetCategoriesAdminPage, page, site}) => {
	await assetCategoriesAdminPage.goto(site.friendlyUrlPath);

	await assetCategoriesAdminPage.addNewVocabulary();

	await page.pause();
});
