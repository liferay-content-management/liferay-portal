/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import {ApiHelpers} from '../../../helpers/ApiHelpers';
import {getRandomInt} from '../../../utils/getRandomInt';
import getRandomString from '../../../utils/getRandomString';
import {
	getUserAccount,
	performUserSwitch,
	performUserSwitchViaApi,
	userData,
} from '../../../utils/performLogin';
import {waitForAlert} from '../../../utils/waitForAlert';
import {
	SITE_CMS_SPACE_NAME,
	SITE_CMS_USER_ADMIN_NAMES,
	SITE_CMS_USER_EDIT_NAMES as SITE_CMS_USER_EDITOR_NAMES,
	SITE_CMS_USER_NAMES,
} from '../../setup/site-cms-site/constants/space';
import {cmsPagesTest} from './fixtures/cmsPagesTest';
import {AssetsPage} from './pages/AssetsPage';

const test = mergeTests(
	cmsPagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPD-17564': {enabled: true},
	}),
	loginTest()
);

test(
	'Can edit a folder',
	{tag: '@LPD-42841'},
	async ({apiHelpers, assetsPage, page}) => {
		const folderTitle = getRandomString();

		const folderData =
			await apiHelpers.objectFolder.createObjectEntryFolder({
				scopeKey: 'Default',
				title: folderTitle,
			});

		await assetsPage.gotoFiles();

		await assetsPage.execCardItemAction({
			action: 'Edit',
			filter: folderTitle,
		});

		const newFolderTitle = getRandomString();

		await page.getByLabel('Name').fill(newFolderTitle);
		await page.getByLabel('Description').fill('folder description');
		await page.getByRole('button', {name: 'Save'}).click();

		await waitForAlert(
			page,
			`Success:${newFolderTitle} was updated successfully.`
		);

		await expect(
			page.getByLabel(newFolderTitle, {exact: true})
		).toBeVisible();

		await apiHelpers.objectFolder.deleteObjectEntryFolder(folderData.id);
	}
);

test(
	'Folders should not show status',
	{tag: ['@LPD-78615', '@LPD-92355']},
	async ({apiHelpers, assetsPage, page}) => {
		const folderTitle = getRandomString();

		await apiHelpers.objectFolder.createObjectEntryFolder({
			parentObjectEntryFolderExternalReferenceCode: 'L_CONTENTS',
			scopeKey: 'Default',
			title: folderTitle,
		});

		await assetsPage.gotoContents();

		const row = page
			.getByRole('row')
			.filter({has: page.getByRole('link', {name: folderTitle})});

		await expect(row.locator('.cell-embedded-status')).toHaveText('--');
	}
);

test(
	'Folders have View Folder action, but not View',
	{tag: '@LPD-58720'},
	async ({apiHelpers, assetsPage, page}) => {
		const folderTitle = getRandomString();

		const folderData =
			await apiHelpers.objectFolder.createObjectEntryFolder({
				scopeKey: 'Default',
				title: folderTitle,
			});

		await assetsPage.gotoFiles();

		assetsPage
			.getCardItem(folderTitle)
			.getByLabel(`${folderTitle} Actions`)
			.click();

		expect(
			page.getByRole('menuitem', {exact: true, name: 'View'})
		).toBeHidden();
		expect(
			page.getByRole('menuitem', {exact: true, name: 'View Folder'})
		).toBeVisible();

		await apiHelpers.objectFolder.deleteObjectEntryFolder(folderData.id);
	}
);

test(
	'Info panel shows correct number of assets in folder',
	{tag: '@LPD-69166'},
	async ({apiHelpers, assetsPage, page}) => {
		const folderName = `Folder ${getRandomInt()}`;

		const folder = await apiHelpers.objectFolder.createObjectEntryFolder({
			parentObjectEntryFolderExternalReferenceCode: 'L_FILES',
			scopeKey: 'Default',
			title: folderName,
		});

		try {
			await apiHelpers.objectEntry.postObjectEntry(
				{
					file: {
						fileBase64: 'R0lGODlhAQABAAAAACw=',
						name: `file_${getRandomString()}.png`,
					},
					objectEntryFolderExternalReferenceCode:
						folder.externalReferenceCode,
					title: `Content ${getRandomInt()}`,
				},
				'cms/basic-documents',
				'Default'
			);

			await apiHelpers.objectFolder.createObjectEntryFolder({
				parentObjectEntryFolderExternalReferenceCode:
					folder.externalReferenceCode,
				scopeKey: 'Default',
				title: `Subfolder ${getRandomInt()}`,
			});

			await assetsPage.gotoFiles();

			await page.getByLabel(/View Selected/i).click();
			await page.getByRole('option', {name: 'Table'}).click();

			await page
				.getByRole('row', {name: folderName})
				.getByRole('checkbox')
				.check();

			await page.getByRole('button', {name: 'Show Info Panel'}).click();

			expect(
				await page.getByTestId('number-of-assets').textContent()
			).toContain('2');
		}
		finally {
			await apiHelpers.objectFolder.deleteObjectEntryFolder(folder.id);
		}
	}
);

