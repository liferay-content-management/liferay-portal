/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import {TreeView as ClayTreeView} from '@clayui/core';
import ClayEmptyState from '@clayui/empty-state';
import classnames from 'classnames';
import {navigate} from 'frontend-js-web';
import PropTypes from 'prop-types';
import React from 'react';

import ActionsDropdown from './ActionsDropdown';

export default function TemplatesPanel({items, selectedItemId}) {
	const handleClickItem = (event, item) => {
		if (event.defaultPrevented) {
			return;
		}

		event.stopPropagation();
		event.preventDefault();

		navigate(item.href);
	};

	return items?.length ? (
		<ClayTreeView
			defaultItems={items}
			defaultSelectedKeys={new Set([selectedItemId])}
			nestedKey="children"
			showExpanderOnHover={false}
		>
			{(item) => {
				return (
					<ClayTreeView.Item
						actions={ActionsDropdown({actions: item.actions})}
						onClick={(event) => {
							handleClickItem(event, item);
						}}
					>
						<ClayTreeView.ItemStack
							className={classnames({
								'knowledge-base-navigation-item-active':
									item.id === selectedItemId,
							})}
						>
							{item.name}
						</ClayTreeView.ItemStack>
					</ClayTreeView.Item>
				);
			}}
		</ClayTreeView>
	) : (
		<ClayEmptyState
			description=""
			imgSrc={`${themeDisplay.getPathThemeImages()}/states/empty_state.gif`}
			small
			title={Liferay.Language.get('there-are-no-article-templates')}
		/>
	);
}

TemplatesPanel.propTypes = {
	items: PropTypes.arrayOf(
		PropTypes.shape({
			href: PropTypes.string.isRequired,
			name: PropTypes.string.isRequired,
			id: PropTypes.string.isRequired,
		})
	),
	selectedItemId: PropTypes.string,
};
