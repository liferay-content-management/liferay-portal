/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayEmptyState from '@clayui/empty-state';
import React from 'react';

export default function EmptyState({
	description = Liferay.Language.get(
		'click-new-to-create-your-first-content'
	),
	title = Liferay.Language.get('no-assets-yet'),
}) {
	return (
		<ClayEmptyState
			description={description}
			imgSrc={`${themeDisplay.getPathThemeImages()}/states/cms_empty_state.svg`}
			title={title}
		/>
	);
}