test(
	'Duplicating a folder creates a copy in the same parent',
	{tag: '@LPD-88657'},
	async ({apiHelpers, assetsPage, page}) => {
		const folderName = `Folder ${getRandomString()}`;
		const spaceName = 'Default';

		await test.step('Create a folder in the Space', async () => {
			await apiHelpers.objectFolder.createObjectEntryFolder({
				parentObjectEntryFolderExternalReferenceCode: 'L_CONTENTS',
				scopeKey: spaceName,
				title: folderName,
			});
		});

		await test.step('Duplicate the folder', async () => {
			await assetsPage.gotoContents(spaceName);

			await assetsPage.execItemAction({
				action: 'Duplicate',
				filter: folderName,
				parentAction: 'Copy',
			});

			await expect(
				page.getByRole('link', {
					exact: true,
					name: `${folderName} (Copy)`,
				})
			).toBeVisible();
		});

		await test.step('Duplicate the original again and check the suffix increments', async () => {
			await assetsPage.execItemAction({
				action: 'Duplicate',
				filter: folderName,
				parentAction: 'Copy',
			});

			await expect(
				page.getByRole('link', {
					exact: true,
					name: `${folderName} (Copy 1)`,
				})
			).toBeVisible();
		});
	}
);

test(
	'Shared folder shows a shared icon in the Files section only for the recipient',
	{tag: '@LPD-66045'},
	async ({apiHelpers, assetsPage, page}) => {
		const folderTitle1 = `Folder ${getRandomString()}`;
		const folderTitle2 = `Folder ${getRandomString()}`;
		const spaceName = `Space ${getRandomString()}`;

		const space = await apiHelpers.headlessAssetLibrary.createAssetLibrary({
			name: spaceName,
			settings: {},
			type: 'Space',
		});

		const objectEntryFolder1 =
			await apiHelpers.objectFolder.createObjectEntryFolder({
				parentObjectEntryFolderExternalReferenceCode: 'L_FILES',
				scopeKey: space.assetLibraryKey,
				title: folderTitle1,
			});

		const objectEntryFolder2 =
			await apiHelpers.objectFolder.createObjectEntryFolder({
				parentObjectEntryFolderExternalReferenceCode: 'L_FILES',
				scopeKey: space.assetLibraryKey,
				title: folderTitle2,
			});

		try {
			const user = await apiHelpers.headlessAdminUser.postUserAccount();

			userData[user.alternateName] = {
				name: user.givenName,
				password: 'test',
				surname: user.familyName,
			};

			const cmsAdminRole =
				await apiHelpers.headlessAdminUser.getRoleByName(
					'CMS Administrator'
				);

			await apiHelpers.headlessAdminUser.postRoleUserAccountAssociation(
				cmsAdminRole.id,
				Number(user.id)
			);

			await apiHelpers.objectFolder.postObjectEntryFolderCollaborators(
				[
					{
						actionIds: ['VIEW'],
						id: user.id,
						share: true,
						type: 'User',
					},
				],
				objectEntryFolder1.id
			);

			await performUserSwitch(page, user.alternateName);

			await assetsPage.gotoFiles();

			await assetsPage.changeVisualizationMode('Table');

			const folderRow1 = page
				.getByRole('row')
				.filter({has: page.getByRole('link', {name: folderTitle1})});

			await expect(folderRow1).toBeVisible();

			await expect(
				folderRow1.locator('.lexicon-icon-users').first()
			).toBeVisible();

			const folderRow2 = page
				.getByRole('row')
				.filter({has: page.getByRole('link', {name: folderTitle2})});

			await expect(folderRow2).toBeVisible();

			await expect(folderRow2.locator('.lexicon-icon-users')).toHaveCount(
				0
			);
		}
		finally {
			await apiHelpers.objectFolder.deleteObjectEntryFolder(
				objectEntryFolder1.id
			);

			await apiHelpers.objectFolder.deleteObjectEntryFolder(
				objectEntryFolder2.id
			);
		}
	}
);

function createFolder(
	apiHelpers: ApiHelpers,
	parentExternalReferenceCode: string
) {
	return apiHelpers.objectFolder.createObjectEntryFolder({
		parentObjectEntryFolderExternalReferenceCode:
			parentExternalReferenceCode,
		scopeKey: SITE_CMS_SPACE_NAME,
		title: `Folder ${getRandomString()}`,
	});
}

async function expectRowActionHidden(
	page: Page,
	folderTitle: string,
	action: string
) {
	await page
		.getByRole('row', {name: folderTitle})
		.getByRole('button', {name: `${folderTitle} Actions`})
		.click();

	const menu = page.getByRole('menu');

	await expect(menu).toBeVisible();

	await expect(
		menu.getByRole('menuitem', {exact: true, name: action})
	).toBeHidden();

	await page.keyboard.press('Escape');
}

// Opens a folder row's Actions menu and drives toward the permissions dialog as
// far as the current user is allowed: "Permissions" is a submenu whose child
// "Permissions" item opens the dialog. Users who cannot define permissions
// either lack the parent entry (Space Member) or the child entry (Content
// Reviewer), so the dialog never opens.

async function attemptOpenFolderPermissions(page: Page, folderTitle: string) {
	await page
		.getByRole('row', {name: folderTitle})
		.getByRole('button', {name: `${folderTitle} Actions`})
		.click();

	await page.getByRole('menu').first().waitFor();

	const permissionsItems = page.getByRole('menuitem', {
		exact: true,
		name: 'Permissions',
	});

	if (await permissionsItems.count()) {
		await permissionsItems.first().click();

		if ((await permissionsItems.count()) > 1) {
			await permissionsItems.last().click();
		}
	}
}

interface FolderDestination {
	id: string;
	space: string;
	title: string;
}

// Moves or copies a folder to a destination, retrying the whole flow on
// failure. Success is verified by navigating into the destination folder by its
// direct URL and asserting the folder landed there. This is robust to two
// quirks of the file sections for non-admin users: the destination space can be
// intermittently missing from the dialog (re-navigating clears it), and an
// operation can redirect to the portal home, which would otherwise hide the
// success toast. Short per-step timeouts let a failed attempt retry quickly.

