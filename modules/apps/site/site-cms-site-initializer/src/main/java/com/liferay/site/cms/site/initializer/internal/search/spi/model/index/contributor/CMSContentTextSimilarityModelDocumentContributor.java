/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.spi.model.index.contributor;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.model.bag.ObjectFieldBag;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.HtmlParserUtil;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.site.cms.site.initializer.internal.search.similarity.TextSimilaritySignatureUtil;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;

/**
 * Adds the near-duplicate band signatures of a CMS content's main text fields
 * as a keyword field, so the Content Governance Dashboard's Text Similarity
 * widget can group near-duplicate content through a single aggregation, without
 * a per-document similarity query at read time.
 *
 * <p>
 * Runs alongside the core {@code ObjectEntryModelDocumentContributor} on the
 * same document and only contributes for CMS content object entries.
 * </p>
 *
 * @author Mikel Lorza
 */
@Component(
	property = "indexer.class.name=com.liferay.object.model.ObjectEntry",
	service = ModelDocumentContributor.class
)
public class CMSContentTextSimilarityModelDocumentContributor
	implements ModelDocumentContributor<ObjectEntry> {

	@Override
	public void contribute(Document document, ObjectEntry objectEntry) {
		try {
			if (!_isCMSContent(objectEntry)) {
				return;
			}

			String text = _getText(objectEntry);

			String[] bandSignatures =
				TextSimilaritySignatureUtil.getBandSignatures(text);

			if (bandSignatures.length > 0) {
				document.addKeyword("textSimilarityBands", bandSignatures);
			}

			String[] signature = TextSimilaritySignatureUtil.getSignature(text);

			if (signature.length > 0) {
				document.addKeyword("textSimilaritySignature", signature);
			}
		}
		catch (Exception exception) {

			// Never break indexing of the object entry because of the
			// similarity signature.

			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to contribute text similarity bands for object " +
						"entry " + objectEntry.getObjectEntryId(),
					exception);
			}
		}
	}

	private String _getText(ObjectEntry objectEntry) throws Exception {
		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		Map<String, Serializable> indexedValues =
			objectEntry.getIndexedValues();

		String defaultLanguageId = objectEntry.getDefaultLanguageId();

		ObjectFieldBag objectFieldBag = objectDefinition.getObjectFieldBag();

		StringBundler sb = new StringBundler();

		for (ObjectField objectField :
				objectFieldBag.getIndexedObjectFields()) {

			String businessType = objectField.getBusinessType();

			if (!businessType.equals(ObjectFieldConstants.BUSINESS_TYPE_TEXT) &&
				!businessType.equals(
					ObjectFieldConstants.BUSINESS_TYPE_LONG_TEXT) &&
				!businessType.equals(
					ObjectFieldConstants.BUSINESS_TYPE_RICH_TEXT)) {

				continue;
			}

			Object value = null;

			if (objectField.isLocalized()) {
				Object localizedValues = indexedValues.get(
					objectField.getI18nObjectFieldName());

				if (localizedValues instanceof Map) {
					Map<?, ?> localizedValuesMap = (Map<?, ?>)localizedValues;

					value = localizedValuesMap.get(defaultLanguageId);
				}
			}
			else {
				value = indexedValues.get(objectField.getName());
			}

			if (value == null) {
				continue;
			}

			String valueString = String.valueOf(value);

			if (businessType.equals(
					ObjectFieldConstants.BUSINESS_TYPE_RICH_TEXT)) {

				valueString = HtmlParserUtil.extractText(valueString);
			}

			sb.append(valueString);
			sb.append(CharPool.SPACE);
		}

		return sb.toString();
	}

	private boolean _isCMSContent(ObjectEntry objectEntry) throws Exception {
		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		ObjectFolder objectFolder = objectDefinition.getObjectFolder();

		if (objectFolder == null) {
			return false;
		}

		String externalReferenceCode = objectFolder.getExternalReferenceCode();

		if (Objects.equals(
				externalReferenceCode,
				ObjectFolderConstants.
					EXTERNAL_REFERENCE_CODE_CONTENT_STRUCTURES) ||
			Objects.equals(
				externalReferenceCode,
				ObjectFolderConstants.EXTERNAL_REFERENCE_CODE_FILE_TYPES)) {

			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CMSContentTextSimilarityModelDocumentContributor.class);

}