/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.search.spi.model.index.contributor;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jan Brychta
 */
@Component(
	property = "indexer.class.name=com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken",
	service = ModelDocumentContributor.class
)
public class KaleoTaskInstanceTokenModelDocumentContributor
	implements ModelDocumentContributor<KaleoTaskInstanceToken> {

	@Override
	public void contribute(
		Document document, KaleoTaskInstanceToken kaleoTaskInstanceToken) {

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			kaleoTaskInstanceToken.getClassPK());

		if (objectEntry == null) {
			return;
		}

		ObjectDefinition objectDefinition = objectEntry.getObjectDefinition();

		if (objectDefinition == null) {
			return;
		}

		if (objectDefinition.isCMP()) {
			document.addKeyword("cmpAsset", true);
		}

		if (objectDefinition.isCMS()) {
			document.addKeyword("cmsAsset", true);
		}
	}

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

}