async function relocateFolderWithRetry(
	assetsPage: AssetsPage,
	page: Page,
	goto: () => Promise<void>,
	args: {
		action: 'Copy' | 'Move';
		destination: FolderDestination;
		itemTitle: string;
	}
) {
	await expect(async () => {
		await goto();

		await assetsPage.changeVisualizationMode('Table');

		if (args.action === 'Move') {
			await assetsPage.execItemAction({
				action: 'Move',
				filter: args.itemTitle,
			});
		}
		else {
			await assetsPage.execItemAction({
				action: 'Copy To',
				filter: args.itemTitle,
				parentAction: 'Copy',
			});
		}

		const dialog = assetsPage.getCopyOrMoveDestinationDialog();

		await dialog.waitFor({timeout: 15000});

		await dialog.getByLabel(args.destination.space).click({timeout: 15000});

		await dialog
			.getByRole('radio', {
				exact: true,
				name: `Select ${args.destination.title}`,
			})
			.click({timeout: 15000});

		await dialog.getByRole('button', {exact: true, name: 'Select'}).click();

		await assetsPage.gotoFolder(
			args.destination.id,
			args.destination.title
		);

		await expect(
			page.getByRole('link', {name: args.itemTitle})
		).toBeVisible({timeout: 15000});
	}).toPass({intervals: [1000, 3000, 5000], timeout: 60000});
}

// Provisions a second Space plus a destination folder under both the Default
// space and the second Space, for each content/file area, so move and copy can
// target both "another folder in the same space" and "another space".

async function createMoveCopyDestinations(
	apiHelpers: ApiHelpers,
	spaceName: string
) {
	const secondSpace =
		await apiHelpers.headlessAssetLibrary.createAssetLibrary({
			name: `Space ${getRandomString()}`,
			settings: {},
			type: 'Space',
		});

	// Grant the space-scoped managing users their roles in the second space so
	// it is a selectable destination for them; otherwise only the CMS Admin,
	// who sees every space, could move or copy across spaces.

	const spaceRolesByUser = {
		'cms.space.admin': [
			'Asset Library Administrator',
			'Asset Library Member',
		],
		'cms.space.content.reviewer': [
			'Asset Library Content Reviewer',
			'Asset Library Member',
		],
	};

	for (const [screenName, roleNames] of Object.entries(spaceRolesByUser)) {
		const {externalReferenceCode} = getUserAccount(screenName);

		await apiHelpers.headlessAssetLibrary.putAssetLibraryUserAccount(
			secondSpace.externalReferenceCode,
			externalReferenceCode
		);

		await apiHelpers.headlessAssetLibrary.putAssetLibraryUserAccountRoles(
			secondSpace.externalReferenceCode,
			externalReferenceCode,
			roleNames
		);
	}

	async function createDestination(
		parentExternalReferenceCode,
		scopeKey,
		space
	): Promise<FolderDestination> {
		const folder = await apiHelpers.objectFolder.createObjectEntryFolder({
			parentObjectEntryFolderExternalReferenceCode:
				parentExternalReferenceCode,
			scopeKey,
			title: `Destination ${getRandomString()}`,
		});

		return {id: folder.id, space, title: folder.title};
	}

	const destinationsByType: Record<string, FolderDestination[]> = {
		content: [
			await createDestination('L_CONTENTS', spaceName, spaceName),
			await createDestination(
				'L_CONTENTS',
				secondSpace.assetLibraryKey,
				secondSpace.name
			),
		],
		file: [
			await createDestination('L_FILES', spaceName, spaceName),
			await createDestination(
				'L_FILES',
				secondSpace.assetLibraryKey,
				secondSpace.name
			),
		],
	};

	return destinationsByType;
}

test(
	'A folder can be added in every section by users who can edit content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, folderPage, page, spaceSummaryPage}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');
		const fileFolder = await createFolder(apiHelpers, 'L_FILES');

		const addFolder = async () => {
			const folderName = `Folder ${getRandomString()}`;

			await folderPage.createFolder(folderName, SITE_CMS_SPACE_NAME);

			await expect(
				page.getByRole('link', {name: folderName})
			).toBeVisible();
		};

		for (const userName of SITE_CMS_USER_EDITOR_NAMES) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} can add a folder in Contents`, async () => {
				await assetsPage.gotoContents();

				await addFolder();
			});

			await test.step(`${userName} can add a folder in Files`, async () => {
				await assetsPage.gotoFiles();

				await addFolder();
			});

			await test.step(`${userName} can add a folder in Space > Contents`, async () => {
				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);

				await addFolder();
			});

			await test.step(`${userName} can add a folder in Space > Files`, async () => {
				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);

				await addFolder();
			});

			await test.step(`${userName} can add a folder in Space > Contents > Folder`, async () => {
				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await addFolder();
			});

			await test.step(`${userName} can add a folder in Space > Files > Folder`, async () => {
				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await addFolder();
			});

			await test.step(`${userName} can add a folder in Space > Summary`, async () => {
				await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

				await spaceSummaryPage.createContentFolder(
					`Folder ${getRandomString()}`
				);

				await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

				await spaceSummaryPage.createFileFolder(
					`Folder ${getRandomString()}`
				);
			});
		}
	}
);

test(
	'A folder cannot be added in any section by users who cannot edit content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page, spaceSummaryPage}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');

		const fileFolder = await createFolder(apiHelpers, 'L_FILES');

		const nonEditorUserNames = SITE_CMS_USER_NAMES.filter(
			(userName) => !SITE_CMS_USER_EDITOR_NAMES.includes(userName)
		);

		for (const userName of nonEditorUserNames) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} cannot add a folder in Contents`, async () => {
				await assetsPage.gotoContents();

				await expect(assetsPage.newButton).toBeHidden();
			});

			await test.step(`${userName} cannot add a folder in Files`, async () => {
				await assetsPage.gotoFiles();

				await expect(assetsPage.newButton).toBeHidden();
			});

			await test.step(`${userName} cannot add a folder in Space > Contents`, async () => {
				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);

				await expect(assetsPage.newButton).toBeHidden();
			});

			await test.step(`${userName} cannot add a folder in Space > Files`, async () => {
				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);

				await expect(assetsPage.newButton).toBeHidden();
			});

			await test.step(`${userName} cannot add a folder in Space > Contents > Folder`, async () => {
				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await expect(assetsPage.newButton).toBeHidden();
			});

			await test.step(`${userName} cannot add a folder in Space > Files > Folder`, async () => {
				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await expect(assetsPage.newButton).toBeHidden();
			});

			await test.step(`${userName} cannot add a folder in Space > Summary`, async () => {
				await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

				await expect(spaceSummaryPage.addContentButton).toBeHidden();
				await expect(spaceSummaryPage.addFileButton).toBeHidden();
			});
		}
	}
);

