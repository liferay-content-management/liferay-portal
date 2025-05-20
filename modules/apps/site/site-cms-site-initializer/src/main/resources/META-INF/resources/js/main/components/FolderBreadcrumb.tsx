/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayBreadcrumb from '@clayui/breadcrumb';
import ClayToolbar from '@clayui/toolbar';
import React from 'react';

import SpaceSticker from './SpaceSticker';

interface Props {
	breadcrumbItems: BreadcrumbItem[];
}

interface BreadcrumbItem {
	active?: boolean;
	href?: string;
	label: string;
	onClick?: () => void;
}

export default function FolderBreadcrumb({breadcrumbItems}: Props) {
	return (
		<ClayToolbar aria-label={breadcrumbItems[0]?.label} light>
			<div className="container-fluid">
				<ClayToolbar.Nav>
					<div className="mr-1 mt-3">
						<SpaceSticker
							name={breadcrumbItems[0]?.label}
							showName={false}
							size="sm"
						/>
					</div>

					<ClayBreadcrumb items={breadcrumbItems} />
				</ClayToolbar.Nav>
			</div>
		</ClayToolbar>
	);
}
