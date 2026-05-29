/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../../fixtures/loginTest';
import getRandomString from '../../../../utils/getRandomString';
import {performUserSwitchViaApi} from '../../../../utils/performLogin';
import {PORTLET_URLS} from '../../../../utils/portletUrls';
import {waitForAlert} from '../../../../utils/waitForAlert';
import {SITE_CMS_SPACE_NAME} from '../../../setup/site-cms-site/constants/space';
import {cmsPagesTest} from '../fixtures/cmsPagesTest';

const test = mergeTests(
	cmsPagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPD-17564': {enabled: true},
		'LPD-34594': {enabled: true},
	}),
	loginTest()
);

test(
	'A Space Admin can manage space content and settings',
	{tag: '@LPD-85681'},
	async ({apiHelpers, assetsPage, page, spaceSummaryPage}) => {
		const applicationName = 'cms/basic-web-contents';
		const contentFolderName = `Folder ${getRandomString()}`;
		const contentTitle = `Title ${getRandomString()}`;
		const description = `Description ${getRandomString()}`;
		const fileFolderName = `Folder ${getRandomString()}`;

		await apiHelpers.objectEntry.postObjectEntry(
			{
				objectEntryFolderExternalReferenceCode: 'L_CONTENTS',
				title: contentTitle,
			},
			applicationName,
			SITE_CMS_SPACE_NAME
		);

		await performUserSwitchViaApi(page, 'cms.space.admin');

		await test.step('Manage space members', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.viewAllMembersLink.click();

			await page.getByRole('dialog').waitFor();

			await expect(
				page.getByLabel('Add People to Collaborate', {exact: true})
			).toBeVisible();

			await spaceSummaryPage.closeButton.click();
		});

		await test.step('Create a content folder', async () => {
			await expect(spaceSummaryPage.addContentButton).toBeVisible();

			await spaceSummaryPage.createContentFolder(contentFolderName);

			await expect(
				page.getByRole('link', {name: contentFolderName})
			).toBeVisible();
		});

		await test.step('Delete a content folder', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.viewAllContentLink.click();

			await assetsPage.execItemAction({
				action: 'Delete',
				filter: contentFolderName,
			});

			await waitForAlert(page, `${contentFolderName} was moved`);

			await expect(page.getByText(contentFolderName)).not.toBeVisible();
		});

		await test.step('Create a file folder', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.createFileFolder(fileFolderName);

			await expect(
				page.getByRole('link', {name: fileFolderName})
			).toBeVisible();
		});

		await test.step('Delete a file folder', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.viewAllFilesLink.click();

			await assetsPage.changeVisualizationMode('Table');

			await assetsPage.execItemAction({
				action: 'Delete',
				filter: fileFolderName,
			});

			await waitForAlert(page, `${fileFolderName} was moved`);

			await expect(page.getByText(fileFolderName)).not.toBeVisible();
		});

		await test.step('Delete content', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.viewAllContentLink.click();

			await assetsPage.execItemAction({
				action: 'Delete',
				filter: contentTitle,
			});

			await waitForAlert(page, `${contentTitle} was moved`);

			await expect(page.getByText(contentTitle)).not.toBeVisible();
		});

		await test.step('Change general settings', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await page.getByRole('button', {name: 'More Actions'}).click();
			await page.getByRole('menuitem', {name: 'Settings'}).click();

			const descriptionTextbox = page.getByRole('textbox', {
				name: 'Description',
			});

			await descriptionTextbox.fill(description);

			await page.getByRole('button', {name: 'Save'}).click();

			await waitForAlert(page, 'Success');

			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await page.getByRole('button', {name: 'More Actions'}).click();
			await page.getByRole('menuitem', {name: 'Settings'}).click();

			await expect(descriptionTextbox).toHaveValue(description);
		});

		await test.step('Open Permissions modal', async () => {
			await page.goto(PORTLET_URLS.cmsAllSpaces);

			await page
				.getByRole('row', {name: SITE_CMS_SPACE_NAME})
				.getByRole('button', {name: 'Actions'})
				.click();

			await page
				.getByRole('menuitem', {exact: true, name: 'Permissions'})
				.click();

			await page
				.getByRole('menuitem', {exact: true, name: 'Permissions'})
				.last()
				.click();

			await expect(
				page
					.getByRole('dialog')
					.getByRole('heading', {name: 'Permissions'})
			).toBeVisible();
		});
	}
);

