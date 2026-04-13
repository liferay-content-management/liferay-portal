/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openConfirmModal} from 'frontend-js-components-web';
import {debounce, fetch, navigate, sub} from 'frontend-js-web';

import {LocaleChangedHandler} from './LocaleChangedHandler.es';
import removeAlert from './removeAlert';
import showAlert from './showAlert';

const AUTO_SAVE_DELAY = 1500;
const NON_CONTENT_FORM_FIELD_SUFFIXES = [
	'articleId',
	'formDate',
	'activePage',
	'availableLocales',
	'jakarta-portlet-action',
	'jakarta.portlet.action',
	'javax-portlet-action',
	'javax.portlet.action',
	'languageId',
	'persistDefaultValues',
	'version',
	'workflowAction',
];

export default function _JournalPortlet({
	articleId: initialArticleId,
	autoSaveDraftEnabled,
	autoSaveDraftURL,
	availableLocales: initialAvailableLocales,
	classNameId,
	contentTitle,
	defaultLanguageId: initialDefaultLanguageId,
	hasSavePermission,
	namespace,
}) {
	const formId = `${namespace}fm1`;

	const actionInput = document.getElementById(
		`${namespace}jakarta-portlet-action`
	);
	const availableLocalesInput = document.getElementById(
		`${namespace}availableLocales`
	);
	const contextualSidebarButton = document.getElementById(
		`${namespace}contextualSidebarButton`
	);
	const contextualSidebarContainer = document.getElementById(
		`${namespace}contextualSidebarContainer`
	);
	const form = document.getElementById(formId);
	const formDateInput = document.getElementById(`${namespace}formDate`);
	const publishButton = document.getElementById(`${namespace}publishButton`);
	const resetValuesButton = document.getElementById(
		`${namespace}resetValuesButton`
	);
	const saveButton = document.getElementById(`${namespace}saveButton`);

	const availableLocales = [
		...initialAvailableLocales,
		initialDefaultLanguageId,
	];

	availableLocalesInput.value = availableLocales;

	let articleId = initialArticleId;
	let defaultLanguageId = initialDefaultLanguageId;
	let selectedLanguageId = initialDefaultLanguageId;
	let formSubmitted = false;

	const lockHolder = {};

	Liferay.componentReady(`${namespace}publishing`).then((lock) => {
		lockHolder.lock = lock;
	});

	const editingDefaultValues = classNameId && classNameId !== '0';

	if (editingDefaultValues) {
		actionInput.value = articleId
			? '/journal/update_data_engine_default_values'
			: '/journal/add_data_engine_default_values';
	}

	const handleContextualSidebarButton = () => {
		contextualSidebarContainer?.classList.toggle(
			'contextual-sidebar-visible'
		);
	};

	const isContextualSidebarOpen = () =>
		contextualSidebarContainer.classList.contains(
			'contextual-sidebar-visible'
		);

	const updateContextualSidebarAriaAttributes = () => {
		const isOpen = isContextualSidebarOpen();

		const title = isOpen
			? Liferay.Language.get('close-configuration-panel')
			: Liferay.Language.get('open-configuration-panel');

		contextualSidebarButton.setAttribute('aria-label', title);
		contextualSidebarButton.setAttribute('aria-selected', isOpen);
		contextualSidebarButton.setAttribute('title', title);
	};

	const ignoredFormFieldNames = new Set(
		NON_CONTENT_FORM_FIELD_SUFFIXES.map((suffix) => `${namespace}${suffix}`)
	);
	const getFormValuesSignature = () =>
		serializeFormValues(form, ignoredFormFieldNames);

	let hasUnsavedChanges = false;
	let hasUserInteractedWithForm = false;
	let inFlightFormValuesSignature = null;
	let lastSavedFormValuesSignature = '';

	const initializeLastSavedFormValuesSignature = () => {
		if (!lastSavedFormValuesSignature) {
			lastSavedFormValuesSignature = getFormValuesSignature();
		}
	};

	const handleAutoSave = (formValuesSignature = getFormValuesSignature()) => {
		initializeLastSavedFormValuesSignature();

		if (formValuesSignature === lastSavedFormValuesSignature) {
			return;
		}

		lockHolder.lock?.lock();

		actionInput.value = articleId
			? '/journal/update_article'
			: '/journal/add_article';

		inFlightFormValuesSignature = formValuesSignature;

		handleDDMFormValid({
			redirectOnSave: false,
			showErrors: false,
		});
	};

	const handleContextualSidebarButtonClick = () => {
		handleContextualSidebarButton();

		updateContextualSidebarAriaAttributes();

		if (isContextualSidebarOpen()) {
			contextualSidebarContainer.focus({preventScroll: true});
		}
	};

	const handleDDMFormError = (event) => {
		if (event.error?.statusCode) {
			showAlert(event.error.message);
		}

		const workflowActionInput = document.getElementById(
			`${namespace}workflowAction`
		);

		workflowActionInput.value = Liferay.Workflow.ACTION_SAVE_DRAFT;

		const titleInputComponent = Liferay.component(
			`${namespace}titleMapAsXML`
		);

		if (!titleInputComponent) {
			return;
		}

		if (!titleInputComponent?.getValue(defaultLanguageId)) {
			showAlert(
				sub(
					Liferay.Language.get(
						'please-enter-a-valid-title-for-the-default-language-x'
					),
					defaultLanguageId.replaceAll('_', '-')
				)
			);

			validateRequiredDDMFields();
		}
	};

	const handleDDMFormValid = (
		{redirectOnSave, showErrors} = {
			redirectOnSave: false,
			showErrors: false,
		}
	) => {
		const titleInputComponent = Liferay.component(
			`${namespace}titleMapAsXML`
		);

		if (!titleInputComponent) {
			inFlightFormValuesSignature = null;

			return;
		}

		if (
			titleInputComponent?.getValue(defaultLanguageId) ||
			editingDefaultValues
		) {
			if (!articleId) {
				const newArticleIdInput = document.getElementById(
					`${namespace}newArticleId`
				);

				articleId = newArticleIdInput.value || '';
			}

			const articleIdInput = document.getElementById(
				`${namespace}articleId`
			);

			articleIdInput.value = articleId;

			availableLocalesInput.value = availableLocales;

			if (autoSaveDraftEnabled && !redirectOnSave) {
				if (showErrors) {
					Liferay.componentReady(
						`${namespace}dataEngineLayoutRenderer`
					)
						.then((dataEngineLayoutRenderer) => {
							const dataEngineLayoutRendererRef =
								dataEngineLayoutRenderer?.reactComponentRef;

							return dataEngineLayoutRendererRef.current.validate();
						})
						.then((validForm) => {
							if (validForm) {
								removeAlert();
								submitAsyncForm(form, {redirectOnSave});
							}
						});
				}
				else {
					submitAsyncForm(form, {redirectOnSave});
				}
			}
			else if (!formSubmitted) {
				formSubmitted = true;

				form.submit();
			}
		}
		else if (showErrors) {
			showAlert(
				sub(
					Liferay.Language.get(
						'please-enter-a-valid-title-for-the-default-language-x'
					),
					defaultLanguageId.replaceAll('_', '-')
				)
			);

			validateRequiredDDMFields();

			inFlightFormValuesSignature = null;
			lockHolder.lock?.unlock(true);
		}
		else {
			showAlert(
				Liferay.Language.get(
					'please-complete-all-mandatory-fields-to-enable-autosave'
				),
				Liferay.Language.get('info'),
				'info'
			);

			inFlightFormValuesSignature = null;
			lockHolder.lock?.unlock(true);
		}
	};

	const handlePublishButtonClick = (event) => {
		lockHolder.lock?.lock();

		if (Liferay.FeatureFlags['LPD-11228']) {
			return;
		}

		document
			.querySelectorAll('.journal-alert-container')
			.forEach((alertElement) => {
				alertElement.parentElement.removeChild(alertElement);
			});

		const workflowActionInput = document.getElementById(
			`${namespace}workflowAction`
		);

		if (event.currentTarget.dataset.actionname === 'publish') {
			workflowActionInput.value = Liferay.Workflow.ACTION_PUBLISH;
		}

		if (editingDefaultValues) {
			Liferay.component(`${namespace}dataEngineLayoutRenderer`)
				.reactComponentRef.current.getFields()
				.forEach((field) => {
					field.required = false;
				});
		}
		else {
			articleId = document.getElementById(`${namespace}articleId`).value;

			actionInput.value = articleId
				? '/journal/update_article'
				: '/journal/add_article';
		}

		const descriptionInputComponent = Liferay.component(
			`${namespace}descriptionMapAsXML`
		);
		const titleInputComponent = Liferay.component(
			`${namespace}titleMapAsXML`
		);

		[titleInputComponent, descriptionInputComponent].forEach(
			(inputComponent) => {
				const translatedLanguages = inputComponent.get(
					'translatedLanguages'
				);

				if (
					!translatedLanguages.has(selectedLanguageId) &&
					selectedLanguageId !== defaultLanguageId
				) {
					inputComponent.updateInput('');

					Liferay.Form.get(formId).removeRule(
						`${namespace}${inputComponent.get('id')}`,
						'required'
					);
				}
			}
		);

		lockHolder.lock?.unlock();
	};

	const handleResetValuesButtonClick = (event) => {
		lockHolder.lock?.lock();

		openConfirmModal({
			message: Liferay.Language.get(
				'are-you-sure-you-want-to-reset-the-default-values'
			),
			onConfirm: (isConfirmed) => {
				if (isConfirmed) {
					if (editingDefaultValues) {
						actionInput.value = articleId
							? '/journal/update_data_engine_default_values'
							: '/journal/add_data_engine_default_values';
					}

					submitForm(
						document.hrefFm,
						event.currentTarget.dataset.url
					);
				}
				else {
					lockHolder.lock?.unlock();
				}
			},
		});
	};

	const submitAsyncForm = (
		formElement,
		{redirectOnSave} = {redirectOnSave: false}
	) => {
		return fetch(autoSaveDraftURL, {
			body: new FormData(formElement),
			method: formElement.method,
		})
			.then((response) => {
				if (redirectOnSave) {
					navigate(
						response.redirected && response.url
							? response.url
							: window.location.href
					);
				}

				return response.json();
			})
			.then((data) => {
				if (data.success) {
					if (!articleId) {
						articleId = data.articleId;
						document.getElementById(`${namespace}articleId`).value =
							articleId;

						Liferay.fire('asyncFormSubmission', {articleId});

						const friendlyUrlInputComponent = Liferay.component(
							`${namespace}friendlyURL`
						);

						if (!friendlyUrlInputComponent.getValue()) {
							const friendlyURL = data.friendlyURL;
							friendlyUrlInputComponent.updateInputLanguage(
								friendlyURL,
								defaultLanguageId
							);
							friendlyUrlInputComponent.updateInput(friendlyURL);

							Liferay.fire('journal:update-friendly-url', {
								friendlyURL,
							});
						}
					}

					const articleIdWrapper = document.getElementById(
						`${namespace}articleIdWrapper`
					);
					const articleVersionInput = document.getElementById(
						`${namespace}version`
					);
					const articleVersionStatusWrapper = document.getElementById(
						`${namespace}articleVersionStatusWrapper`
					);
					const displayedArticleId = document.getElementById(
						`${namespace}displayedArticleId`
					);
					const displayedVersion = document.getElementById(
						`${namespace}displayedVersion`
					);
					const statusDraftLabel = document.getElementById(
						`${namespace}statusDraftLabel`
					);
					const statusLabel = document.getElementById(
						`${namespace}statusLabel`
					);

					if (statusLabel) {
						statusLabel.classList.add('hide');
					}

					articleVersionStatusWrapper.classList.remove('hide');
					statusDraftLabel.classList.remove('hide');

					articleVersionInput.value = data.version;
					displayedVersion.innerHTML = data.version;

					articleIdWrapper.classList.remove('hide');
					displayedArticleId.innerHTML = articleId;

					const savedFormValuesSignature =
						inFlightFormValuesSignature;

					formDateInput.value = data.modifiedDate;
					if (savedFormValuesSignature !== null) {
						lastSavedFormValuesSignature = savedFormValuesSignature;
					}

					inFlightFormValuesSignature = null;
					hasUnsavedChanges =
						savedFormValuesSignature !== null &&
						getFormValuesSignature() !==
							lastSavedFormValuesSignature;
					if (!hasUnsavedChanges) {
						hasUserInteractedWithForm = false;
					}
					lockHolder.lock?.unlock();
					removeAlert();

					if (hasUnsavedChanges) {
						hasUnsavedChanges = false;

						handleAutoSave();
					}
				}
				else {
					formDateInput.value = data.modifiedDate;
					inFlightFormValuesSignature = null;
					lockHolder.lock?.unlock(true);
					showAlert(
						Liferay.Language.get(
							'please-complete-all-mandatory-fields-to-enable-autosave'
						),
						Liferay.Language.get('info'),
						'info'
					);
				}
			})
			.catch((error) => {
				console.error(error);
				inFlightFormValuesSignature = null;
				lockHolder.lock?.unlock(true);
			});
	};

	const eventHandlers = [
		attachListener(
			contextualSidebarButton,
			'click',
			handleContextualSidebarButtonClick
		),
		attachListener(publishButton, 'click', handlePublishButtonClick),
		attachListener(saveButton, 'click', handlePublishButtonClick),
		attachListener(
			resetValuesButton,
			'click',
			handleResetValuesButtonClick
		),

		new LocaleChangedHandler({
			contentTitle,
			defaultLanguageId,
			namespace,
			onDefaultLocaleChangedCallback: (languageId) => {
				defaultLanguageId = languageId;
			},
			onLocaleChangedCallback: (_context, languageId) => {
				if (!availableLocales.includes(languageId)) {
					availableLocales.push(languageId);
					availableLocalesInput.value = availableLocales;
				}

				selectedLanguageId = languageId;
			},
		}),

		Liferay.on('ddmFormError', handleDDMFormError),
		Liferay.on('ddmFormValid', () =>
			handleDDMFormValid({
				redirectOnSave: true,
				showErrors: true,
			})
		),
	];

	const validateRequiredDDMFields = () => {
		Liferay.componentReady(`${namespace}dataEngineLayoutRenderer`).then(
			(dataEngineLayoutRenderer) => {
				const dataEngineLayoutRendererRef =
					dataEngineLayoutRenderer?.reactComponentRef;

				return dataEngineLayoutRendererRef.current.validate();
			}
		);
	};

	if (
		autoSaveDraftEnabled &&
		hasSavePermission &&
		(!classNameId || classNameId === '0')
	) {
		eventHandlers.push(
			attachFormChangeListener({
				acceptMutationRecord: (mutationRecord) => {
					return [
						mutationRecord.target,
						...mutationRecord.addedNodes,
						...mutationRecord.removedNodes,
					].some(
						(node) =>
							node.name &&
							node.name.startsWith(namespace) &&
							node.name !== `${namespace}languageId`
					);
				},
				callback: ({
					eventSourceType,
					hadChildListMutation,
					hadUserInteraction,
				} = {}) => {
					if (
						hadUserInteraction ||
						eventSourceType === 'change' ||
						eventSourceType === 'input'
					) {
						hasUserInteractedWithForm = true;
					}

					initializeLastSavedFormValuesSignature();

					const currentFormValuesSignature = getFormValuesSignature();
					const baselineFormValuesSignature =
						inFlightFormValuesSignature ??
						lastSavedFormValuesSignature;

					if (lockHolder.lock?.isLocked()) {
						hasUnsavedChanges =
							currentFormValuesSignature !==
							(inFlightFormValuesSignature ??
								lastSavedFormValuesSignature);

						return;
					}

					if (
						eventSourceType === 'mutation' &&
						hadChildListMutation &&
						!hasUserInteractedWithForm &&
						isFieldMaterializationMutation(
							baselineFormValuesSignature,
							currentFormValuesSignature
						)
					) {
						lastSavedFormValuesSignature =
							currentFormValuesSignature;

						return;
					}

					if (
						currentFormValuesSignature ===
						lastSavedFormValuesSignature
					) {
						return;
					}

					handleAutoSave(currentFormValuesSignature);
				},
				form,
				namespace,
				onReady: initializeLastSavedFormValuesSignature,
			})
		);
	}

	if (window.innerWidth > Liferay.BREAKPOINTS.PHONE) {
		handleContextualSidebarButton();
	}

	updateContextualSidebarAriaAttributes();

	return {
		dispose() {
			eventHandlers.forEach((eventHandler) => {
				eventHandler.detach();
			});
		},
	};
}

