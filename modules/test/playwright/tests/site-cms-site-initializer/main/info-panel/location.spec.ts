/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {cmsPagesTest} from '../fixtures/cmsPagesTest';

const test = mergeTests(
	cmsPagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPD-17564': {enabled: true},
	}),
	loginTest()
);

const SECTIONS = [
	{
		expectedLabels: {
			en_US: 'Files',
			es_ES: 'Archivos',
		},
		externalReferenceCode: 'L_FILES',
	},
	{
		expectedLabels: {
			en_US: 'Contents',
			es_ES: 'Contenidos',
		},
		externalReferenceCode: 'L_CONTENTS',
	},
];

for (const {expectedLabels, externalReferenceCode} of SECTIONS) {
	test(
		`Info panel location reads localized label for ${externalReferenceCode}`,
		{tag: '@LPD-90001'},
		async ({apiHelpers}) => {
			const rootFolder = await apiHelpers.get(
				`/o/headless-object/v1.0/scopes/Default/object-entry-folder/by-external-reference-code/${externalReferenceCode}`
			);

			expect(rootFolder.label_i18n).toMatchObject(expectedLabels);
		}
	);
}
