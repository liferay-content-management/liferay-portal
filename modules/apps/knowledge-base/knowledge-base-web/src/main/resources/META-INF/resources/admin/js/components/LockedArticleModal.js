/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayModal, {useModal} from '@clayui/modal';
import React from 'react';

export default function LockedArticleModal() {
	const {observer, onClose} = useModal();

	return (
		<ClayModal observer={observer} size="md" status="info">
			<ClayModal.Header>
				{Liferay.Language.get('article-in-edition')}
			</ClayModal.Header>

			<ClayModal.Body>
				<p>{Liferay.Language.get('article-in-edition-description')}</p>
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group>
						<ClayButton displayType="primary" onClick={onClose}>
							{Liferay.Language.get('ok')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</ClayModal>
	);
}