function attachFormChangeListener({
	acceptMutationRecord,
	callback,
	form,
	namespace,
	onReady,
}) {
	let hadChildListMutationSinceLastCallback = false;
	let hadUserInteractionSinceLastCallback = false;

	const handleChange = debounce((eventSourceType) => {
		callback({
			eventSourceType,
			hadChildListMutation: hadChildListMutationSinceLastCallback,
			hadUserInteraction: hadUserInteractionSinceLastCallback,
		});

		hadChildListMutationSinceLastCallback = false;
		hadUserInteractionSinceLastCallback = false;
	}, AUTO_SAVE_DELAY);
	const handleFormChange = () => {
		hadUserInteractionSinceLastCallback = true;
		handleChange('change');
	};
	const handleInput = () => {
		hadUserInteractionSinceLastCallback = true;
		handleChange('input');
	};

	const mutationObserver = new MutationObserver((mutationRecords) => {
		const observedMutationRecords = mutationRecords
			.filter((mutationRecord) => {
				if (mutationRecord.target.id === `${namespace}formDate`) {
					return;
				}
				else if (mutationRecord.type === 'attributes') {
					return (
						mutationRecord.oldValue !== null &&
						mutationRecord.target.value.trim() !==
							mutationRecord.oldValue.trim()
					);
				}
				else if (mutationRecord.type === 'childList') {
					return [
						...mutationRecord.addedNodes,
						...mutationRecord.removedNodes,
					].some((node) => node.name);
				}
			})
			.filter((mutationRecord) => acceptMutationRecord(mutationRecord));

		if (observedMutationRecords.length) {
			hadChildListMutationSinceLastCallback =
				hadChildListMutationSinceLastCallback ||
				observedMutationRecords.some(
					(mutationRecord) => mutationRecord.type === 'childList'
				);

			handleChange('mutation');
		}
	});

	Liferay.componentReady(`${namespace}SelectAssetDisplayPage`).then(() => {
		onReady?.();

		mutationObserver.observe(form, {
			attributeFilter: ['value'],
			attributeOldValue: true,
			attributes: true,
			childList: true,
			subtree: true,
		});

		form.addEventListener('change', handleFormChange);
		form.addEventListener('input', handleInput);
	});

	return {
		detach() {
			mutationObserver.disconnect();
			form.removeEventListener('change', handleFormChange);
			form.removeEventListener('input', handleInput);
		},
	};
}

