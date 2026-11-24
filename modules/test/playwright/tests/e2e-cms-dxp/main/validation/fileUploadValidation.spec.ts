/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {clickAndExpectToBeVisible} from '../../../../utils/clickAndExpectToBeVisible';
import createTempFile from '../../../../utils/createTempFile';
import getRandomString from '../../../../utils/getRandomString';
import {cmsPagesTest} from '../../../site-cms-site-initializer/main/fixtures/cmsPagesTest';
import {structureBuilderPagesTest} from '../../../site-cms-site-initializer/structure-builder/fixtures/structureBuilderPagesTest';

const test = mergeTests(
	cmsPagesTest,
	dataApiHelpersTest,
	loginTest(),
	structureBuilderPagesTest
);

const CMS_MAXIMUM_FILE_SIZE_MB = 71;

const MAXIMUM_FILE_SIZE_MB = 1;

test(
	'Uploading a file whose extension the file structure does not accept is blocked',
	{tag: ['@LPD-95548', '@LPD-95548/TC-23.d']},
	async ({
		apiHelpers,
		assetsPage,
		contentsPage,
		page,
		structureBuilderPage,
	}) => {
		const space = await apiHelpers.headlessAssetLibrary.createSpace();

		const structureLabel = `Structure${getRandomString()}`;

		await structureBuilderPage.createStructureFromData({
			label: structureLabel,
			page: structureBuilderPage,
			publish: false,
			spaces: [space.name],
			type: 'file',
		});

		await structureBuilderPage.selectFields([{label: 'File'}]);

		await structureBuilderPage.changeFieldSettings({
			acceptedFileExtensions: 'png',
		});

		await structureBuilderPage.publishStructure();

		const title = `File ${getRandomString()}`;

		await test.step('Upload a text file into a structure that accepts only png', async () => {
			await assetsPage.gotoFiles();

			await contentsPage.createContent(structureLabel, space.name);

			await page.locator('input[type="file"]').setInputFiles({
				buffer: Buffer.from('This is not an image.'),
				mimeType: 'text/plain',
				name: `${title}.txt`,
			});

			await expect(page.getByText(`${title}.txt`)).toBeVisible();

			await page
				.getByRole('textbox', {exact: true, name: 'Title'})
				.fill(title);

			await contentsPage.publishButton.click();
		});

		await test.step('An extension error is shown and the file is not saved', async () => {
			await expect(
				page
					.getByText(
						'Please enter a file with a valid extension (png).'
					)
					.first()
			).toBeVisible();

			await expect(
				page.getByRole('heading', {name: 'New'})
			).toBeVisible();
		});
	}
);

test(
	'Uploading a file larger than the maximum size the file structure allows is blocked',
	{tag: ['@LPD-95548', '@LPD-95548/TC-23.e']},
	async ({
		apiHelpers,
		assetsPage,
		contentsPage,
		page,
		structureBuilderPage,
	}) => {
		const space = await apiHelpers.headlessAssetLibrary.createSpace();

		const structureLabel = `Structure${getRandomString()}`;

		await structureBuilderPage.createStructureFromData({
			label: structureLabel,
			page: structureBuilderPage,
			publish: false,
			spaces: [space.name],
			type: 'file',
		});

		await structureBuilderPage.selectFields([{label: 'File'}]);

		await structureBuilderPage.changeFieldSettings({
			acceptedFileExtensions: 'png',
			maximumFileSize: MAXIMUM_FILE_SIZE_MB,
		});

		await structureBuilderPage.publishStructure();

		const title = `File ${getRandomString()}`;

		await test.step('Upload a png larger than the maximum size', async () => {
			await assetsPage.gotoFiles();

			await contentsPage.createContent(structureLabel, space.name);

			await page.locator('input[type="file"]').setInputFiles({
				buffer: Buffer.alloc((MAXIMUM_FILE_SIZE_MB + 1) * 1024 * 1024),
				mimeType: 'image/png',
				name: `${title}.png`,
			});
		});

		await test.step('A size error naming the limit is shown and the file is never attached', async () => {
			await expect(
				page
					.getByText(
						`Please enter a file with a valid file size no larger than ${MAXIMUM_FILE_SIZE_MB} MB.`
					)
					.first()
			).toBeVisible();

			await expect(page.getByText(`${title}.png`)).toBeHidden();
		});

		await test.step('The file entry cannot be published', async () => {
			await page
				.getByRole('textbox', {exact: true, name: 'Title'})
				.fill(title);

			await contentsPage.publishButton.click();

			await expect(
				page.getByRole('heading', {name: 'New'})
			).toBeVisible();
		});
	}
);

test(
	'Uploading a file larger than a basic document can store from the file selector of an upload field is blocked',
	{tag: '@LPD-107933'},
	async ({apiHelpers, contentsPage, page, structureBuilderPage}) => {
		const space = await apiHelpers.headlessAssetLibrary.createSpace();

		const structureLabel = getRandomString();

		await structureBuilderPage.createStructureFromData({
			label: structureLabel,
			page: structureBuilderPage,
			publish: false,
			spaces: [space.name],
		});

		await structureBuilderPage.addField('Upload');

		await structureBuilderPage.changeFieldSettings({
			requestFile: 'document-library',
		});

		await structureBuilderPage.publishStructure();

		const dialog = page.getByRole('dialog');
		const fileName = `${getRandomString()}.png`;

		await test.step('The field help shows the size a basic document can store', async () => {
			await contentsPage.goto();

			await contentsPage.createContent(structureLabel, space.name);

			await expect(
				page.getByText(`no larger than ${CMS_MAXIMUM_FILE_SIZE_MB} MB.`)
			).toBeVisible();
		});

		await test.step('Upload a png larger than that size from the file selector', async () => {
			const uploadFilesButton = dialog.getByRole('button', {
				name: 'Upload Files',
			});

			await clickAndExpectToBeVisible({
				target: uploadFilesButton,
				trigger: page.getByRole('button', {
					exact: true,
					name: 'Select File',
				}),
			});

			await uploadFilesButton.click();

			await dialog
				.locator('input[type="file"]')
				.setInputFiles(
					createTempFile(
						fileName,
						Buffer.alloc(
							(CMS_MAXIMUM_FILE_SIZE_MB + 1) * 1024 * 1024
						)
					)
				);
		});

		await test.step('A size error naming the limit is shown and the file is not listed', async () => {
			await expect(
				dialog.getByText(
					`Please enter a file with a valid file size no larger than ${CMS_MAXIMUM_FILE_SIZE_MB.toFixed(1)}MB.`
				)
			).toBeVisible();

			await expect(dialog.getByText(fileName)).toBeHidden();
		});
	}
);
