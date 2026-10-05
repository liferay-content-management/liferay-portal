/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const EVENT_TYPES = ['click', 'focusin'];

export function alignFieldsOnFocus(
	iframe: HTMLIFrameElement,
	getIframeToScroll: () => HTMLIFrameElement | null
) {
	const iframeDocument = iframe.contentDocument;

	if (!iframeDocument) {
		return;
	}

	const alignOnFieldActivation = (event: Event) => {
		const field = (event.target as Element)
			.closest('.form-control')
			?.closest<HTMLElement>('[data-field-name]');

		if (field) {
			scrollToMatchField(field, getIframeToScroll());
		}
	};

	EVENT_TYPES.forEach((type) =>
		iframeDocument.addEventListener(type, alignOnFieldActivation)
	);

	return () =>
		EVENT_TYPES.forEach((type) =>
			iframeDocument.removeEventListener(type, alignOnFieldActivation)
		);
}

function getScrollContainer(element: HTMLElement): HTMLElement | Window {
	const window = element.ownerDocument.defaultView as Window;

	for (
		let parent = element.parentElement;
		parent;
		parent = parent.parentElement
	) {
		const {overflowY} = window.getComputedStyle(parent);

		if (
			(overflowY === 'auto' || overflowY === 'scroll') &&
			parent.scrollHeight > parent.clientHeight
		) {
			return parent;
		}
	}

	return window;
}

function scrollToMatchField(
	field: HTMLElement,
	iframeToScroll: HTMLIFrameElement | null
) {
	const selector = `[data-field-name="${CSS.escape(
		field.dataset.fieldName ?? ''
	)}"]`;

	const index = Array.from(
		field.ownerDocument.querySelectorAll(selector)
	).indexOf(field);

	const fieldToScroll =
		iframeToScroll?.contentDocument?.querySelectorAll<HTMLElement>(
			selector
		)[index];

	if (!iframeToScroll || !fieldToScroll) {
		return;
	}

	getScrollContainer(fieldToScroll).scrollBy({
		top:
			fieldToScroll.getBoundingClientRect().top -
			field.getBoundingClientRect().top,
	});
}
