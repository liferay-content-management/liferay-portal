/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import selectCanSwitchEditMode from '../../../../src/main/resources/META-INF/resources/page_editor/app/selectors/selectCanSwitchEditMode';

describe('selectCanSwitchEditMode', () => {
	it('returns true when the user has permission to switch edit mode', () => {
		const canSwitchEditMode = selectCanSwitchEditMode({
			permissions: {SWITCH_EDIT_MODE: true},
		});

		expect(canSwitchEditMode).toBe(true);
	});

	it('returns false when the page template is read only', () => {
		const canSwitchEditMode = selectCanSwitchEditMode({
			permissions: {READ_ONLY: true, SWITCH_EDIT_MODE: true},
		});

		expect(canSwitchEditMode).toBe(false);
	});
});
