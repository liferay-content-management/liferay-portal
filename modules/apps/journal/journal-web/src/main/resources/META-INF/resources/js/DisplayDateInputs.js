/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayInput} from '@clayui/form';
import {dateUtils} from 'frontend-js-web';
import React, {useCallback, useEffect, useState} from 'react';

function getDate(value) {
	const date = new Date(value);

	if (dateUtils.isValid(date)) {
		return {
			day: date.getDate(),
			hour: date.getHours(),
			minutes: date.getMinutes(),
			month: date.getMonth(),
			year: date.getFullYear(),
		};
	}

	return {day: '', hour: '', minutes: '', month: '', year: ''};
}

export default function DisplayDateInputs({
	displayDate: defaultDisplayDate,
	portletNamespace,
	timeZone,
}) {
	const formId = `${portletNamespace}fm1`;

	const [displayDate, setDisplayDate] = useState(() => {
		const currentDate = new Date(
			new Date().toLocaleString('en-US', {timeZone})
		);

		return defaultDisplayDate || currentDate;
	});

	const updateDisplayDateToCurrent = useCallback(() => {
		const currentDate = new Date(
			new Date().toLocaleString('en-US', {timeZone})
		);
		setDisplayDate(currentDate);
	}, [timeZone]);

	useEffect(() => {
		Liferay.on('displayDate:updateToCurrent', updateDisplayDateToCurrent);

		return () => {
			Liferay.detach(
				'displayDate:updateToCurrent',
				updateDisplayDateToCurrent
			);
		};
	}, [updateDisplayDateToCurrent]);

	const {day, hour, minutes, month, year} = getDate(displayDate);

	return (
		<>
			<ClayInput
				form={formId}
				name={`${portletNamespace}displayDateDay`}
				type="hidden"
				value={day}
			/>

			<ClayInput
				form={formId}
				name={`${portletNamespace}displayDateHour`}
				type="hidden"
				value={hour}
			/>

			<ClayInput
				form={formId}
				name={`${portletNamespace}displayDateMinute`}
				type="hidden"
				value={minutes}
			/>

			<ClayInput
				form={formId}
				name={`${portletNamespace}displayDateMonth`}
				type="hidden"
				value={month}
			/>

			<ClayInput
				form={formId}
				name={`${portletNamespace}displayDateYear`}
				type="hidden"
				value={year}
			/>
		</>
	);
}
