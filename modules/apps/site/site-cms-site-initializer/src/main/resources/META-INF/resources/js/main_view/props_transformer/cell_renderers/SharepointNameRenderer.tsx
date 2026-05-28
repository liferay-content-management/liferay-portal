/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

import {AssetIcon, MimeTypes} from '../../../common/components/AssetIcon';

function getMimeType({
	mimeType,
	type,
}: {
	mimeType?: string;
	type?: string;
}): MimeTypes {
	if (type === 'folder') {
		return MimeTypes.Folder;
	}

	const normalizedMimeType = mimeType || '';

	if (normalizedMimeType.startsWith('image/')) {
		return MimeTypes.DocumentImage;
	}

	if (
		normalizedMimeType.startsWith('audio/') ||
		normalizedMimeType.startsWith('video/')
	) {
		return MimeTypes.DocumentMultimedia;
	}

	if (
		normalizedMimeType === 'application/pdf' ||
		normalizedMimeType.startsWith('text/')
	) {
		return MimeTypes.DocumentText;
	}

	return MimeTypes.DocumentDefault;
}

const SharepointNameRenderer = ({
	itemData,
	value,
}: {
	itemData?: {mimeType?: string; type?: string; webUrl?: string};
	value?: string;
}) => {
	if (!itemData) {
		return <span>{value}</span>;
	}

	return (
		<span className="align-items-center d-flex">
			<AssetIcon mimeType={getMimeType(itemData)} />

			<span className="ms-3">
				{itemData.webUrl ? (
					<a
						href={itemData.webUrl}
						rel="noopener noreferrer"
						target="_blank"
					>
						{value}
					</a>
				) : (
					value
				)}
			</span>
		</span>
	);
};

export default SharepointNameRenderer;
