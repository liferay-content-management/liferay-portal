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
	SITE_CMS_USER_NAMES,
	SITE_CMS_USER_ADMIN_NAMES,
	SITE_CMS_USER_EDIT_NAMES,
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
			await assetsPage.gotoSpaceContents(spaceName);

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

// Folder permission matrix (LPD-85556). Each folder use case is exercised for
// every CMS user role across every section a folder can live in. Because the
// fixture provisions a single Space ("Default") where the cms.space.* users
// hold their roles, the expected outcome for a (use case, role) pair is the
// same in every section; looping the sections verifies the UI enforces that
// permission consistently at each entry point. SITE_CMS_USERS_EDIT holds the
// users that can manage content and SITE_CMS_USERS_ADMIN the users that can
// administer the space.

interface FolderRef {
	externalReferenceCode: string;
	id: string;
	title: string;
}

interface FolderSection {
	goto: (assetsPage: AssetsPage, spaceName: string) => Promise<void>;
	label: string;
	parentExternalReferenceCode: string;
	type: 'content' | 'file';
}

// Builds the six "listing" sections (the seventh, Space > Summary, has no folder
// row listing and is handled inline per use case). For the two "inside a folder"
// sections the target folders are created under the given parent folders.

function makeFolderSections(parents: {
	contentFolder: FolderRef;
	fileFolder: FolderRef;
}): FolderSection[] {
	return [
		{
			goto: async (assetsPage) => assetsPage.gotoContents(),
			label: 'Contents',
			parentExternalReferenceCode: 'L_CONTENTS',
			type: 'content',
		},
		{
			goto: async (assetsPage) => assetsPage.gotoFiles(),
			label: 'Files',
			parentExternalReferenceCode: 'L_FILES',
			type: 'file',
		},
		{
			goto: async (assetsPage, spaceName) =>
				assetsPage.gotoSpaceContents(spaceName),
			label: 'Space > Contents',
			parentExternalReferenceCode: 'L_CONTENTS',
			type: 'content',
		},
		{
			goto: async (assetsPage, spaceName) =>
				assetsPage.gotoSpaceFiles(spaceName),
			label: 'Space > Files',
			parentExternalReferenceCode: 'L_FILES',
			type: 'file',
		},
		{
			goto: async (assetsPage) =>
				assetsPage.gotoFolder(
					parents.contentFolder.id,
					parents.contentFolder.title
				),
			label: 'Space > Contents > Folder',
			parentExternalReferenceCode:
				parents.contentFolder.externalReferenceCode,
			type: 'content',
		},
		{
			goto: async (assetsPage) =>
				assetsPage.gotoFolder(
					parents.fileFolder.id,
					parents.fileFolder.title
				),
			label: 'Space > Files > Folder',
			parentExternalReferenceCode:
				parents.fileFolder.externalReferenceCode,
			type: 'file',
		},
	];
}

// Creates the two parent folders used by the "inside a folder" sections.

async function createParentFolders(apiHelpers: ApiHelpers, spaceName: string) {
	const contentFolder = await apiHelpers.objectFolder.createObjectEntryFolder(
		{
			parentObjectEntryFolderExternalReferenceCode: 'L_CONTENTS',
			scopeKey: spaceName,
			title: `Parent Content ${getRandomString()}`,
		}
	);

	const fileFolder = await apiHelpers.objectFolder.createObjectEntryFolder({
		parentObjectEntryFolderExternalReferenceCode: 'L_FILES',
		scopeKey: spaceName,
		title: `Parent File ${getRandomString()}`,
	});

	return {contentFolder, fileFolder};
}

// Creates one target folder per section, shared across users (for read-only use
// cases such as navigate and permissions).

async function createTargetsPerSection(
	apiHelpers: ApiHelpers,
	spaceName: string,
	sections: FolderSection[]
) {
	const targets = new Map<string, FolderRef>();

	for (const section of sections) {
		targets.set(
			section.label,
			await apiHelpers.objectFolder.createObjectEntryFolder({
				parentObjectEntryFolderExternalReferenceCode:
					section.parentExternalReferenceCode,
				scopeKey: spaceName,
				title: `Folder ${getRandomString()}`,
			})
		);
	}

	return targets;
}

// Creates one target folder per (user, section), for use cases that mutate the
// folder (delete renames it away, edit renames it) and therefore cannot share a
// single target across the user loop.

