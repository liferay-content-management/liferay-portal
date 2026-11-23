/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {readContentCredentials} from '../../src/main/resources/META-INF/resources/js/content-credentials';

function ascii(text: string): Uint8Array {
	return Uint8Array.from(text, (character) => character.charCodeAt(0));
}

function concat(...parts: Uint8Array[]): Uint8Array {
	const bytes = new Uint8Array(
		parts.reduce((total, part) => total + part.length, 0)
	);

	parts.reduce((offset, part) => {
		bytes.set(part, offset);

		return offset + part.length;
	}, 0);

	return bytes;
}

function uint32(value: number, littleEndian = false): Uint8Array {
	const bytes = new Uint8Array(4);

	new DataView(bytes.buffer).setUint32(0, value, littleEndian);

	return bytes;
}

function manifestStore(sourceType: string): Uint8Array {
	return ascii(
		`....jumb....c2pa....http://cv.iptc.org/newscodes/digitalsourcetype/${sourceType}`
	);
}

function jpeg(...packets: Uint8Array[]): Uint8Array {
	const segment = (marker: number, data: Uint8Array) =>
		concat(
			new Uint8Array([
				0xff,
				marker,
				(data.length + 2) >> 8,
				(data.length + 2) & 0xff,
			]),
			data
		);

	return concat(
		new Uint8Array([0xff, 0xd8]),
		segment(0xe0, ascii('JFIF\0')),
		...packets.map((data, index) =>
			segment(
				0xeb,
				concat(
					ascii('JP'),
					new Uint8Array([0, 1]),
					uint32(index + 1),
					uint32(data.length + 8),
					ascii('jumb'),
					data
				)
			)
		),
		new Uint8Array([0xff, 0xda, 0, 2])
	);
}

function png(store?: Uint8Array): Uint8Array {
	const chunk = (type: string, data: Uint8Array) =>
		concat(uint32(data.length), ascii(type), data, uint32(0));

	return concat(
		new Uint8Array([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
		chunk('IHDR', new Uint8Array(13)),
		...(store ? [chunk('caBX', store)] : []),
		chunk('IEND', new Uint8Array())
	);
}

function webp(store?: Uint8Array): Uint8Array {
	const chunk = (type: string, data: Uint8Array) =>
		concat(ascii(type), uint32(data.length, true), data);

	const body = concat(
		ascii('WEBP'),
		chunk('VP8L', new Uint8Array(6)),
		...(store ? [chunk('C2PA', store)] : [])
	);

	return concat(ascii('RIFF'), uint32(body.length, true), body);
}

describe('readContentCredentials', () => {
	it.each([
		['JPEG', jpeg(manifestStore('trainedAlgorithmicMedia'))],
		['PNG', png(manifestStore('trainedAlgorithmicMedia'))],
		['WebP', webp(manifestStore('trainedAlgorithmicMedia'))],
	])('finds an AI generated manifest in a %s', (_, bytes) => {
		expect(readContentCredentials(bytes)).toEqual({aiGenerated: true});
	});

	it('treats an image edited with AI as AI generated', () => {
		expect(
			readContentCredentials(
				png(manifestStore('compositeWithTrainedAlgorithmicMedia'))
			)
		).toEqual({aiGenerated: true});
	});

	it('reports credentials that do not claim AI', () => {
		expect(
			readContentCredentials(png(manifestStore('digitalCapture')))
		).toEqual({aiGenerated: false});
	});

	it('rejoins a JPEG manifest split through the source type', () => {
		const store = manifestStore('trainedAlgorithmicMedia');

		const middle = store.length - 12;

		expect(
			readContentCredentials(
				jpeg(store.subarray(0, middle), store.subarray(middle))
			)
		).toEqual({aiGenerated: true});
	});

	it.each([
		['JPEG', jpeg()],
		['PNG', png()],
		['WebP', webp()],
		['GIF', ascii('GIF89a')],
	])('finds nothing in a %s without a manifest', (_, bytes) => {
		expect(readContentCredentials(bytes)).toBeNull();
	});
});
