/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {IInternalRenderer} from '@liferay/frontend-data-set-web';

import SharepointNameRenderer from './cell_renderers/SharepointNameRenderer';
import SharepointSizeRenderer from './cell_renderers/SharepointSizeRenderer';

export default function SharepointEntriesFDSPropsTransformer({
	...otherProps
}: any) {
	return {
		...otherProps,
		customRenderers: {
			tableCell: [
				{
					component: SharepointNameRenderer,
					name: 'sharepointNameRenderer',
					type: 'internal',
				} as IInternalRenderer,
				{
					component: SharepointSizeRenderer,
					name: 'sharepointSizeRenderer',
					type: 'internal',
				} as IInternalRenderer,
			],
		},
	};
}