test(
	'A folder can be navigated into by clicking its title by every user',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page, spaceSummaryPage}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');
		const contentSubfolder = await createFolder(
			apiHelpers,
			contentFolder.externalReferenceCode
		);
		const fileFolder = await createFolder(apiHelpers, 'L_FILES');
		const fileSubfolder = await createFolder(
			apiHelpers,
			fileFolder.externalReferenceCode
		);

		const navigateIntoFolder = async (folderTitle: string) => {
			await page
				.getByRole('link', {exact: true, name: folderTitle})
				.click();

			await expect(page).toHaveURL(/view-folder/);

			await expect(
				page.getByTestId(`testId${folderTitle}`)
			).toBeVisible();
		};

		for (const userName of SITE_CMS_USER_NAMES) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} can navigate into a folder in Contents`, async () => {
				await assetsPage.gotoContents();
				await navigateIntoFolder(contentFolder.title);
			});

			await test.step(`${userName} can navigate into a folder in Files`, async () => {
				await assetsPage.gotoFiles();
				await navigateIntoFolder(fileFolder.title);
			});

			await test.step(`${userName} can navigate into a folder in Space > Contents`, async () => {
				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);
				await navigateIntoFolder(contentFolder.title);
			});

			await test.step(`${userName} can navigate into a folder in Space > Files`, async () => {
				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);
				await navigateIntoFolder(fileFolder.title);
			});

			await test.step(`${userName} can navigate into a folder in Space > Contents > Folder`, async () => {
				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await navigateIntoFolder(contentSubfolder.title);
			});

			await test.step(`${userName} can navigate into a folder in Space > Files > Folder`, async () => {
				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await navigateIntoFolder(fileSubfolder.title);
			});

			await test.step(`${userName} can navigate into a folder in Space > Summary`, async () => {
				await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

				await navigateIntoFolder(contentFolder.title);

				await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

				await navigateIntoFolder(fileFolder.title);
			});
		}
	}
);

test(
	'A folder can be deleted in every section by users who can edit content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page, spaceSummaryPage}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');
		const fileFolder = await createFolder(apiHelpers, 'L_FILES');

		const expectFolderDeleted = async (folderTitle: string) => {
			await waitForAlert(page, `${folderTitle} was moved`);

			await expect(page.getByText(folderTitle)).not.toBeVisible();
		};

		const deleteFolderRow = async (folderTitle: string) => {
			await assetsPage.changeVisualizationMode('Table');

			await assetsPage.execItemAction({
				action: 'Delete',
				filter: folderTitle,
			});

			await expectFolderDeleted(folderTitle);
		};

		for (const userName of SITE_CMS_USER_EDITOR_NAMES) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} can delete a folder in Contents`, async () => {
				const folder = await createFolder(apiHelpers, 'L_CONTENTS');

				await assetsPage.gotoContents();

				await deleteFolderRow(folder.title);
			});

			await test.step(`${userName} can delete a folder in Files`, async () => {
				const folder = await createFolder(apiHelpers, 'L_FILES');

				await assetsPage.gotoFiles();

				await deleteFolderRow(folder.title);
			});

			await test.step(`${userName} can delete a folder in Space > Contents`, async () => {
				const folder = await createFolder(apiHelpers, 'L_CONTENTS');

				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);

				await deleteFolderRow(folder.title);
			});

			await test.step(`${userName} can delete a folder in Space > Files`, async () => {
				const folder = await createFolder(apiHelpers, 'L_FILES');

				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);

				await deleteFolderRow(folder.title);
			});

			await test.step(`${userName} can delete a folder in Space > Contents > Folder`, async () => {
				const folder = await createFolder(
					apiHelpers,
					contentFolder.externalReferenceCode
				);

				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await deleteFolderRow(folder.title);
			});

			await test.step(`${userName} can delete a folder in Space > Files > Folder`, async () => {
				const folder = await createFolder(
					apiHelpers,
					fileFolder.externalReferenceCode
				);

				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await deleteFolderRow(folder.title);
			});

			await test.step(`${userName} can delete a folder in Space > Summary`, async () => {
				let folder = await createFolder(apiHelpers, 'L_CONTENTS');

				await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

				await assetsPage.execItemAction({
					action: 'Delete',
					filter: folder.title,
				});

				await expectFolderDeleted(folder.title);

				folder = await createFolder(apiHelpers, 'L_FILES');

				await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

				await assetsPage.execCardItemAction({
					action: 'Delete',
					filter: folder.title,
				});

				await expectFolderDeleted(folder.title);
			});
		}
	}
);

