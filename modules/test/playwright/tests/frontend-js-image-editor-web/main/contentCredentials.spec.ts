/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect} from '@playwright/test';

import {checkAccessibility} from '../../../utils/checkAccessibility';
import {imageEditorSamplePageTest as test} from './fixtures/imageEditorSamplePageTest';
import addContentCredentials from './utils/addContentCredentials';

const DISCLAIMER =
	'The image editor is not intended for use with AI generated content.';

async function serveSampleImageWithManifest(page: Page, sourceType: string) {
	await page.route(
		'**/frontend-js-image-editor-sample-web/images/sample.jpg',
		async (route) => {
			const response = await route.fetch();

			const body = await response.body();

			await route.fulfill({
				body: addContentCredentials(body, sourceType),
				response,
			});
		}
	);
}

test(
	'An AI generated image opens with the disclaimer above the editor',
	{tag: '@LPD-117106'},
	async ({imageEditorSamplePage, page}) => {
		await serveSampleImageWithManifest(page, 'trainedAlgorithmicMedia');

		const imageEditor = await imageEditorSamplePage.openEditor();

		const disclaimer = imageEditor.root.locator('.editor-ai-disclaimer');

		await expect(disclaimer).toContainText(DISCLAIMER);
		await expect(disclaimer).not.toHaveAttribute('role');
		await expect(
			imageEditor.root.locator(':scope > :first-child')
		).toHaveClass(/editor-ai-disclaimer/);
		await expect(page.locator('.editor-announcer')).toContainText(
			DISCLAIMER
		);

		await checkAccessibility({
			page,
			selectors: ['.image-editor'],
		});
	}
);

test(
	'An image whose credentials do not claim AI opens without the disclaimer',
	{tag: '@LPD-117106'},
	async ({imageEditorSamplePage, page}) => {
		await serveSampleImageWithManifest(page, 'digitalCapture');

		const imageEditor = await imageEditorSamplePage.openEditor();

		await expect(page.locator('.editor-announcer')).not.toBeEmpty();

		await expect(
			imageEditor.root.locator('.editor-ai-disclaimer')
		).toHaveCount(0);
		await expect(page.locator('.editor-announcer')).not.toContainText(
			DISCLAIMER
		);
	}
);
