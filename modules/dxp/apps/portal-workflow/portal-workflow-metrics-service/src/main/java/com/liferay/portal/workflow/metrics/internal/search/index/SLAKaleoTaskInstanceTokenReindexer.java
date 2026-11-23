/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.internal.search.index;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.search.indexer.IndexerDocumentBuilder;
import com.liferay.portal.search.indexer.IndexerWriter;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;

import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 */
@Component(service = SLAKaleoTaskInstanceTokenReindexer.class)
public class SLAKaleoTaskInstanceTokenReindexer {

	public void reindex(Collection<Long> taskIds) {
		Indexer<KaleoTaskInstanceToken> indexer =
			IndexerRegistryUtil.nullSafeGetIndexer(
				KaleoTaskInstanceToken.class);

		for (long taskId : taskIds) {
			KaleoTaskInstanceToken kaleoTaskInstanceToken =
				_kaleoTaskInstanceTokenLocalService.fetchKaleoTaskInstanceToken(
					taskId);

			if (kaleoTaskInstanceToken == null) {
				continue;
			}

			try {
				indexer.reindex(kaleoTaskInstanceToken);
			}
			catch (Exception exception) {
				_log.error(exception);
			}
		}
	}

	public void reindex(long companyId, long processId) {
		IndexableActionableDynamicQuery indexableActionableDynamicQuery =
			_indexerWriter.getIndexableActionableDynamicQuery();

		indexableActionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				DynamicQuery kaleoInstanceDynamicQuery =
					_kaleoInstanceLocalService.dynamicQuery();

				kaleoInstanceDynamicQuery.add(
					RestrictionsFactoryUtil.eq("kaleoDefinitionId", processId));
				kaleoInstanceDynamicQuery.add(
					RestrictionsFactoryUtil.eq("completed", false));
				kaleoInstanceDynamicQuery.setProjection(
					ProjectionFactoryUtil.property("kaleoInstanceId"));

				Property kaleoInstanceIdProperty = PropertyFactoryUtil.forName(
					"kaleoInstanceId");

				dynamicQuery.add(
					kaleoInstanceIdProperty.in(kaleoInstanceDynamicQuery));
			});
		indexableActionableDynamicQuery.setCompanyId(companyId);
		indexableActionableDynamicQuery.setPerformActionMethod(
			_indexerDocumentBuilder::getDocument);

		indexableActionableDynamicQuery.performActions();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SLAKaleoTaskInstanceTokenReindexer.class);

	@Reference(
		target = "(indexer.class.name=com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken)"
	)
	private IndexerDocumentBuilder _indexerDocumentBuilder;

	@Reference(
		target = "(indexer.class.name=com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken)"
	)
	private IndexerWriter<KaleoTaskInstanceToken> _indexerWriter;

	@Reference
	private KaleoInstanceLocalService _kaleoInstanceLocalService;

	@Reference
	private KaleoTaskInstanceTokenLocalService
		_kaleoTaskInstanceTokenLocalService;

}