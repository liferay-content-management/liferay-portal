/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import CMSFileUploaderComponent from '../src/main/resources/META-INF/resources/item_selector_file_uploader/CMSFileUploaderComponent';

const DEFAULT_PROPS = {
	files: [],
	onCloseUploadView: jest.fn(),
};

describe('CMSFileUploaderComponent', () => {
	afterEach(() => {
		jest.clearAllMocks();
	});

	it('asks for the space when no group is given', () => {
		render(<CMSFileUploaderComponent {...DEFAULT_PROPS} />);

		expect(screen.getByLabelText(/space/)).toBeInTheDocument();

		expect(
			screen.getByText('select-the-space-to-upload-the-file')
		).toBeInTheDocument();
	});

	it('does not ask for the space when a group is given', () => {
		render(<CMSFileUploaderComponent {...DEFAULT_PROPS} groupId={123} />);

		expect(screen.queryByLabelText(/space/)).not.toBeInTheDocument();
	});

	it('shows the server error when the upload fails', async () => {
		Liferay.Util.fetch = jest.fn().mockResolvedValue({
			json: () =>
				Promise.resolve({
					status: 'BAD_REQUEST',
					title: 'Unable to store the file',
				}),
			ok: false,
		});
		Liferay.Util.formatStorage = jest.fn();

		const user = userEvent.setup();

		render(
			<CMSFileUploaderComponent
				{...DEFAULT_PROPS}
				files={[
					{
						file: new File(['a'], 'file.txt', {type: 'text/plain'}),
						name: 'file.txt',
						size: 1,
					},
				]}
				groupId={123}
			/>
		);

		await user.click(await screen.findByRole('button', {name: /^upload-/}));

		expect(
			await screen.findByText('Unable to store the file')
		).toBeInTheDocument();
		expect(DEFAULT_PROPS.onCloseUploadView).not.toHaveBeenCalled();
	});
});
