/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export default function addContentCredentials(
	jpeg: Buffer,
	sourceType: string
): Buffer {
	const store = Buffer.from(
		`jumb c2pa http://cv.iptc.org/newscodes/digitalsourcetype/${sourceType}`,
		'latin1'
	);

	const header = Buffer.alloc(12);

	header.write('JP', 0, 'ascii');
	header.writeUInt16BE(1, 2);
	header.writeUInt32BE(1, 4);
	header.writeUInt32BE(store.length + 4, 8);

	const marker = Buffer.from([0xff, 0xeb, 0, 0]);

	marker.writeUInt16BE(header.length + store.length + 2, 2);

	return Buffer.concat([
		jpeg.subarray(0, 2),
		marker,
		header,
		store,
		jpeg.subarray(2),
	]);
}