export function serializeFormValues(form, ignoredFieldNames = new Set()) {
	const serializedFormValues = [];
	const formDataEntries = Array.from(new FormData(form).entries());
	const localizedFieldBaseNames = getLocalizedFieldBaseNames(formDataEntries);

	formDataEntries.forEach(([name, value]) => {
		const normalizedFieldName = normalizeTrackedFormFieldName(name);

		if (
			name.endsWith('_edited') ||
			normalizedFieldName.endsWith('_edited') ||
			localizedFieldBaseNames.has(name) ||
			localizedFieldBaseNames.has(normalizedFieldName) ||
			ignoredFieldNames.has(name) ||
			ignoredFieldNames.has(normalizedFieldName)
		) {
			return;
		}

		const normalizedValue =
			typeof File !== 'undefined' && value instanceof File
				? `${value.name}:${value.size}:${value.type}:${value.lastModified}`
				: normalizeTrackedFormFieldValue(value);

		if (normalizedValue === '') {
			return;
		}

		serializedFormValues.push([normalizedFieldName, normalizedValue]);
	});

	serializedFormValues.sort(([fieldNameA, valueA], [fieldNameB, valueB]) => {
		const comparedFieldNames = fieldNameA.localeCompare(fieldNameB);

		if (comparedFieldNames) {
			return comparedFieldNames;
		}

		return valueA.localeCompare(valueB);
	});

	return JSON.stringify(serializedFormValues);
}