test(
	'A folder cannot be deleted in any section by users who cannot edit content NEXT',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');
		const contentSubfolder = await createFolder(
			apiHelpers,
			contentFolder.externalReferenceCode
		);
		const fileFolder = await createFolder(apiHelpers, 'L_FILES');
		const fileSubfolder = await createFolder(
			apiHelpers,
			fileFolder.externalReferenceCode
		);

		const nonEditorUserNames = SITE_CMS_USER_NAMES.filter(
			(userName) => !SITE_CMS_USER_EDITOR_NAMES.includes(userName)
		);

		const expectCannotDeleteFolder = async (folderTitle: string) => {
			await assetsPage.changeVisualizationMode('Table');

			await expectRowActionHidden(page, folderTitle, 'Delete');
		};

		for (const userName of nonEditorUserNames) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} cannot delete a folder in Contents`, async () => {
				await assetsPage.gotoContents();

				await expectCannotDeleteFolder(contentFolder.title);
			});

			await test.step(`${userName} cannot delete a folder in Files`, async () => {
				await assetsPage.gotoFiles();

				await expectCannotDeleteFolder(fileFolder.title);
			});

			await test.step(`${userName} cannot delete a folder in Space > Contents`, async () => {
				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);

				await expectCannotDeleteFolder(contentFolder.title);
			});

			await test.step(`${userName} cannot delete a folder in Space > Files`, async () => {
				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);

				await expectCannotDeleteFolder(fileFolder.title);
			});

			await test.step(`${userName} cannot delete a folder in Space > Contents > Folder`, async () => {
				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await expectCannotDeleteFolder(contentSubfolder.title);
			});

			await test.step(`${userName} cannot delete a folder in Space > Files > Folder`, async () => {
				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await expectCannotDeleteFolder(fileSubfolder.title);
			});
		}
	}
);

test(
	'A folder can be edited in every section by users who can manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');

		const fileFolder = await createFolder(apiHelpers, 'L_FILES');

		const editFolderRow = async (folder: {
			externalReferenceCode: string;
			title: string;
		}) => {
			await assetsPage.changeVisualizationMode('Table');

			await assetsPage.execItemAction({
				action: 'Edit',
				filter: folder.title,
			});

			const newTitle = `Folder ${getRandomString()}`;

			const nameField = page.getByLabel('Name');

			// Wait for the edit form to finish loading the current name before
			// replacing it; otherwise the value can be overwritten as the form
			// hydrates and the rename is lost.

			await expect(nameField).toHaveValue(folder.title);

			await nameField.fill(newTitle);

			await page.getByRole('button', {name: 'Save'}).click();

			// Verify the rename persisted by polling the folder by its external
			// reference code. This is robust to the differing post-save behavior
			// across sections (an in-place modal in some, a redirect in others).

			await expect
				.poll(
					async () => {
						const updatedFolder =
							await apiHelpers.objectFolder.getObjectEntryFolderByExternalReferenceCode(
								{
									externalReferenceCode:
										folder.externalReferenceCode,
									scopeKey: SITE_CMS_SPACE_NAME,
								}
							);

						return updatedFolder.title;
					},
					{timeout: 30000}
				)
				.toBe(newTitle);
		};

		for (const userName of SITE_CMS_USER_EDITOR_NAMES) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} can edit a folder in Contents`, async () => {
				const folder = await createFolder(
					apiHelpers,

					'L_CONTENTS'
				);

				await assetsPage.gotoContents();

				await editFolderRow(folder);
			});

			await test.step(`${userName} can edit a folder in Files`, async () => {
				const folder = await createFolder(
					apiHelpers,

					'L_FILES'
				);

				await assetsPage.gotoFiles();

				await editFolderRow(folder);
			});

			await test.step(`${userName} can edit a folder in Space > Contents`, async () => {
				const folder = await createFolder(
					apiHelpers,

					'L_CONTENTS'
				);

				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);

				await editFolderRow(folder);
			});

			await test.step(`${userName} can edit a folder in Space > Files`, async () => {
				const folder = await createFolder(
					apiHelpers,

					'L_FILES'
				);

				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);

				await editFolderRow(folder);
			});

			await test.step(`${userName} can edit a folder in Space > Contents > Folder`, async () => {
				const folder = await createFolder(
					apiHelpers,

					contentFolder.externalReferenceCode
				);

				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await editFolderRow(folder);
			});

			await test.step(`${userName} can edit a folder in Space > Files > Folder`, async () => {
				const folder = await createFolder(
					apiHelpers,

					fileFolder.externalReferenceCode
				);

				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await editFolderRow(folder);
			});
		}
	}
);

