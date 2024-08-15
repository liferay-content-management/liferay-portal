/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.internal.dto.v1_0.converter;

import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.service.DDMStructureService;
import com.liferay.headless.delivery.dto.v1_0.ContentStructureField;
import com.liferay.headless.delivery.dto.v1_0.DocumentMetadataSet;
import com.liferay.headless.delivery.dto.v1_0.Option;
import com.liferay.headless.delivery.dto.v1_0.util.ContentStructureUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.util.GroupUtil;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Sam Ziemer
 */
@Component(
	property = "dto.class.name=com.liferay.dynamic.data.mapping.model.DDMStructure",
	service = DTOConverter.class
)
public class DocumentMetadataSetDTOConverter
	implements DTOConverter<DDMStructure, DocumentMetadataSet> {

	@Override
	public String getContentType() {
		return DDMStructure.class.getSimpleName();
	}

	@Override
	public DocumentMetadataSet toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		DDMStructure ddmStructure = _ddmStructureService.getStructure(
			(Long)dtoConverterContext.getId());

		Group group = _groupLocalService.getGroup(ddmStructure.getGroupId());

		return new DocumentMetadataSet() {
			{
				setActions(dtoConverterContext::getActions);
				setAssetLibraryKey(() -> GroupUtil.getAssetLibraryKey(group));

				setAvailableLanguages(
					() -> LocaleUtil.toW3cLanguageIds(
						ddmStructure.getAvailableLanguageIds()));

				setContentStructureFields(
					() -> TransformUtil.transformToArray(
						ddmStructure.getRootFieldNames(),
						fieldName -> _toContentStructureField(
							dtoConverterContext.isAcceptAllLanguages(),
							ddmStructure.getDDMFormField(fieldName),
							dtoConverterContext.getLocale()),
						ContentStructureField.class));
				setDateCreated(ddmStructure::getCreateDate);
				setDateModified(ddmStructure::getModifiedDate);

				setDescription(
					() -> ddmStructure.getDescription(
						dtoConverterContext.getLocale()));

				setDescription_i18n(
					() -> LocalizedMapUtil.getI18nMap(
						ddmStructure.getDescriptionMap()));
				setId(ddmStructure::getStructureId);

				setName(
					() -> ddmStructure.getName(
						dtoConverterContext.getLocale()));

				setName_i18n(
					() -> LocalizedMapUtil.getI18nMap(
						ddmStructure.getNameMap()));
			}
		};
	}

	private ContentStructureField _toContentStructureField(
		boolean acceptAllLanguage, DDMFormField ddmFormField, Locale locale) {

		LocalizedValue labelLocalizedValue = ddmFormField.getLabel();

		LocalizedValue predefinedLocalizedValue =
			ddmFormField.getPredefinedValue();

		return new ContentStructureField() {
			{
				setDataType(
					() -> ContentStructureUtil.toDataType(ddmFormField));
				setInputControl(
					() -> ContentStructureUtil.toInputControl(ddmFormField));
				setLabel(() -> _toString(labelLocalizedValue, locale));
				setLabel_i18n(
					() -> LocalizedMapUtil.getI18nMap(
						acceptAllLanguage, labelLocalizedValue.getValues()));
				setLocalizable(ddmFormField::isLocalizable);
				setMultiple(ddmFormField::isMultiple);
				setName(ddmFormField::getFieldReference);
				setNestedContentStructureFields(
					() -> TransformUtil.transformToArray(
						ddmFormField.getNestedDDMFormFields(),
						ddmFormField -> _toContentStructureField(
							acceptAllLanguage, ddmFormField, locale),
						ContentStructureField.class));
				setOptions(
					() -> {
						DDMFormFieldOptions ddmFormFieldOptions =
							ddmFormField.getDDMFormFieldOptions();

						if (ddmFormFieldOptions == null) {
							return new Option[0];
						}

						Map<String, LocalizedValue> map =
							ddmFormFieldOptions.getOptions();

						return TransformUtil.transformToArray(
							map.entrySet(),
							entry -> new Option() {
								{
									LocalizedValue localizedValue =
										entry.getValue();

									setLabel(
										() -> _toString(
											localizedValue, locale));
									setLabel_i18n(
										() -> LocalizedMapUtil.getI18nMap(
											acceptAllLanguage,
											localizedValue.getValues()));

									setValue(entry::getKey);
								}
							},
							Option.class);
					});
				setPredefinedValue(
					() -> _toString(predefinedLocalizedValue, locale));
				setPredefinedValue_i18n(
					() -> LocalizedMapUtil.getI18nMap(
						acceptAllLanguage,
						predefinedLocalizedValue.getValues()));
				setRepeatable(ddmFormField::isRepeatable);
				setRequired(ddmFormField::isRequired);
				setShowLabel(ddmFormField::isShowLabel);
			}
		};
	}

	private String _toString(LocalizedValue localizedValue, Locale locale) {
		if (localizedValue == null) {
			return null;
		}

		return localizedValue.getString(locale);
	}

	@Reference
	private DDMStructureService _ddmStructureService;

	@Reference
	private GroupLocalService _groupLocalService;

}