async function createTargetsPerUserSection(
	apiHelpers: ApiHelpers,
	spaceName: string,
	sections: FolderSection[]
) {
	const targets = new Map<string, FolderRef>();

	for (const user of SITE_CMS_USER_NAMES) {
		for (const section of sections) {
			targets.set(
				`${user}|${section.label}`,
				await apiHelpers.objectFolder.createObjectEntryFolder({
					parentObjectEntryFolderExternalReferenceCode:
						section.parentExternalReferenceCode,
					scopeKey: spaceName,
					title: `Folder ${getRandomString()}`,
				})
			);
		}
	}

	return targets;
}

// Navigates to a section and forces the Table visualization so folders are
// listed as rows with an Actions menu.

async function gotoTableListing(
	assetsPage: AssetsPage,
	section: FolderSection,
	spaceName: string
) {
	await section.goto(assetsPage, spaceName);

	await assetsPage.changeVisualizationMode('Table');
}

// Asserts that a folder row's Actions menu does not offer the given action.

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
	section: FolderSection,
	spaceName: string,
	args: {
		action: 'Copy' | 'Move';
		destination: FolderDestination;
		itemTitle: string;
	}
) {
	await expect(async () => {
		await gotoTableListing(assetsPage, section, spaceName);

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
	'A folder can be added in every section only by users who can manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, folderPage, page, spaceSummaryPage}) => {
		const parents = await createParentFolders(apiHelpers, SITE_CMS_SPACE_NAME);

		const sections = makeFolderSections(parents);

		for (const userName of SITE_CMS_USER_NAMES) {
			await performUserSwitchViaApi(page, userName);

			const canManage = SITE_CMS_USER_EDIT_NAMES.includes(userName);

			for (const section of sections) {
				await test.step(`${userName} ${canManage ? 'can' : 'cannot'} add a folder in ${section.label}`, async () => {
					await section.goto(assetsPage, SITE_CMS_SPACE_NAME);

					if (canManage) {
						const folderName = `Folder ${getRandomString()}`;

						await folderPage.createFolder(folderName, SITE_CMS_SPACE_NAME);

						await expect(
							page.getByRole('link', {name: folderName})
						).toBeVisible();
					}
					else {
						await expect(assetsPage.newButton).toBeHidden();
					}
				});
			}

			await test.step(`${userName} ${canManage ? 'can' : 'cannot'} add a folder in Space > Summary`, async () => {
				await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

				if (canManage) {
					await spaceSummaryPage.createContentFolder(
						`Folder ${getRandomString()}`
					);

					await spaceSummaryPage.goto(SITE_CMS_SPACE_NAME);

					await spaceSummaryPage.createFileFolder(
						`Folder ${getRandomString()}`
					);
				}
				else {
					await expect(
						spaceSummaryPage.addContentButton
					).toBeHidden();
					await expect(spaceSummaryPage.addFileButton).toBeHidden();
				}
			});
		}
	}
);

test(
	'A folder can be navigated into by clicking its title by every user',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const spaceName = SITE_CMS_SPACE_NAME;

		const parents = await createParentFolders(apiHelpers, spaceName);

		const sections = makeFolderSections(parents);

		// Create one target folder per section up front, as an admin.

		const targets = await createTargetsPerSection(
			apiHelpers,
			spaceName,
			sections
		);

		for (const user of SITE_CMS_USER_NAMES) {
			await performUserSwitchViaApi(page, user);

			for (const section of sections) {
				await test.step(`${user} can navigate into a folder in ${section.label}`, async () => {
					await section.goto(assetsPage, spaceName);

					const folder = targets.get(section.label);

					await page
						.getByRole('link', {
							exact: true,
							name: folder.title,
						})
						.click();

					await expect(page).toHaveURL(/view-folder/);

					await expect(
						page.getByTestId(`testId${folder.title}`)
					).toBeVisible();
				});
			}
		}
	}
);

test(
	'A folder can be deleted in every section only by users who can manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const spaceName = SITE_CMS_SPACE_NAME;

		const parents = await createParentFolders(apiHelpers, spaceName);

		const sections = makeFolderSections(parents);

		const targets = await createTargetsPerUserSection(
			apiHelpers,
			spaceName,
			sections
		);

		for (const user of SITE_CMS_USER_NAMES) {
			await performUserSwitchViaApi(page, user);

			const canManage = SITE_CMS_USER_EDIT_NAMES.includes(user);

			for (const section of sections) {
				const folder = targets.get(`${user}|${section.label}`);

				await test.step(`${user} ${canManage ? 'can' : 'cannot'} delete a folder in ${section.label}`, async () => {
					await gotoTableListing(assetsPage, section, spaceName);

					if (canManage) {
						await assetsPage.execItemAction({
							action: 'Delete',
							filter: folder.title,
						});

						await waitForAlert(page, `${folder.title} was moved`);

						await expect(
							page.getByText(folder.title)
						).not.toBeVisible();
					}
					else {
						await expectRowActionHidden(
							page,
							folder.title,
							'Delete'
						);
					}
				});
			}
		}
	}
);

