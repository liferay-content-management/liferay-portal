/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

function formatBytes(bytes: number): string {
	const units = ['B', 'KB', 'MB', 'GB', 'TB'];
	let size = bytes;
	let unitIndex = 0;

	while (size >= 1024 && unitIndex < units.length - 1) {
		size /= 1024;
		unitIndex++;
	}

	if (unitIndex === 0) {
		return `${size} ${units[unitIndex]}`;
	}

	return `${size.toFixed(1)} ${units[unitIndex]}`;
}

const SharepointSizeRenderer = ({
	itemData,
	value,
}: {
	itemData?: {type?: string};
	value?: number;
}) => {
	if (itemData?.type === 'folder' || typeof value !== 'number') {
		return <span className="text-secondary">--</span>;
	}

	return <span>{formatBytes(value)}</span>;
};

export default SharepointSizeRenderer;
