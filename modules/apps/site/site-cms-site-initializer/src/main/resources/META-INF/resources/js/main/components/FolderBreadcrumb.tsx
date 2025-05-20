/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayBreadcrumb from '@clayui/breadcrumb';
import {ClayButtonWithIcon} from '@clayui/button';
import {ClayDropDownWithItems} from '@clayui/drop-down';
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

const FolderBreadcrumb = ({breadcrumbItems}: Props) => {
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

					<ClayToolbar.Item>
						<ClayDropDownWithItems
							items={[
								{
									label: Liferay.Language.get('order-by'),
									type: 'group',
								},
							]}
							trigger={
								<ClayButtonWithIcon
									aria-label={Liferay.Language.get(
										'more-actions'
									)}
									displayType="unstyled"
									size="xs"
									symbol="ellipsis-v"
								/>
							}
						/>
					</ClayToolbar.Item>
				</ClayToolbar.Nav>
			</div>
		</ClayToolbar>
	);
};

export default FolderBreadcrumb;
