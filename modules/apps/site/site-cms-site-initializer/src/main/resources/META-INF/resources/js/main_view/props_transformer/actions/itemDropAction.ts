/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';
import {sub} from 'frontend-js-web';

import ApiHelper from '../../../common/services/ApiHelper';
import FolderService from '../../../common/services/FolderService';
import {OBJECT_ENTRY_FOLDER_CLASS_NAME} from '../../../common/utils/constants';
import {displayErrorToast} from '../../../common/utils/toastUtil';

export default function itemDropAction(draggedItem: any, dropTarget: any) {
	if (!draggedItem || !dropTarget) {
		return;
	}

	const isFolder =
		draggedItem.entryClassName === OBJECT_ENTRY_FOLDER_CLASS_NAME;

	const targetFolderId = dropTarget.embedded?.id;

	if (!targetFolderId) {
		return;
	}

	const draggedId = draggedItem.embedded?.id;

	if (isFolder && draggedId === targetFolderId) {
		return;
	}

	const draggedParentId = draggedItem.embedded?.parentObjectEntryFolderId;

	if (draggedParentId && draggedParentId === targetFolderId) {
		return;
	}

	const draggedTitle = Liferay.Util.escapeHTML(
		draggedItem.embedded?.title ?? draggedItem.title ?? ''
	);
	const targetTitle = Liferay.Util.escapeHTML(
		dropTarget.embedded?.title ?? ''
	);

	openToast({
		message: sub(
			Liferay.Language.get('moving-x-to-x'),
			draggedTitle,
			`<strong>${targetTitle}</strong>`
		),
		type: 'info',
	});

	let promise: Promise<any>;

	if (isFolder) {
		promise = FolderService.moveFolder(draggedId, targetFolderId);
	}
	else {
		const moveHref = draggedItem.actions?.move?.href;

		if (!moveHref) {
			displayErrorToast();

			return;
		}

		promise = ApiHelper.post(
			moveHref.replace('{objectEntryFolderId}', String(targetFolderId))
		);
	}

	promise.then((result: any) => {
		if (result?.error) {
			displayErrorToast(
				typeof result.error === 'string' ? result.error : undefined
			);

			return;
		}

		openToast({
			message: sub(
				Liferay.Language.get('x-was-successfully-moved-to-x'),
				draggedTitle,
				`<strong>${targetTitle}</strong>`
			),
			type: 'success',
		});

		window.location.reload();
	});
}
