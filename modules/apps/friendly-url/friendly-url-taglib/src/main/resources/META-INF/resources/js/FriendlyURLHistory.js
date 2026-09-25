/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayButtonWithIcon} from '@clayui/button';
import {useModal} from '@clayui/modal';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import React, {useState} from 'react';

import FriendlyURLHistoryModal from './FriendlyURLHistoryModal';

export default function FriendlyURLHistory({
	disabled = false,
	elementId,
	localizable = false,
	...restProps
}) {
	const [showModal, setShowModal] = useState(false);
	const [selectedLanguageId, setSelectedLanguageId] = useState();

	const handleOnClose = () => {
		setShowModal(false);
	};

	const {observer, onClose} = useModal({
		onClose: handleOnClose,
	});

	return (
		<>
			<ClayButtonWithIcon
				aria-label={Liferay.Language.get('history')}
				borderless
				className={classNames('btn-url-history', {
					['btn-url-history-localizable']: localizable,
				})}
				disabled={disabled}
				displayType="secondary"
				onClick={() => {
					if (localizable) {
						setSelectedLanguageId(
							Liferay.component(elementId).getSelectedLanguageId()
						);
					}
					setShowModal(true);
				}}
				outline
				small
				symbol="time"
				title={Liferay.Language.get('history')}
			/>
			{showModal && (
				<FriendlyURLHistoryModal
					{...restProps}
					elementId={elementId}
					initialLanguageId={selectedLanguageId}
					localizable={localizable}
					observer={observer}
					onModalClose={onClose}
				/>
			)}
		</>
	);
}

FriendlyURLHistory.propTypes = {
	disabled: PropTypes.bool,
	elementId: PropTypes.string.isRequired,
	localizable: PropTypes.bool,
};