test(
	'A Space Content Reviewer can create, edit, and delete content in a space',
	{tag: '@LPD-85670'},
	async ({apiHelpers, assetsPage, page, spaceSummaryPage}) => {
		const contentApplicationName = 'cms/basic-web-contents';
		const contentFolderName = `Folder ${getRandomString()}`;
		const contentTitle = `Title ${getRandomString()}`;
		const fileApplicationName = 'cms/basic-documents';
		const fileFolderName = `Folder ${getRandomString()}`;
		const fileTitle = `File ${getRandomString()}`;

		await apiHelpers.objectEntry.postObjectEntry(
			{
				objectEntryFolderExternalReferenceCode: 'L_CONTENTS',
				title: contentTitle,
			},
			contentApplicationName,
			SITE_CMS_SPACE_NAME
		);

		await apiHelpers.objectEntry.postObjectEntry(
			{
				file: {
					fileBase64: 'SGVsbG8sIHdvcmxkIQ==',
					name: `file_${getRandomString()}.txt`,
				},
				objectEntryFolderExternalReferenceCode: 'L_FILES',
				title: fileTitle,
			},
			fileApplicationName,
			SITE_CMS_SPACE_NAME
		);

		await performUserSwitchViaApi(page, 'cms.space.content.reviewer');

		await test.step('Create a content folder', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await expect(spaceSummaryPage.addContentButton).toBeVisible();

			await spaceSummaryPage.createContentFolder(contentFolderName);

			await expect(
				page.getByRole('link', {name: contentFolderName})
			).toBeVisible();
		});

		await test.step('Delete a content folder', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.viewAllContentLink.click();

			await assetsPage.execItemAction({
				action: 'Delete',
				filter: contentFolderName,
			});

			await waitForAlert(page, `${contentFolderName} was moved`);

			await expect(page.getByText(contentFolderName)).not.toBeVisible();
		});

		await test.step('Create a file folder', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.createFileFolder(fileFolderName);

			await expect(
				page.getByRole('link', {name: fileFolderName})
			).toBeVisible();
		});

		await test.step('Delete a file folder', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.viewAllFilesLink.click();

			await assetsPage.changeVisualizationMode('Table');

			await assetsPage.execItemAction({
				action: 'Delete',
				filter: fileFolderName,
			});

			await waitForAlert(page, `${fileFolderName} was moved`);

			await expect(page.getByText(fileFolderName)).not.toBeVisible();
		});

		await test.step('Edit content', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.viewAllContentLink.click();

			await assetsPage.execItemAction({
				action: 'Edit',
				filter: contentTitle,
			});

			await expect(page.getByLabel('Title')).toBeEditable();
		});

		await test.step('Delete content', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.viewAllContentLink.click();

			await assetsPage.execItemAction({
				action: 'Delete',
				filter: contentTitle,
			});

			await waitForAlert(page, `${contentTitle} was moved`);

			await expect(page.getByText(contentTitle)).not.toBeVisible();
		});

		await test.step('Delete a file', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.viewAllFilesLink.click();

			await assetsPage.changeVisualizationMode('Table');

			await assetsPage.execItemAction({
				action: 'Delete',
				filter: fileTitle,
			});

			await waitForAlert(page, `${fileTitle} was moved`);

			await expect(page.getByText(fileTitle)).not.toBeVisible();
		});
	}
);

