/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import selectCanUpdateItemAdvancedConfiguration from '../../../../src/main/resources/META-INF/resources/page_editor/app/selectors/selectCanUpdateItemAdvancedConfiguration';

describe('selectCanUpdateItemAdvancedConfiguration', () => {
	it('returns true when the user has update permission', () => {
		const canUpdateItemAdvancedConfiguration =
			selectCanUpdateItemAdvancedConfiguration({
				permissions: {UPDATE: true},
			});

		expect(canUpdateItemAdvancedConfiguration).toBe(true);
	});

	it('returns false when the page template is read only', () => {
		const canUpdateItemAdvancedConfiguration =
			selectCanUpdateItemAdvancedConfiguration({
				permissions: {READ_ONLY: true, UPDATE: true},
			});

		expect(canUpdateItemAdvancedConfiguration).toBe(false);
	});
});
