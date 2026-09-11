/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import selectCanSetCustomValue from '../../../../src/main/resources/META-INF/resources/page_editor/app/selectors/selectCanSetCustomValue';

describe('selectCanSetCustomValue', () => {
	it('returns true when the user has update permission', () => {
		const canSetCustomValue = selectCanSetCustomValue({
			permissions: {UPDATE: true},
		});

		expect(canSetCustomValue).toBe(true);
	});

	it('returns false when the page template is read only', () => {
		const canSetCustomValue = selectCanSetCustomValue({
			permissions: {READ_ONLY: true, UPDATE: true},
		});

		expect(canSetCustomValue).toBe(false);
	});
});
