package com.liferay.layout.page.template.internal.search.spi.index.contributor;

import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.portal.search.batch.BatchIndexingActionable;
import com.liferay.portal.search.batch.DynamicQueryBatchIndexingActionableFactory;
import com.liferay.portal.search.spi.model.index.contributor.ModelIndexerWriterContributor;
import com.liferay.portal.search.spi.model.index.contributor.helper.ModelIndexerWriterDocumentHelper;

/**
 * @author Juan Pablo Montero
 */
public class LayoutPageTemplateEntryModelIndexerWriterContributor  implements ModelIndexerWriterContributor<LayoutPageTemplateEntry> {

    public LayoutPageTemplateEntryModelIndexerWriterContributor(LayoutPageTemplateEntryLocalService layoutPageTemplateEntryLocalService,
                                                                DynamicQueryBatchIndexingActionableFactory dynamicQueryBatchIndexingActionableFactory ) {

        _layoutPageTemplateEntryLocalService = layoutPageTemplateEntryLocalService;
        _dynamicQueryBatchIndexingActionableFactory = dynamicQueryBatchIndexingActionableFactory;

    }
    @Override
    public void customize(BatchIndexingActionable batchIndexingActionable, ModelIndexerWriterDocumentHelper modelIndexerWriterDocumentHelper) {
        batchIndexingActionable.setPerformActionMethod(
                (LayoutPageTemplateEntry layoutPageTemplateEntry) -> batchIndexingActionable.addDocuments(
                        modelIndexerWriterDocumentHelper.getDocument(layoutPageTemplateEntry)));
    }

    @Override
    public BatchIndexingActionable getBatchIndexingActionable() {
        return _dynamicQueryBatchIndexingActionableFactory.getBatchIndexingActionable(
                _layoutPageTemplateEntryLocalService.getIndexableActionableDynamicQuery());
    }

    @Override
    public long getCompanyId(LayoutPageTemplateEntry layoutPageTemplateEntry) {

        return layoutPageTemplateEntry.getCompanyId();
    }

    private final DynamicQueryBatchIndexingActionableFactory
            _dynamicQueryBatchIndexingActionableFactory;

    private final LayoutPageTemplateEntryLocalService _layoutPageTemplateEntryLocalService;

}
