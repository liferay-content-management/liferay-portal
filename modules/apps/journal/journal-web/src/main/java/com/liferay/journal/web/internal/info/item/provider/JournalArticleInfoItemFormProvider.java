/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.journal.web.internal.info.item.provider;

import com.liferay.asset.info.item.provider.AssetEntryInfoItemFieldSetProvider;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.dynamic.data.mapping.exception.NoSuchStructureException;
import com.liferay.dynamic.data.mapping.info.item.provider.DDMStructureInfoItemFieldSetProvider;
import com.liferay.dynamic.data.mapping.info.item.provider.DDMTemplateInfoItemFieldSetProvider;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.expando.info.item.provider.ExpandoInfoItemFieldSetProvider;
import com.liferay.info.exception.NoSuchClassTypeException;
import com.liferay.info.field.InfoFieldSetEntry;
import com.liferay.info.form.InfoForm;
import com.liferay.info.item.field.reader.InfoItemFieldReaderFieldSetProvider;
import com.liferay.info.item.provider.InfoItemFormProvider;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.web.internal.info.item.JournalArticleInfoItemFields;

import java.util.Arrays;
import java.util.Collection;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 * @author Jorge Ferrer
 */
@Component(
	immediate = true, property = Constants.SERVICE_RANKING + ":Integer=10",
	service = InfoItemFormProvider.class
)
public class JournalArticleInfoItemFormProvider
	implements InfoItemFormProvider<JournalArticle> {

	@Override
	public InfoForm getInfoForm() {
		return InfoForm.builder(
		).addAll(
			_getJournalArticleFields()
		).add(
			_infoItemFieldReaderFieldSetProvider.getInfoFieldSet(
				JournalArticle.class.getName())
		).add(
			_assetEntryInfoItemFieldSetProvider.getInfoFieldSet(
				AssetEntry.class.getName())
		).add(
			_expandoInfoItemFieldSetProvider.getInfoFieldSet(
				JournalArticle.class.getName())
		).name(
			JournalArticle.class.getName()
		).build();
	}

	@Override
	public InfoForm getInfoForm(JournalArticle article) {
		DDMStructure ddmStructure = article.getDDMStructure();

		long ddmStructureId = ddmStructure.getStructureId();

		try {
			return getInfoForm(ddmStructureId);
		}
		catch (NoSuchClassTypeException noSuchClassTypeException) {
			throw new RuntimeException(
				"Unable to get dynamic data mapping structure " +
					ddmStructureId,
				noSuchClassTypeException);
		}
	}

	@Override
	public InfoForm getInfoForm(long ddmStructureId)
		throws NoSuchClassTypeException {

		try {
			return InfoForm.builder(
			).addAll(
				_getJournalArticleFields()
			).add(
				_infoItemFieldReaderFieldSetProvider.getInfoFieldSet(
					JournalArticle.class.getName())
			).add(
				_assetEntryInfoItemFieldSetProvider.getInfoFieldSet(
					AssetEntry.class.getName())
			).add(
				_expandoInfoItemFieldSetProvider.getInfoFieldSet(
					JournalArticle.class.getName())
			).<NoSuchStructureException>add(
				consumer -> {
					if (ddmStructureId != 0) {
						consumer.accept(
							_ddmStructureInfoItemFieldSetProvider.
								getInfoItemFieldSet(ddmStructureId));

						consumer.accept(
							_ddmTemplateInfoItemFieldSetProvider.
								getInfoItemFieldSet(ddmStructureId));
					}
				}
			).name(
				JournalArticle.class.getName()
			).build();
		}
		catch (NoSuchStructureException noSuchStructureException) {
			throw new NoSuchClassTypeException(
				ddmStructureId, noSuchStructureException);
		}
	}

	private Collection<InfoFieldSetEntry> _getJournalArticleFields() {
		return Arrays.asList(
			JournalArticleInfoItemFields.titleInfoField,
			JournalArticleInfoItemFields.descriptionInfoField,
			JournalArticleInfoItemFields.smallImageInfoField,
			JournalArticleInfoItemFields.authorNameInfoField,
			JournalArticleInfoItemFields.authorProfileImageInfoField,
			JournalArticleInfoItemFields.lastEditorNameInfoField,
			JournalArticleInfoItemFields.lastEditorProfileImageInfoField,
			JournalArticleInfoItemFields.publishDateInfoField,
			JournalArticleInfoItemFields.displayPageUrlInfoField);
	}

	@Reference
	private AssetEntryInfoItemFieldSetProvider
		_assetEntryInfoItemFieldSetProvider;

	@Reference
	private DDMStructureInfoItemFieldSetProvider
		_ddmStructureInfoItemFieldSetProvider;

	@Reference
	private DDMTemplateInfoItemFieldSetProvider
		_ddmTemplateInfoItemFieldSetProvider;

	@Reference
	private ExpandoInfoItemFieldSetProvider _expandoInfoItemFieldSetProvider;

	@Reference
	private InfoItemFieldReaderFieldSetProvider
		_infoItemFieldReaderFieldSetProvider;

}