test(
	'A folder cannot be edited in any section by users who cannot manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');
		const contentSubfolder = await createFolder(
			apiHelpers,
			contentFolder.externalReferenceCode
		);
		const fileFolder = await createFolder(apiHelpers, 'L_FILES');
		const fileSubfolder = await createFolder(
			apiHelpers,
			fileFolder.externalReferenceCode
		);

		const nonEditorUserNames = SITE_CMS_USER_NAMES.filter(
			(userName) => !SITE_CMS_USER_EDITOR_NAMES.includes(userName)
		);

		const expectCannotEditFolder = async (folderTitle: string) => {
			await assetsPage.changeVisualizationMode('Table');

			await expectRowActionHidden(page, folderTitle, 'Edit');
		};

		for (const userName of nonEditorUserNames) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} cannot edit a folder in Contents`, async () => {
				await assetsPage.gotoContents();

				await expectCannotEditFolder(contentFolder.title);
			});

			await test.step(`${userName} cannot edit a folder in Files`, async () => {
				await assetsPage.gotoFiles();

				await expectCannotEditFolder(fileFolder.title);
			});

			await test.step(`${userName} cannot edit a folder in Space > Contents`, async () => {
				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);

				await expectCannotEditFolder(contentFolder.title);
			});

			await test.step(`${userName} cannot edit a folder in Space > Files`, async () => {
				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);

				await expectCannotEditFolder(fileFolder.title);
			});

			await test.step(`${userName} cannot edit a folder in Space > Contents > Folder`, async () => {
				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await expectCannotEditFolder(contentSubfolder.title);
			});

			await test.step(`${userName} cannot edit a folder in Space > Files > Folder`, async () => {
				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await expectCannotEditFolder(fileSubfolder.title);
			});
		}
	}
);

test(
	'Folder permissions can be defined in every section by space administrators',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');
		const contentSubfolder = await createFolder(
			apiHelpers,
			contentFolder.externalReferenceCode
		);
		const fileFolder = await createFolder(apiHelpers, 'L_FILES');
		const fileSubfolder = await createFolder(
			apiHelpers,
			fileFolder.externalReferenceCode
		);

		const defineFolderPermissions = async (folderTitle: string) => {
			await assetsPage.changeVisualizationMode('Table');

			await attemptOpenFolderPermissions(page, folderTitle);

			await expect(
				page.getByRole('dialog').getByRole('heading', {
					name: 'Permissions',
				})
			).toBeVisible();

			await page.keyboard.press('Escape');
		};

		for (const userName of SITE_CMS_USER_ADMIN_NAMES) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} can define folder permissions in Contents`, async () => {
				await assetsPage.gotoContents();

				await defineFolderPermissions(contentFolder.title);
			});

			await test.step(`${userName} can define folder permissions in Files`, async () => {
				await assetsPage.gotoFiles();

				await defineFolderPermissions(fileFolder.title);
			});

			await test.step(`${userName} can define folder permissions in Space > Contents`, async () => {
				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);

				await defineFolderPermissions(contentFolder.title);
			});

			await test.step(`${userName} can define folder permissions in Space > Files`, async () => {
				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);

				await defineFolderPermissions(fileFolder.title);
			});

			await test.step(`${userName} can define folder permissions in Space > Contents > Folder`, async () => {
				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await defineFolderPermissions(contentSubfolder.title);
			});

			await test.step(`${userName} can define folder permissions in Space > Files > Folder`, async () => {
				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await defineFolderPermissions(fileSubfolder.title);
			});
		}
	}
);

test(
	'Folder permissions cannot be defined in any section by users who cannot administer the space',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');
		const contentSubfolder = await createFolder(
			apiHelpers,
			contentFolder.externalReferenceCode
		);
		const fileFolder = await createFolder(apiHelpers, 'L_FILES');
		const fileSubfolder = await createFolder(
			apiHelpers,
			fileFolder.externalReferenceCode
		);

		const nonAdminUserNames = SITE_CMS_USER_NAMES.filter(
			(userName) => !SITE_CMS_USER_ADMIN_NAMES.includes(userName)
		);

		const expectCannotDefineFolderPermissions = async (
			folderTitle: string
		) => {
			await assetsPage.changeVisualizationMode('Table');

			await attemptOpenFolderPermissions(page, folderTitle);

			await expect(
				page.getByRole('dialog').getByRole('heading', {
					name: 'Permissions',
				})
			).toBeHidden();

			await page.keyboard.press('Escape');
		};

		for (const userName of nonAdminUserNames) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} cannot define folder permissions in Contents`, async () => {
				await assetsPage.gotoContents();

				await expectCannotDefineFolderPermissions(contentFolder.title);
			});

			await test.step(`${userName} cannot define folder permissions in Files`, async () => {
				await assetsPage.gotoFiles();

				await expectCannotDefineFolderPermissions(fileFolder.title);
			});

			await test.step(`${userName} cannot define folder permissions in Space > Contents`, async () => {
				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);

				await expectCannotDefineFolderPermissions(contentFolder.title);
			});

			await test.step(`${userName} cannot define folder permissions in Space > Files`, async () => {
				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);

				await expectCannotDefineFolderPermissions(fileFolder.title);
			});

			await test.step(`${userName} cannot define folder permissions in Space > Contents > Folder`, async () => {
				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await expectCannotDefineFolderPermissions(
					contentSubfolder.title
				);
			});

			await test.step(`${userName} cannot define folder permissions in Space > Files > Folder`, async () => {
				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await expectCannotDefineFolderPermissions(fileSubfolder.title);
			});
		}
	}
);

test(
	'Content can be added inside a folder in every section by users who can manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');
		const contentSubfolder = await createFolder(
			apiHelpers,
			contentFolder.externalReferenceCode
		);
		const fileFolder = await createFolder(apiHelpers, 'L_FILES');
		const fileSubfolder = await createFolder(
			apiHelpers,
			fileFolder.externalReferenceCode
		);

		const addContentInsideFolder = async (
			folder: {id: string; title: string},
			type: 'content' | 'file'
		) => {
			await assetsPage.gotoFolder(folder.id, folder.title);

			await assetsPage.newButton.click();

			const contentType =
				type === 'content' ? 'Basic Web Content' : 'Single File';

			await expect(
				page.getByRole('menuitem', {name: contentType})
			).toBeVisible();

			await page.keyboard.press('Escape');
		};

		for (const userName of SITE_CMS_USER_EDITOR_NAMES) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} can add content inside a folder in Contents`, async () => {
				await addContentInsideFolder(contentFolder, 'content');
			});

			await test.step(`${userName} can add content inside a folder in Files`, async () => {
				await addContentInsideFolder(fileFolder, 'file');
			});

			await test.step(`${userName} can add content inside a folder in Space > Contents`, async () => {
				await addContentInsideFolder(contentFolder, 'content');
			});

			await test.step(`${userName} can add content inside a folder in Space > Files`, async () => {
				await addContentInsideFolder(fileFolder, 'file');
			});

			await test.step(`${userName} can add content inside a folder in Space > Contents > Folder`, async () => {
				await addContentInsideFolder(contentSubfolder, 'content');
			});

			await test.step(`${userName} can add content inside a folder in Space > Files > Folder`, async () => {
				await addContentInsideFolder(fileSubfolder, 'file');
			});
		}
	}
);

