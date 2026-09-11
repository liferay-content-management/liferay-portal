/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import selectCanUpdateCSSAdvancedOptions from '../../../../src/main/resources/META-INF/resources/page_editor/app/selectors/selectCanUpdateCSSAdvancedOptions';

describe('selectCanUpdateCSSAdvancedOptions', () => {
	it('returns true when the user has update permission', () => {
		const canUpdateCSSAdvancedOptions = selectCanUpdateCSSAdvancedOptions({
			permissions: {UPDATE: true},
		});

		expect(canUpdateCSSAdvancedOptions).toBe(true);
	});

	it('returns false when the page template is read only', () => {
		const canUpdateCSSAdvancedOptions = selectCanUpdateCSSAdvancedOptions({
			permissions: {READ_ONLY: true, UPDATE: true},
		});

		expect(canUpdateCSSAdvancedOptions).toBe(false);
	});
});
