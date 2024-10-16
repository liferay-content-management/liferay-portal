/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {clickAndExpectToBeVisible} from '../../../utils/clickAndExpectToBeVisible';
import fillAndClickOutside from '../../../utils/fillAndClickOutside';
import getRandomString from '../../../utils/getRandomString';
import {openFieldset} from '../../../utils/openFieldset';
import {JournalTemplatesPage} from './JournalTemplatesPage';

export class JournalEditTemplatePage {
	readonly page: Page;

	readonly basicInformation: Locator;
	readonly elementsButton: Locator;
	readonly journalTemplatesPage: JournalTemplatesPage;
	readonly propertiesTab: Locator;
	readonly selectStructureButton: Locator;
	readonly titleInput: Locator;

	constructor(page: Page) {
		this.page = page;

		this.basicInformation = page.getByRole('button', {
			name: 'Basic Information',
		});
		this.elementsButton = page.getByTitle('Elements', {exact: true});
		this.journalTemplatesPage = new JournalTemplatesPage(page);
		this.propertiesTab = page.getByRole('heading', {name: 'Properties'});
		this.selectStructureButton = page.getByRole('button', {
			name: 'Select Structure',
		});
		this.titleInput = page.locator(
			'#_com_liferay_journal_web_portlet_JournalPortlet_name'
		);
	}

	async goto(siteUrl?: Site['friendlyUrlPath']) {
		await this.journalTemplatesPage.goToCreateNewTemplate(siteUrl);

		// Do it twice so we decrease flakiness

		await this.journalTemplatesPage.goToCreateNewTemplate(siteUrl);

		await this.basicInformation.waitFor();
	}

	async gotoElements() {
		await this.elementsButton.click();
	}

	async fillTitle(title: string) {
		await this.propertiesTab.waitFor();
		await fillAndClickOutside(this.page, this.titleInput, title);
	}

	async createTemplate({
		fields = [],
		siteUrl,
		structureName,
		title,
	}: {
		fields?: string[];
		siteUrl?: Site['friendlyUrlPath'];
		structureName?: string;
		title?: string;
	} = {}) {
		await this.goto(siteUrl);
		await this.fillTitle(title || getRandomString());
		if (structureName) {
			await this.selectStructure(structureName);
		}
		if (fields) {
			await this.elementsButton.click();
			for (const field of fields) {
				await this.page.getByRole('button', {name: field}).click();
			}
		}

		await this.page
			.getByRole('button', {name: 'Save', exact: true})
			.click();
	}
	async selectStructure(structureName: string) {
		await openFieldset(this.page, 'Basic Information');

		await this.selectStructureButton.click();
		await this.page
			.frameLocator('iframe[title="Select Structure"]')
			.getByRole('cell', {name: structureName})
			.click();
	}
}