test(
	'A folder can be edited in every section only by users who can manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const spaceName = SITE_CMS_SPACE_NAME;

		const parents = await createParentFolders(apiHelpers, spaceName);

		const sections = makeFolderSections(parents);

		const targets = await createTargetsPerUserSection(
			apiHelpers,
			spaceName,
			sections
		);

		for (const user of SITE_CMS_USER_NAMES) {
			await performUserSwitchViaApi(page, user);

			const canManage = SITE_CMS_USER_EDIT_NAMES.includes(user);

			for (const section of sections) {
				const folder = targets.get(`${user}|${section.label}`);

				await test.step(`${user} ${canManage ? 'can' : 'cannot'} edit a folder in ${section.label}`, async () => {
					await gotoTableListing(assetsPage, section, spaceName);

					if (canManage) {
						await assetsPage.execItemAction({
							action: 'Edit',
							filter: folder.title,
						});

						const newTitle = `Folder ${getRandomString()}`;

						const nameField = page.getByLabel('Name');

						// Wait for the edit form to finish loading the current
						// name before replacing it; otherwise the value can be
						// overwritten as the form hydrates and the rename is
						// lost.

						await expect(nameField).toHaveValue(folder.title);

						await nameField.fill(newTitle);

						await page.getByRole('button', {name: 'Save'}).click();

						// Verify the rename persisted by polling the folder
						// by its external reference code. This is robust to
						// the differing post-save behavior across sections
						// (an in-place modal in some, a redirect in others).

						await expect
							.poll(
								async () => {
									const updatedFolder =
										await apiHelpers.objectFolder.getObjectEntryFolderByExternalReferenceCode(
											{
												externalReferenceCode:
													folder.externalReferenceCode,
												scopeKey: spaceName,
											}
										);

									return updatedFolder.title;
								},
								{timeout: 30000}
							)
							.toBe(newTitle);
					}
					else {
						await expectRowActionHidden(page, folder.title, 'Edit');
					}
				});
			}
		}
	}
);

test(
	'Folder permissions can be defined in every section only by space administrators',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const spaceName = SITE_CMS_SPACE_NAME;

		const parents = await createParentFolders(apiHelpers, spaceName);

		const sections = makeFolderSections(parents);

		const targets = await createTargetsPerSection(
			apiHelpers,
			spaceName,
			sections
		);

		for (const user of SITE_CMS_USER_NAMES) {
			await performUserSwitchViaApi(page, user);

			const canAdminister = SITE_CMS_USER_ADMIN_NAMES.includes(user);

			for (const section of sections) {
				const folder = targets.get(section.label);

				await test.step(`${user} ${canAdminister ? 'can' : 'cannot'} define folder permissions in ${section.label}`, async () => {
					await gotoTableListing(assetsPage, section, spaceName);

					await attemptOpenFolderPermissions(page, folder.title);

					const permissionsDialog = page
						.getByRole('dialog')
						.getByRole('heading', {name: 'Permissions'});

					if (canAdminister) {
						await expect(permissionsDialog).toBeVisible();
					}
					else {
						await expect(permissionsDialog).toBeHidden();
					}

					await page.keyboard.press('Escape');
				});
			}
		}
	}
);

test(
	'Content can be added inside a folder in every section only by users who can manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {
		const spaceName = SITE_CMS_SPACE_NAME;

		const parents = await createParentFolders(apiHelpers, spaceName);

		const sections = makeFolderSections(parents);

		const targets = await createTargetsPerSection(
			apiHelpers,
			spaceName,
			sections
		);

		for (const user of SITE_CMS_USER_NAMES) {
			await performUserSwitchViaApi(page, user);

			const canManage = SITE_CMS_USER_EDIT_NAMES.includes(user);

			for (const section of sections) {
				const folder = targets.get(section.label);

				const contentType =
					section.type === 'content'
						? 'Basic Web Content'
						: 'Single File';

				await test.step(`${user} ${canManage ? 'can' : 'cannot'} add content inside a folder in ${section.label}`, async () => {
					await assetsPage.gotoFolder(folder.id, folder.title);

					if (canManage) {
						await assetsPage.newButton.click();

						await expect(
							page.getByRole('menuitem', {name: contentType})
						).toBeVisible();

						await page.keyboard.press('Escape');
					}
					else {
						await expect(assetsPage.newButton).toBeHidden();
					}
				});
			}
		}
	}
);

