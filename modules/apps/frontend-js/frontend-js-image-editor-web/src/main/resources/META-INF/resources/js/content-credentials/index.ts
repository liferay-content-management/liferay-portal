/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const AI_SOURCE_TYPES = [
	'compositeWithTrainedAlgorithmicMedia',
	'trainedAlgorithmicMedia',
];

export interface ContentCredentials {
	aiGenerated: boolean;
}

export function readContentCredentials(
	bytes: Uint8Array
): ContentCredentials | null {
	const store = getManifestStore(bytes);

	if (!store) {
		return null;
	}

	const text = new TextDecoder('latin1').decode(store);

	if (!text.includes('c2pa')) {
		return null;
	}

	return {
		aiGenerated: AI_SOURCE_TYPES.some((sourceType) =>
			text.includes(`digitalsourcetype/${sourceType}`)
		),
	};
}

function getFourCC(bytes: Uint8Array, offset: number): string {
	return String.fromCharCode(...bytes.subarray(offset, offset + 4));
}

function getJPEGManifestStore(bytes: Uint8Array): Uint8Array | null {
	const parts: Uint8Array[] = [];

	for (let offset = 2; offset + 4 <= bytes.length; ) {
		if (bytes[offset] !== 0xff || bytes[offset + 1] === 0xda) {
			break;
		}

		const length = (bytes[offset + 2] << 8) | bytes[offset + 3];

		const segment = bytes.subarray(offset + 4, offset + 2 + length);

		if (
			bytes[offset + 1] === 0xeb &&
			segment[0] === 0x4a &&
			segment[1] === 0x50 &&
			segment.length > 16
		) {
			const dataView = new DataView(
				segment.buffer,
				segment.byteOffset,
				segment.byteLength
			);

			const boxHeaderLength = dataView.getUint32(8) === 1 ? 16 : 8;

			parts.push(
				segment.subarray(
					dataView.getUint32(4) > 1 ? 8 + boxHeaderLength : 8
				)
			);
		}

		offset += 2 + length;
	}

	if (!parts.length) {
		return null;
	}

	const store = new Uint8Array(
		parts.reduce((total, part) => total + part.length, 0)
	);

	parts.reduce((offset, part) => {
		store.set(part, offset);

		return offset + part.length;
	}, 0);

	return store;
}

function getManifestStore(bytes: Uint8Array): Uint8Array | null {
	if (bytes[0] === 0x89 && getFourCC(bytes, 1).startsWith('PNG')) {
		return getPNGManifestStore(bytes);
	}

	if (bytes[0] === 0xff && bytes[1] === 0xd8) {
		return getJPEGManifestStore(bytes);
	}

	if (getFourCC(bytes, 0) === 'RIFF' && getFourCC(bytes, 8) === 'WEBP') {
		return getWebPManifestStore(bytes);
	}

	return null;
}

function getPNGManifestStore(bytes: Uint8Array): Uint8Array | null {
	const dataView = new DataView(
		bytes.buffer,
		bytes.byteOffset,
		bytes.byteLength
	);

	for (let offset = 8; offset + 8 <= bytes.length; ) {
		const length = dataView.getUint32(offset);

		if (getFourCC(bytes, offset + 4) === 'caBX') {
			return bytes.subarray(offset + 8, offset + 8 + length);
		}

		offset += 12 + length;
	}

	return null;
}

function getWebPManifestStore(bytes: Uint8Array): Uint8Array | null {
	const dataView = new DataView(
		bytes.buffer,
		bytes.byteOffset,
		bytes.byteLength
	);

	for (let offset = 12; offset + 8 <= bytes.length; ) {
		const length = dataView.getUint32(offset + 4, true);

		if (getFourCC(bytes, offset) === 'C2PA') {
			return bytes.subarray(offset + 8, offset + 8 + length);
		}

		offset += 8 + length + (length % 2);
	}

	return null;
}
