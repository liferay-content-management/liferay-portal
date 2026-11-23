/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render, screen, waitFor} from '@testing-library/react';
import {fetch} from 'frontend-js-web';
import React from 'react';

import '@testing-library/jest-dom';

import ImageEditor from '../src/main/resources/META-INF/resources/js/ImageEditor';

jest.mock('cropperjs', () => jest.fn());

jest.mock('frontend-js-web', () => ({
	fetch: jest.fn(),
}));

const DISCLAIMER =
	'the-image-editor-is-not-intended-for-use-with-ai-generated-content';

function jpegWithContentCredentials(sourceType) {
	const store = Array.from(
		`jumb c2pa http://cv.iptc.org/newscodes/digitalsourcetype/${sourceType}`,
		(character) => character.charCodeAt(0)
	);

	const length = store.length + 14;

	return new Uint8Array([
		0xff,
		0xd8,
		0xff,
		0xeb,
		length >> 8,
		length & 0xff,
		0x4a,
		0x50,
		0,
		1,
		0,
		0,
		0,
		1,
		0,
		0,
		0,
		store.length + 4,
		...store,
		0xff,
		0xda,
	]);
}

function renderImageEditor() {
	return render(
		<ImageEditor
			imageId="1"
			imageSrc="/documents/image.jpg"
			saveURL="/save"
		/>
	);
}

beforeEach(() => {
	URL.createObjectURL = jest.fn(() => 'blob:image');
	URL.revokeObjectURL = jest.fn();
});

describe('the AI content disclaimer', () => {
	it('opens above the image when the image claims to be AI generated', async () => {
		fetch.mockResolvedValue({
			arrayBuffer: () =>
				Promise.resolve(
					jpegWithContentCredentials('trainedAlgorithmicMedia').buffer
				),
		});

		const {container} = renderImageEditor();

		const alert = await screen.findByRole('alert');

		expect(alert).toHaveTextContent(DISCLAIMER);
		expect(alert.closest('.image-editor-ai-disclaimer')).toBe(
			container.querySelector('.image-editor-content').firstElementChild
		);
		expect(container.querySelector('img')).toHaveAttribute(
			'src',
			'blob:image'
		);
	});

	it('stays out of the way for credentials that do not claim AI', async () => {
		fetch.mockResolvedValue({
			arrayBuffer: () =>
				Promise.resolve(
					jpegWithContentCredentials('digitalCapture').buffer
				),
		});

		const {container} = renderImageEditor();

		await waitFor(() =>
			expect(container.querySelector('img')).toHaveAttribute(
				'src',
				'blob:image'
			)
		);

		expect(screen.queryByRole('alert')).toBeNull();
	});

	it('falls back to the original image when the bytes cannot be read', async () => {
		fetch.mockRejectedValue(new Error('offline'));

		const {container} = renderImageEditor();

		await waitFor(() =>
			expect(container.querySelector('img')).toHaveAttribute(
				'src',
				'/documents/image.jpg'
			)
		);

		expect(screen.queryByRole('alert')).toBeNull();
	});

	it('releases the image bytes when it closes', async () => {
		fetch.mockResolvedValue({
			arrayBuffer: () =>
				Promise.resolve(
					jpegWithContentCredentials('digitalCapture').buffer
				),
		});

		const {container, unmount} = renderImageEditor();

		await waitFor(() =>
			expect(container.querySelector('img')).toHaveAttribute(
				'src',
				'blob:image'
			)
		);

		unmount();

		expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:image');
	});
});
