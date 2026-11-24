/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import AttachmentBase from '../../js/Attachment/AttachmentBase';

const mockItemSelectorModal = jest.fn();

jest.mock('@liferay/frontend-js-item-selector-web', () => ({
	CMSFileUploaderComponent: () => null,
	ItemSelectorModal: (props: any) => {
		mockItemSelectorModal(props);

		return null;
	},
	getCMSItemSelectorFilters: jest.fn().mockReturnValue([]),
	getCMSItemSelectorGroupedFilters: jest.fn().mockReturnValue([]),
}));

jest.mock('data-engine-js-components-web', () => ({
	useConfig: () => ({portletNamespace: 'portletNamespace'}),
}));

jest.mock('frontend-js-web', () => ({
	...jest.requireActual('frontend-js-web'),
	fetch: jest.fn().mockResolvedValue({json: () => Promise.resolve({})}),
}));

describe('The AttachmentBase component', () => {
	afterEach(() => {
		jest.clearAllMocks();
	});

	it('passes the maximum file size to the CMS files selector', async () => {
		const user = userEvent.setup();

		render(
			<AttachmentBase
				acceptedFileExtensions="pdf"
				attachment={null}
				error={{}}
				fileSource="CMSBasicDocument"
				handleDelete={() => {}}
				maximumFileSize={75000000}
				onAttachmentChange={() => {}}
				overallMaximumUploadRequestSize={104857600}
				readOnly={false}
				setError={() => {}}
				tip=""
				url=""
				value=""
			/>
		);

		await user.click(screen.getByRole('button', {name: 'select-file'}));

		expect(mockItemSelectorModal).toHaveBeenLastCalledWith(
			expect.objectContaining({maxFileSize: 75000000, open: true})
		);
	});
});
