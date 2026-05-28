/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ApiHelpers} from '../helpers/ApiHelpers';
import {userData} from './performLogin';

export async function addCMSAdministrator(
	apiHelpers: ApiHelpers,
	userAccount?: TUserAccount
): Promise<TUserAccount> {
	const user =
		await apiHelpers.headlessAdminUser.postUserAccount(userAccount);

	if (!userAccount) {
		userData[user.alternateName] = {
			name: user.givenName,
			password: 'test',
			surname: user.familyName,
		};
	}

	const cmsAdminRole =
		await apiHelpers.headlessAdminUser.getRoleByName('CMS Administrator');

	await apiHelpers.headlessAdminUser.postRoleUserAccountAssociation(
		cmsAdminRole.id,
		Number(user.id)
	);

	return user;
}