function getLocalizedFieldBaseNames(formDataEntries) {
	const localizedFieldBaseNames = new Set();

	formDataEntries.forEach(([name]) => {
		const matchedName = name.match(/^(.*)_[a-z]{2}_[A-Z]{2}(?:_.*)?$/);

		if (matchedName) {
			localizedFieldBaseNames.add(matchedName[1]);
		}
	});

	return localizedFieldBaseNames;
}

function isFieldMaterializationMutation(
	previousSignature = '',
	nextSignature = ''
) {
	const previousFieldValueMap = getSignatureFieldValueMap(previousSignature);
	const nextFieldValueMap = getSignatureFieldValueMap(nextSignature);
	const trackedFieldNames = new Set([
		...previousFieldValueMap.keys(),
		...nextFieldValueMap.keys(),
	]);
	let hasChangedFields = false;

	for (const fieldName of trackedFieldNames) {
		const previousValues = previousFieldValueMap.get(fieldName) || [];
		const nextValues = nextFieldValueMap.get(fieldName) || [];

		if (
			previousValues.length !== nextValues.length ||
			previousValues.some((value, index) => value !== nextValues[index])
		) {
			hasChangedFields = true;

			if (previousValues.length) {
				return false;
			}
		}
	}

	return hasChangedFields;
}

