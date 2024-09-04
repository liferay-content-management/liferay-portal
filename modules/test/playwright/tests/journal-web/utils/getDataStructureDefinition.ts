/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

interface Props {
	defaultLanguageId: Locale;
	fields: Field[];
	name: string;
}

interface Field {
	localizable?: boolean;
	name: string;
	repeatable?: boolean;
	required?: boolean;
}

export function getDataStructureDefinition({
	defaultLanguageId,
	fields,
	name,
}: Props): DataDefinition {
	return {
		availableLanguageIds: [defaultLanguageId],
		dataDefinitionFields: fields.map(
			({
				localizable = true,
				name: fieldName,
				repeatable = false,
				required = false,
			}) => {
				return {
					customProperties: {
						dataType: 'string',
						displayStyle: 'singleline',
						fieldReference: fieldName,
					},
					defaultValue: {},
					fieldType: 'text',
					indexType: 'keyword',
					label: {
						[defaultLanguageId]: fieldName,
					},
					localizable,
					name: fieldName,
					repeatable,
					required,
					showLabel: true,
				};
			}
		),
		defaultDataLayout: {
			dataLayoutPages: [
				{
					dataLayoutRows: fields.map((field) => {
						return {
							dataLayoutColumns: [
								{
									columnSize: 12,
									fieldNames: [field.name],
								},
							],
						};
					}),
					description: {
						[defaultLanguageId]: '',
					},
					title: {
						[defaultLanguageId]: '',
					},
				},
			],
			name: {
				[defaultLanguageId]: name,
			},
			paginationMode: 'single-page',
		},
		defaultLanguageId,
		id: '',
		name: {
			[defaultLanguageId]: name,
		},
	};
}

export function getDataStructureDefinitionWithSelectFromList({
	defaultLanguageId,
	fields,
	name,
}: Props): DataDefinition {
	return {
		availableLanguageIds: [defaultLanguageId],
		dataDefinitionFields: fields.map(
			({
				localizable = true,
				name: fieldName,
				repeatable = false,
				required = false,
			}) => {
				return {
					customProperties: {
						dataType: "string",
						fieldReference: fieldName,
						options: {
						  [defaultLanguageId]: [
							{
							  label: "1",
							  reference: "option1",
							  value: "Option53486543"
							},
							{
							  label: "2",
							  reference: "option2",
							  value: "Option95243515"
							},
							{
							  label: "3",
							  reference: "option3",
							  value: "Option35020781"
							}
						  ]
						},
					  },
					  defaultValue: {},
					  fieldType: "select",
					  indexType: "keyword",
					  label: {
						[defaultLanguageId]: fieldName
					  },
					  localizable,
					  name: fieldName,
					  repeatable,
					  required,
					  showLabel: true,
				};
			}
		),
		defaultDataLayout: {
			dataLayoutPages: [
				{
					dataLayoutRows: fields.map((field) => {
						return {
							dataLayoutColumns: [
								{
									columnSize: 12,
									fieldNames: [field.name],
								},
							],
						};
					}),
					description: {
						[defaultLanguageId]: '',
					},
					title: {
						[defaultLanguageId]: '',
					},
				},
			],
			name: {
				[defaultLanguageId]: name,
			},
			paginationMode: 'single-page',
		},
		defaultLanguageId,
		id: '',
		name: {
			[defaultLanguageId]: name,
		},
	};
}