test(
	'Content cannot be added inside a folder in any section by users who cannot manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');

		const fileFolder = await createFolder(apiHelpers, 'L_FILES');

		const nonEditorUserNames = SITE_CMS_USER_NAMES.filter(
			(userName) => !SITE_CMS_USER_EDITOR_NAMES.includes(userName)
		);

		const expectCannotAddContentInsideFolder = async (folder: {
			id: string;
			title: string;
		}) => {
			await assetsPage.gotoFolder(folder.id, folder.title);

			await expect(assetsPage.newButton).toBeHidden();
		};

		for (const userName of nonEditorUserNames) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} cannot add content inside a folder in Contents`, async () => {
				const folder = await createFolder(apiHelpers, 'L_CONTENTS');

				await expectCannotAddContentInsideFolder(folder);
			});

			await test.step(`${userName} cannot add content inside a folder in Files`, async () => {
				const folder = await createFolder(apiHelpers, 'L_FILES');

				await expectCannotAddContentInsideFolder(folder);
			});

			await test.step(`${userName} cannot add content inside a folder in Space > Contents`, async () => {
				const folder = await createFolder(apiHelpers, 'L_CONTENTS');

				await expectCannotAddContentInsideFolder(folder);
			});

			await test.step(`${userName} cannot add content inside a folder in Space > Files`, async () => {
				const folder = await createFolder(apiHelpers, 'L_FILES');

				await expectCannotAddContentInsideFolder(folder);
			});

			await test.step(`${userName} cannot add content inside a folder in Space > Contents > Folder`, async () => {
				const folder = await createFolder(
					apiHelpers,
					contentFolder.externalReferenceCode
				);

				await expectCannotAddContentInsideFolder(folder);
			});

			await test.step(`${userName} cannot add content inside a folder in Space > Files > Folder`, async () => {
				const folder = await createFolder(
					apiHelpers,
					fileFolder.externalReferenceCode
				);

				await expectCannotAddContentInsideFolder(folder);
			});
		}
	}
);

test(
	'A folder can be moved to another folder and another space by users who can manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {

		// This matrix performs many moves and may retry across spaces, so it
		// needs more than the default per-test budget.

		test.setTimeout(8 * 60 * 1000);

		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');

		const fileFolder = await createFolder(apiHelpers, 'L_FILES');

		const destinationsByType = await createMoveCopyDestinations(
			apiHelpers,
			SITE_CMS_SPACE_NAME
		);

		for (const userName of SITE_CMS_USER_EDITOR_NAMES) {
			await performUserSwitchViaApi(page, userName);

			// A move relocates its source, so each (section, destination) needs
			// its own freshly created source folder.

			const moveFolderInSection = async (
				label: string,
				goto: () => Promise<void>,
				parentExternalReferenceCode: string,
				type: 'content' | 'file'
			) => {
				for (const destination of destinationsByType[type]) {
					const source = await createFolder(
						apiHelpers,
						parentExternalReferenceCode
					);

					await test.step(`${userName} can move a folder in ${label} to ${destination.space}`, () =>
						relocateFolderWithRetry(assetsPage, page, goto, {
							action: 'Move',
							destination,
							itemTitle: source.title,
						}));
				}
			};

			await moveFolderInSection(
				'Contents',
				() => assetsPage.gotoContents(),
				'L_CONTENTS',
				'content'
			);

			await moveFolderInSection(
				'Files',
				() => assetsPage.gotoFiles(),
				'L_FILES',
				'file'
			);

			await moveFolderInSection(
				'Space > Contents',
				() => assetsPage.gotoContents(SITE_CMS_SPACE_NAME),
				'L_CONTENTS',
				'content'
			);

			await moveFolderInSection(
				'Space > Files',
				() => assetsPage.gotoFiles(SITE_CMS_SPACE_NAME),
				'L_FILES',
				'file'
			);

			await moveFolderInSection(
				'Space > Contents > Folder',
				() =>
					assetsPage.gotoFolder(
						contentFolder.id,
						contentFolder.title
					),
				contentFolder.externalReferenceCode,
				'content'
			);

			await moveFolderInSection(
				'Space > Files > Folder',
				() => assetsPage.gotoFolder(fileFolder.id, fileFolder.title),
				fileFolder.externalReferenceCode,
				'file'
			);
		}
	}
);

test(
	'A folder cannot be moved in any section by users who cannot manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');

		const fileFolder = await createFolder(apiHelpers, 'L_FILES');

		const nonEditorUserNames = SITE_CMS_USER_NAMES.filter(
			(userName) => !SITE_CMS_USER_EDITOR_NAMES.includes(userName)
		);

		const expectCannotMoveFolder = async (folderTitle: string) => {
			await assetsPage.changeVisualizationMode('Table');

			await expectRowActionHidden(page, folderTitle, 'Move');
		};

		for (const userName of nonEditorUserNames) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} cannot move a folder in Contents`, async () => {
				const folder = await createFolder(apiHelpers, 'L_CONTENTS');

				await assetsPage.gotoContents();

				await expectCannotMoveFolder(folder.title);
			});

			await test.step(`${userName} cannot move a folder in Files`, async () => {
				const folder = await createFolder(apiHelpers, 'L_FILES');

				await assetsPage.gotoFiles();

				await expectCannotMoveFolder(folder.title);
			});

			await test.step(`${userName} cannot move a folder in Space > Contents`, async () => {
				const folder = await createFolder(apiHelpers, 'L_CONTENTS');

				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);

				await expectCannotMoveFolder(folder.title);
			});

			await test.step(`${userName} cannot move a folder in Space > Files`, async () => {
				const folder = await createFolder(apiHelpers, 'L_FILES');

				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);

				await expectCannotMoveFolder(folder.title);
			});

			await test.step(`${userName} cannot move a folder in Space > Contents > Folder`, async () => {
				const folder = await createFolder(
					apiHelpers,
					contentFolder.externalReferenceCode
				);

				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await expectCannotMoveFolder(folder.title);
			});

			await test.step(`${userName} cannot move a folder in Space > Files > Folder`, async () => {
				const folder = await createFolder(
					apiHelpers,
					fileFolder.externalReferenceCode
				);

				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await expectCannotMoveFolder(folder.title);
			});
		}
	}
);

