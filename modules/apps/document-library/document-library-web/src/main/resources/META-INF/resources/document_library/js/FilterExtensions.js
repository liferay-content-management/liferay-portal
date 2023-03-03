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

import {SelectFileExtension} from '@liferay/content-dashboard-web';
import React from 'react';

const testProps = {
	fileExtensionGroups: [
		{
			fileExtensions: [
				{
					fileExtension: 'mpga',
					selected: false,
				},
				{
					fileExtension: 'mp3',
					selected: false,
				},
				{
					fileExtension: 'mp2',
					selected: false,
				},
				{
					fileExtension: 'ogg',
					selected: false,
				},
				{
					fileExtension: 'wav',
					selected: false,
				},
			],
			icon: 'document-multimedia',
			label: 'Audio',
		},
		{
			fileExtensions: [
				{
					fileExtension: 'gif',
					selected: false,
				},
				{
					fileExtension: 'jpg',
					selected: false,
				},
				{
					fileExtension: 'jpeg',
					selected: false,
				},
				{
					fileExtension: 'png',
					selected: false,
				},
			],
			icon: 'document-image',
			label: 'Image',
		},
	],
};

export default function ({itemSelectorSaveEvent, portletNamespace}) {
	return (
		<SelectFileExtension
			fileExtensionGroups={testProps.fileExtensionGroups}
			itemSelectorSaveEvent={itemSelectorSaveEvent}
			portletNamespace={portletNamespace}
		/>
	);
}
