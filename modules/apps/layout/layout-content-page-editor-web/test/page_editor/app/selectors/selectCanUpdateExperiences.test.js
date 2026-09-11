/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import selectCanUpdateExperiences from '../../../../src/main/resources/META-INF/resources/page_editor/app/selectors/selectCanUpdateExperiences';

describe('selectCanUpdateExperiences', () => {
	it('returns true when the user has update permission', () => {
		const canUpdateExperiences = selectCanUpdateExperiences({
			permissions: {UPDATE: true},
		});

		expect(canUpdateExperiences).toBe(true);
	});

	it('returns false when the page template is read only', () => {
		const canUpdateExperiences = selectCanUpdateExperiences({
			permissions: {READ_ONLY: true, UPDATE: true},
		});

		expect(canUpdateExperiences).toBe(false);
	});
});
