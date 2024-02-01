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
			<ClayModal.Header>Article in Edition</ClayModal.Header>

			<ClayModal.Body>
				<p>
					This article is currently being edited by another user.
					Actions such as Edit, Expire, Move, and Delete are
					temporarily unavailable. If you need to take control over
					this article, contact your admin.
				</p>
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group>
						<ClayButton
							displayType="primary"
							onClick={onClose}
						>
							{Liferay.Language.get('ok')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</ClayModal>
	);
}
