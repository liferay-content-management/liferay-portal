/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {ClayDropDownWithItems} from '@clayui/drop-down';
import ClayEmptyState from '@clayui/empty-state';
import React from 'react';

import './../css/empty_state.scss';

interface IEmptyStateProps {
	description?: string;
	title?: string;
}

const EmptyState: React.FC<IEmptyStateProps> = ({
	description = Liferay.Language.get(
		'click-new-to-create-your-first-content'
	),
	title = Liferay.Language.get('no-assets-yet'),
}) => {
	return (
		<>
			<ClayEmptyState
				description={description}
				imgSrc={`${Liferay.ThemeDisplay.getPathThemeImages()}/states/cms_empty_state.svg`}
				title={title}
			/>

			<ClayDropDownWithItems
				className="empty-state-new-dropdown"
				items={[]}
				trigger={
					<ClayButton
						aria-label={Liferay.Language.get('new')}
						displayType="primary"
					>
						{Liferay.Language.get('new')}
					</ClayButton>
				}
				triggerIcon="caret-bottom"
			/>
		</>
	);
};

export default EmptyState;