test(
	'A folder can be moved to another folder and another space only by users who can manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {

		// This matrix performs many moves and may retry across spaces, so it
		// needs more than the default per-test budget.

		test.setTimeout(8 * 60 * 1000);

		const spaceName = SITE_CMS_SPACE_NAME;

		const parents = await createParentFolders(apiHelpers, spaceName);

		const sections = makeFolderSections(parents);

		const destinationsByType = await createMoveCopyDestinations(
			apiHelpers,
			spaceName
		);

		// A move relocates its source, so each (user, section, destination)
		// needs its own source folder.

		const sources = new Map<string, FolderRef>();

		for (const user of SITE_CMS_USER_NAMES) {
			for (const section of sections) {
				const destinations = destinationsByType[section.type];

				for (let index = 0; index < destinations.length; index++) {
					sources.set(
						`${user}|${section.label}|${index}`,
						await apiHelpers.objectFolder.createObjectEntryFolder({
							parentObjectEntryFolderExternalReferenceCode:
								section.parentExternalReferenceCode,
							scopeKey: spaceName,
							title: `Folder ${getRandomString()}`,
						})
					);
				}
			}
		}

		for (const user of SITE_CMS_USER_NAMES) {
			await performUserSwitchViaApi(page, user);

			const canManage = SITE_CMS_USER_EDIT_NAMES.includes(user);

			for (const section of sections) {
				const destinations = destinationsByType[section.type];

				if (canManage) {
					for (let index = 0; index < destinations.length; index++) {
						const destination = destinations[index];
						const source = sources.get(
							`${user}|${section.label}|${index}`
						);

						await test.step(`${user} can move a folder in ${section.label} to ${destination.space}`, async () => {
							await relocateFolderWithRetry(
								assetsPage,
								page,
								section,
								spaceName,
								{
									action: 'Move',
									destination,
									itemTitle: source.title,
								}
							);
						});
					}
				}
				else {
					const source = sources.get(`${user}|${section.label}|0`);

					await test.step(`${user} cannot move a folder in ${section.label}`, async () => {
						await gotoTableListing(assetsPage, section, spaceName);

						await expectRowActionHidden(page, source.title, 'Move');
					});
				}
			}
		}
	}
);

test(
	'A folder can be copied to another folder and another space only by users who can manage content',
	{tag: '@LPD-85556'},
	async ({apiHelpers, assetsPage, page}) => {

		// This matrix performs many copies and may retry the cross-space copy,
		// so it needs more than the default per-test budget.

		test.setTimeout(8 * 60 * 1000);

		const spaceName = SITE_CMS_SPACE_NAME;

		const parents = await createParentFolders(apiHelpers, spaceName);

		const sections = makeFolderSections(parents);

		const destinationsByType = await createMoveCopyDestinations(
			apiHelpers,
			spaceName
		);

		// Use a fresh source per (user, section, destination) so that a
		// redirect after one copy cannot disturb the next one.

		const sources = new Map<string, FolderRef>();

		for (const user of SITE_CMS_USER_NAMES) {
			for (const section of sections) {
				const destinations = destinationsByType[section.type];

				for (let index = 0; index < destinations.length; index++) {
					sources.set(
						`${user}|${section.label}|${index}`,
						await apiHelpers.objectFolder.createObjectEntryFolder({
							parentObjectEntryFolderExternalReferenceCode:
								section.parentExternalReferenceCode,
							scopeKey: spaceName,
							title: `Folder ${getRandomString()}`,
						})
					);
				}
			}
		}

		for (const user of SITE_CMS_USER_NAMES) {
			await performUserSwitchViaApi(page, user);

			const canManage = SITE_CMS_USER_EDIT_NAMES.includes(user);

			for (const section of sections) {
				const destinations = destinationsByType[section.type];

				if (canManage) {
					for (let index = 0; index < destinations.length; index++) {
						const destination = destinations[index];
						const source = sources.get(
							`${user}|${section.label}|${index}`
						);

						await test.step(`${user} can copy a folder in ${section.label} to ${destination.space}`, async () => {
							await relocateFolderWithRetry(
								assetsPage,
								page,
								section,
								spaceName,
								{
									action: 'Copy',
									destination,
									itemTitle: source.title,
								}
							);
						});
					}
				}
				else {
					const source = sources.get(`${user}|${section.label}|0`);

					await test.step(`${user} cannot copy a folder in ${section.label}`, async () => {
						await gotoTableListing(assetsPage, section, spaceName);

						await expectRowActionHidden(page, source.title, 'Copy');
					});
				}
			}
		}
	}
);