test(
	'A folder can be copied to another folder and another space by users who can manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {

		// This matrix performs many copies and may retry the cross-space copy,
		// so it needs more than the default per-test budget.

		test.setTimeout(8 * 60 * 1000);

		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');

		const fileFolder = await createFolder(apiHelpers, 'L_FILES');

		const destinationsByType = await createMoveCopyDestinations(
			apiHelpers,
			SITE_CMS_SPACE_NAME
		);

		for (const userName of SITE_CMS_USER_EDITOR_NAMES) {
			await performUserSwitchViaApi(page, userName);

			// Use a fresh source per (section, destination) so that a redirect
			// after one copy cannot disturb the next one.

			const copyFolderInSection = async (
				label: string,
				goto: () => Promise<void>,
				parentExternalReferenceCode: string,
				type: 'content' | 'file'
			) => {
				for (const destination of destinationsByType[type]) {
					const source = await createFolder(
						apiHelpers,
						parentExternalReferenceCode
					);

					await test.step(`${userName} can copy a folder in ${label} to ${destination.space}`, () =>
						relocateFolderWithRetry(assetsPage, page, goto, {
							action: 'Copy',
							destination,
							itemTitle: source.title,
						}));
				}
			};

			await copyFolderInSection(
				'Contents',
				() => assetsPage.gotoContents(),
				'L_CONTENTS',
				'content'
			);

			await copyFolderInSection(
				'Files',
				() => assetsPage.gotoFiles(),
				'L_FILES',
				'file'
			);

			await copyFolderInSection(
				'Space > Contents',
				() => assetsPage.gotoContents(SITE_CMS_SPACE_NAME),
				'L_CONTENTS',
				'content'
			);

			await copyFolderInSection(
				'Space > Files',
				() => assetsPage.gotoFiles(SITE_CMS_SPACE_NAME),
				'L_FILES',
				'file'
			);

			await copyFolderInSection(
				'Space > Contents > Folder',
				() =>
					assetsPage.gotoFolder(
						contentFolder.id,
						contentFolder.title
					),
				contentFolder.externalReferenceCode,
				'content'
			);

			await copyFolderInSection(
				'Space > Files > Folder',
				() => assetsPage.gotoFolder(fileFolder.id, fileFolder.title),
				fileFolder.externalReferenceCode,
				'file'
			);
		}
	}
);

test(
	'A folder cannot be copied in any section by users who cannot manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const contentFolder = await createFolder(apiHelpers, 'L_CONTENTS');

		const fileFolder = await createFolder(apiHelpers, 'L_FILES');

		const nonEditorUserNames = SITE_CMS_USER_NAMES.filter(
			(userName) => !SITE_CMS_USER_EDITOR_NAMES.includes(userName)
		);

		const expectCannotCopyFolder = async (folderTitle: string) => {
			await assetsPage.changeVisualizationMode('Table');

			await expectRowActionHidden(page, folderTitle, 'Copy');
		};

		for (const userName of nonEditorUserNames) {
			await performUserSwitchViaApi(page, userName);

			await test.step(`${userName} cannot copy a folder in Contents`, async () => {
				const folder = await createFolder(apiHelpers, 'L_CONTENTS');

				await assetsPage.gotoContents();

				await expectCannotCopyFolder(folder.title);
			});

			await test.step(`${userName} cannot copy a folder in Files`, async () => {
				const folder = await createFolder(apiHelpers, 'L_FILES');

				await assetsPage.gotoFiles();

				await expectCannotCopyFolder(folder.title);
			});

			await test.step(`${userName} cannot copy a folder in Space > Contents`, async () => {
				const folder = await createFolder(apiHelpers, 'L_CONTENTS');

				await assetsPage.gotoContents(SITE_CMS_SPACE_NAME);

				await expectCannotCopyFolder(folder.title);
			});

			await test.step(`${userName} cannot copy a folder in Space > Files`, async () => {
				const folder = await createFolder(apiHelpers, 'L_FILES');

				await assetsPage.gotoFiles(SITE_CMS_SPACE_NAME);

				await expectCannotCopyFolder(folder.title);
			});

			await test.step(`${userName} cannot copy a folder in Space > Contents > Folder`, async () => {
				const folder = await createFolder(
					apiHelpers,
					contentFolder.externalReferenceCode
				);

				await assetsPage.gotoFolder(
					contentFolder.id,
					contentFolder.title
				);

				await expectCannotCopyFolder(folder.title);
			});

			await test.step(`${userName} cannot copy a folder in Space > Files > Folder`, async () => {
				const folder = await createFolder(
					apiHelpers,
					fileFolder.externalReferenceCode
				);

				await assetsPage.gotoFolder(fileFolder.id, fileFolder.title);

				await expectCannotCopyFolder(folder.title);
			});
		}
	}
);