function getSignatureFieldValueMap(signature = '') {
	const signatureFieldValueMap = new Map();

	if (!signature) {
		return signatureFieldValueMap;
	}

	try {
		const parsedSignature = JSON.parse(signature);

		if (!Array.isArray(parsedSignature)) {
			return signatureFieldValueMap;
		}

		parsedSignature.forEach((entry) => {
			if (!Array.isArray(entry) || entry.length !== 2) {
				return;
			}

			const [fieldName, fieldValue] = entry;
			const previousValues = signatureFieldValueMap.get(fieldName) || [];

			previousValues.push(fieldValue);

			signatureFieldValueMap.set(fieldName, previousValues);
		});
	}
	catch (error) {
		return signatureFieldValueMap;
	}

	return signatureFieldValueMap;
}

function normalizeTrackedFormFieldName(name) {
	if (name.includes('ddm$$')) {
		let normalizedFieldName = name;

		normalizedFieldName = normalizedFieldName.replace(
			/\$[^$#]+\$(\d+#)/g,
			(_matchedName, repeatedSuffix) => `$${repeatedSuffix}`
		);
		normalizedFieldName = normalizedFieldName.replace(
			/\$[^$#]+\$(\d+\$\$)/g,
			(_matchedName, repeatedSuffix) => `$${repeatedSuffix}`
		);

		return normalizedFieldName;
	}

	return name;
}

function normalizeTrackedFormFieldValue(value) {
	if (typeof value !== 'string') {
		return value;
	}

	return value.replace(/\u200B/g, '').trim();
}

function attachListener(element, eventType, callback) {
	element?.addEventListener(eventType, callback);

	return {
		detach() {
			element?.removeEventListener(eventType, callback);
		},
	};
}