test(
	'A Space Content Reviewer cannot manage space members or change space general settings',
	{tag: '@LPD-85681'},
	async ({page, spaceSummaryPage}) => {
		await performUserSwitchViaApi(page, 'cms.space.content.reviewer');

		await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

		await spaceSummaryPage.viewAllMembersLink.click();

		await page.getByRole('dialog').waitFor();

		await expect(
			page.getByLabel('Add People to Collaborate', {exact: true})
		).not.toBeVisible();

		await spaceSummaryPage.closeButton.click();

		await page.getByRole('button', {name: 'More Actions'}).click();

		await expect(
			page.getByRole('menuitem', {name: 'Settings'})
		).not.toBeVisible();
	}
);

test(
	'A Space Member has restricted permissions in a space',
	{tag: ['@LPD-85670', '@LPD-85681']},
	async ({apiHelpers, assetsPage, page, spaceSummaryPage}) => {
		const contentApplicationName = 'cms/basic-web-contents';
		const contentTitle = `Title ${getRandomString()}`;
		const fileApplicationName = 'cms/basic-documents';
		const fileTitle = `File ${getRandomString()}`;
		await apiHelpers.objectEntry.postObjectEntry(
			{
				objectEntryFolderExternalReferenceCode: 'L_CONTENTS',
				title: contentTitle,
			},
			contentApplicationName,
			SITE_CMS_SPACE_NAME
		);

		await apiHelpers.objectEntry.postObjectEntry(
			{
				file: {
					fileBase64: 'SGVsbG8sIHdvcmxkIQ==',
					name: `file_${getRandomString()}.txt`,
				},
				objectEntryFolderExternalReferenceCode: 'L_FILES',
				title: fileTitle,
			},
			fileApplicationName,
			SITE_CMS_SPACE_NAME
		);

		await performUserSwitchViaApi(page, 'cms.space.member');

		await test.step('Cannot see the Add Content or Add Files buttons', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await expect(spaceSummaryPage.addContentButton).not.toBeVisible();
			await expect(spaceSummaryPage.addFileButton).not.toBeVisible();
		});

		await test.step('Cannot edit content', async () => {
			await spaceSummaryPage.viewAllContentLink.click();

			await page
				.getByRole('row', {name: contentTitle})
				.getByRole('button', {name: `${contentTitle} Actions`})
				.click();

			await expect(
				page.getByRole('menuitem', {name: 'Edit'})
			).not.toBeVisible();
		});

		await test.step('Cannot delete content', async () => {
			await expect(
				page.getByRole('menuitem', {name: 'Delete'})
			).not.toBeVisible();
		});

		await test.step('Cannot delete a file', async () => {
			await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

			await spaceSummaryPage.viewAllFilesLink.click();

			await assetsPage.changeVisualizationMode('Table');

			await page
				.getByRole('row', {name: fileTitle})
				.getByRole('button', {name: `${fileTitle} Actions`})
				.click();

			await expect(
				page.getByRole('menuitem', {name: 'Delete'})
			).not.toBeVisible();
		});

		await test.step('Sees a restricted set of actions in the All Spaces view', async () => {
			await page.goto(PORTLET_URLS.cmsAllSpaces);

			const spaceRow = page.getByRole('row', {name: SITE_CMS_SPACE_NAME});

			await spaceRow.getByRole('button', {name: 'Actions'}).click();

			await expect(
				page.getByRole('menuitem', {name: 'View Members'})
			).toBeVisible();
			await expect(
				page.getByRole('menuitem', {name: 'View Connected Sites'})
			).toBeVisible();

			await expect(
				page.getByRole('menuitem', {name: 'Delete'})
			).not.toBeVisible();
			await expect(
				page.getByRole('menuitem', {name: 'Permissions'})
			).not.toBeVisible();
			await expect(
				page.getByRole('menuitem', {name: 'Settings'})
			).not.toBeVisible();
		});
	}
);
