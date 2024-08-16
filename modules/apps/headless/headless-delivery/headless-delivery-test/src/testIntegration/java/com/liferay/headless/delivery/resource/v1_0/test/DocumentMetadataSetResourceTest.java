/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFileEntryMetadata;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestUtil;
import com.liferay.headless.delivery.client.dto.v1_0.ContentStructureField;
import com.liferay.headless.delivery.client.dto.v1_0.DocumentMetadataSet;
import com.liferay.headless.delivery.client.dto.v1_0.Option;
import com.liferay.headless.delivery.dto.v1_0.util.ContentStructureUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.util.GroupUtil;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.util.Locale;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Javier Gamarra
 */
@RunWith(Arquillian.class)
public class DocumentMetadataSetResourceTest
	extends BaseDocumentMetadataSetResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Override
	@Test
	public void testGetDocumentMetadataSet() throws Exception {
		super.testGetDocumentMetadataSet();

		DocumentMetadataSet postDocumentMetadataSet = _addDocumentMetadataSet(
			testGroup);

		DocumentMetadataSet getDocumentMetadataSet =
			documentMetadataSetResource.getDocumentMetadataSet(
				postDocumentMetadataSet.getId());

		getDocumentMetadataSet.setActions(postDocumentMetadataSet.getActions());

		assertEquals(postDocumentMetadataSet, getDocumentMetadataSet);
		assertValid(getDocumentMetadataSet);
	}

	@Override
	protected DocumentMetadataSet
			testGetAssetLibraryDocumentMetadataSetsPage_addDocumentMetadataSet(
				Long assetLibraryId, DocumentMetadataSet documentMetadataSet)
		throws Exception {

		if (assetLibraryId.equals(
				testGetAssetLibraryDocumentMetadataSetsPage_getIrrelevantAssetLibraryId())) {

			return randomIrrelevantDocumentMetadataSet();
		}

		return _addDocumentMetadataSet(testDepotEntry.getGroup());
	}

	@Override
	protected DocumentMetadataSet
			testGetDocumentMetadataSet_addDocumentMetadataSet()
		throws Exception {

		return _addDocumentMetadataSet(testGroup);
	}

	@Override
	protected DocumentMetadataSet
			testGetSiteDocumentMetadataSetsPage_addDocumentMetadataSet(
				Long siteId, DocumentMetadataSet documentMetadataSet)
		throws Exception {

		if (siteId.equals(
				testGetSiteDocumentMetadataSetsPage_getIrrelevantSiteId())) {

			return _addDocumentMetadataSet(irrelevantGroup);
		}

		return _addDocumentMetadataSet(testGroup);
	}

	@Override
	protected DocumentMetadataSet
			testGraphQLDocumentMetadataSet_addDocumentMetadataSet()
		throws Exception {

		return _addDocumentMetadataSet(testGroup);
	}

	private DocumentMetadataSet _addDocumentMetadataSet(Group group)
		throws Exception {

		DDMStructure ddmStructure = DDMStructureTestUtil.addStructure(
			group.getGroupId(), DLFileEntryMetadata.class.getName());

		return new DocumentMetadataSet() {
			{
				setActions(() -> null);
				setAssetLibraryKey(() -> GroupUtil.getAssetLibraryKey(group));

				setAvailableLanguages(
					() -> LocaleUtil.toW3cLanguageIds(
						ddmStructure.getAvailableLanguageIds()));

				setContentStructureFields(
					() -> TransformUtil.transformToArray(
						ddmStructure.getRootFieldNames(),
						fieldName -> _toContentStructureField(
							true, ddmStructure.getDDMFormField(fieldName),
							LocaleThreadLocal.getDefaultLocale()),
						ContentStructureField.class));
				setDateCreated(ddmStructure::getCreateDate);
				setDateModified(ddmStructure::getModifiedDate);

				setDescription(
					() -> ddmStructure.getDescription(
						testGroup.getDefaultLanguageId()));

				setDescription_i18n(
					() -> LocalizedMapUtil.getI18nMap(
						ddmStructure.getDescriptionMap()));
				setId(ddmStructure.getStructureId());

				setName(
					() -> ddmStructure.getName(
						testGroup.getDefaultLanguageId()));

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

}