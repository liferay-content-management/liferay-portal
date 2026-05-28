/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {ApiHelpers} from '../../../../helpers/ApiHelpers';
import {userData} from '../../../../utils/performLogin';
import {cmsPagesTest} from '../../../site-cms-site-initializer/main/fixtures/cmsPagesTest';
import {SITE_CMS_SPACE_EXTERNAL_REFERENCE_CODE} from '../constants/space';

export const test = mergeTests(
	cmsPagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPD-11235': {enabled: false},
		'LPD-17564': {enabled: true},
		'LPD-34594': {enabled: true},
	}),
	loginTest()
);

test('Teardown: Delete objects for Site CMS tests', async ({backendPage}) => {
	const apiHelpers = new ApiHelpers(backendPage);

	await apiHelpers.headlessAssetLibrary.deleteAssetLibrary(
		SITE_CMS_SPACE_EXTERNAL_REFERENCE_CODE
	);

	await apiHelpers.headlessAdminUser.deleteUserAccountByExternalReferenceCode(
		userData['cms.admin'].externalReferenceCode
	);

	await apiHelpers.headlessAdminUser.deleteUserAccountByExternalReferenceCode(
		userData['cms.space.admin'].externalReferenceCode
	);

	await apiHelpers.headlessAdminUser.deleteUserAccountByExternalReferenceCode(
		userData['cms.space.content.reviewer'].externalReferenceCode
	);

	await apiHelpers.headlessAdminUser.deleteUserAccountByExternalReferenceCode(
		userData['cms.space.member'].externalReferenceCode
	);
});
