/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import ItemSelectorRepositoryEntryBrowser from '../../src/main/resources/META-INF/resources/js/repository_entry_browser/ItemSelectorRepositoryEntryBrowser';

describe('ItemSelectorRepositoryEntryBrowser', () => {
	let defaultPrevented;
	let openerFire;
	let rootNode;

	const handleClick = (event) => {
		defaultPrevented = event.defaultPrevented;
	};

	beforeEach(() => {
		defaultPrevented = undefined;
		openerFire = jest.fn();

		Liferay.Util.getOpener = () => ({Liferay: {fire: openerFire}});

		rootNode = document.createElement('div');

		rootNode.id = 'repositoryEntryBrowser';
		rootNode.innerHTML = `
			<a
				class="item-preview"
				data-returntype="FileEntryItemSelectorReturnType"
				data-value="selectedValue"
				href="/documents/image.svg"
			>
				image.svg
			</a>
		`;

		document.body.appendChild(rootNode);

		window.addEventListener('click', handleClick);
	});

	afterEach(() => {
		window.removeEventListener('click', handleClick);

		delete Liferay.Util.getOpener;

		rootNode.remove();
	});

	it('selects the item without following its link', async () => {
		render(
			<ItemSelectorRepositoryEntryBrowser
				folderId="0"
				itemSelectedEventName="itemSelected"
				portletNamespace="_namespace_"
				rootNode="#repositoryEntryBrowser"
				uploaderEnabled={false}
			/>
		);

		await userEvent.click(screen.getByText('image.svg'));

		expect(defaultPrevented).toBe(true);
		expect(openerFire).toHaveBeenCalledWith('itemSelected', {
			folderId: '0',
			returnType: 'FileEntryItemSelectorReturnType',
			value: 'selectedValue',
		});
	});
});
