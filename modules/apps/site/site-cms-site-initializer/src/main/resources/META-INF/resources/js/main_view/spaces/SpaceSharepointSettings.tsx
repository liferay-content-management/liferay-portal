/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {useFormik} from 'formik';
import {openToast} from 'frontend-js-components-web';
import {fetch, navigate} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

import {FieldText} from '../../common/components/forms';

interface SpaceSharepointSettingsProps {
	backURL?: string;
	groupId: string;
}

interface SharepointSettings {
	clientId: string;
	clientSecret: {set: boolean};
	folderUrl: string;
	tenantId: string;
}

async function getSharepointSettings(
	groupId: string
): Promise<SharepointSettings> {
	const response = await fetch(
		`/c/portal/object_storage_sharepoint_get_space_settings?groupId=${groupId}`
	);

	return response.json();
}

async function saveSharepointSettings({
	clientId,
	clientSecret,
	folderUrl,
	groupId,
	tenantId,
}: {
	clientId: string;
	clientSecret: string;
	folderUrl: string;
	groupId: string;
	tenantId: string;
}) {
	const formData = new FormData();

	formData.set('clientId', clientId);
	formData.set('clientSecret', clientSecret);
	formData.set('folderUrl', folderUrl);
	formData.set('groupId', groupId);
	formData.set('tenantId', tenantId);

	return fetch('/c/portal/object_storage_sharepoint_save_space_settings', {
		body: formData,
		method: 'POST',
	});
}

export default function SpaceSharepointSettings({
	backURL,
	groupId,
}: SpaceSharepointSettingsProps) {
	const [secretIsSet, setSecretIsSet] = useState(false);

	const {handleChange, handleSubmit, setValues, submitForm, values} =
		useFormik({
			initialValues: {
				clientId: '',
				clientSecret: '',
				folderUrl: '',
				tenantId: '',
			},
			onSubmit: async (values) => {
				const response = await saveSharepointSettings({
					...values,
					groupId,
				});

				if (response.ok) {
					openToast({
						message: Liferay.Language.get(
							'sharepoint-space-settings-saved'
						),
						type: 'success',
					});

					if (values.clientSecret) {
						setSecretIsSet(true);
					}

					setValues({...values, clientSecret: ''});
				}
				else {
					openToast({
						message: Liferay.Language.get(
							'an-unexpected-error-occurred'
						),
						type: 'danger',
					});
				}
			},
		});

	useEffect(() => {
		getSharepointSettings(groupId)
			.then((settings) => {
				setSecretIsSet(settings.clientSecret.set);

				setValues({
					clientId: settings.clientId ?? '',
					clientSecret: '',
					folderUrl: settings.folderUrl ?? '',
					tenantId: settings.tenantId ?? '',
				});
			})
			.catch(() => {
				openToast({
					message: Liferay.Language.get(
						'an-unexpected-error-occurred'
					),
					type: 'danger',
				});
			});
	}, [groupId, setValues]);

	const onCancel = () => {
		if (backURL) {
			navigate(backURL);
		}
		else {
			window.history.back();
		}
	};

	return (
		<form
			className="container-fluid container-fluid-max-md p-0 p-md-4"
			onSubmit={handleSubmit}
		>
			<FieldText
				label={Liferay.Language.get('client-id')}
				name="clientId"
				onChange={handleChange}
				required
				value={values.clientId}
			/>

			<FieldText
				helpMessage={
					secretIsSet
						? Liferay.Language.get(
								'leave-blank-to-keep-existing-value'
							)
						: undefined
				}
				label={Liferay.Language.get('client-secret')}
				name="clientSecret"
				onChange={handleChange}
				placeholder={secretIsSet ? '••••••••' : undefined}
				type="text"
				value={values.clientSecret}
			/>

			<FieldText
				label={Liferay.Language.get('tenant-id')}
				name="tenantId"
				onChange={handleChange}
				required
				value={values.tenantId}
			/>

			<FieldText
				label={Liferay.Language.get('folder-url')}
				name="folderUrl"
				onChange={handleChange}
				value={values.folderUrl}
			/>

			<ClayButton.Group className="mt-2" spaced>
				<ClayButton onClick={() => submitForm()}>
					{Liferay.Language.get('save')}
				</ClayButton>

				<ClayButton displayType="secondary" onClick={onCancel}>
					{Liferay.Language.get('cancel')}
				</ClayButton>
			</ClayButton.Group>
		</form>
	);
}
