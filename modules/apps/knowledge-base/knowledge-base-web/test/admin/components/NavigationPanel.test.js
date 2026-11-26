/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render, screen} from '@testing-library/react';
import React from 'react';

import '@testing-library/jest-dom';

import NavigationPanel from '../../../src/main/resources/META-INF/resources/js/admin/components/NavigationPanel';

const DEFAULT_PROPS = {
	items: [
		{
			children: [
				{
					classNameId: '2',
					href: 'article_url',
					id: '2',
					name: 'Article',
					type: 'KBArticle',
				},
			],
			classNameId: '1',
			href: 'home_url',
			id: '1',
			name: 'Home',
			type: 'KBFolder',
		},
	],
	moveKBObjectURL: 'move_url',
	portletNamespace: '_portletNamespace_',
	selectedItemId: '1',
};

describe('NavigationPanel', () => {
	it('highlights the selected item without the Clay active style', () => {
		render(<NavigationPanel {...DEFAULT_PROPS} />);

		const treeItem = screen.getByRole('treeitem', {name: 'Home'});

		expect(treeItem).toHaveClass('knowledge-base-navigation-item-active');
		expect(treeItem).not.toHaveClass('active');
	});
});
