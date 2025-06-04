/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLink from '@clayui/link';
import React from 'react';

export default function SpaceAbstractHeader({
	viewAllLabel,
	viewAllURL,
}: {
	viewAllLabel: string;
	viewAllURL: string;
}) {
	return (
		<div>
			<div>
				<h2>{Liferay.Language.get('content')}</h2>
			</div>

			<div>
				<ClayLink displayType="secondary" href={viewAllURL}>
					{viewAllLabel}
				</ClayLink>
			</div>
		</div>
	);
}
