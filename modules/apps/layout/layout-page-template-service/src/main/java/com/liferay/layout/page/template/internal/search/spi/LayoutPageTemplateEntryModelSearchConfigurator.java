package com.liferay.layout.page.template.internal.search.spi;

import com.liferay.layout.page.template.internal.search.spi.index.contributor.LayoutPageTemplateEntryModelIndexerWriterContributor;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.batch.DynamicQueryBatchIndexingActionableFactory;
import com.liferay.portal.search.spi.model.index.contributor.ModelIndexerWriterContributor;
import com.liferay.portal.search.spi.model.registrar.ModelSearchConfigurator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Juan Pablo Montero
 */
@Component(service = ModelSearchConfigurator.class)
public class LayoutPageTemplateEntryModelSearchConfigurator implements ModelSearchConfigurator<LayoutPageTemplateEntry>{
    @Override
    public String getClassName() {
        return LayoutPageTemplateEntry.class.getName();
    }

    @Override
    public long getCompanyId() {
        return ModelSearchConfigurator.super.getCompanyId();
    }

    @Override
    public String[] getDefaultSelectedFieldNames() {
        return new String[] {
                Field.COMPANY_ID, Field.ENTRY_CLASS_NAME, Field.ENTRY_CLASS_PK,
                Field.UID
        };
    }


    @Override
    public ModelIndexerWriterContributor<LayoutPageTemplateEntry> getModelIndexerWriterContributor() {
        return _modelIndexWriterContributor;
    }

    @Activate
    private void activate() {
        _modelIndexWriterContributor =
                new LayoutPageTemplateEntryModelIndexerWriterContributor(
                        _layoutPageTemplateEntryLocalService,
                        _dynamicQueryBatchIndexingActionableFactory);
    }

    @Reference
    private LayoutPageTemplateEntryLocalService _layoutPageTemplateEntryLocalService;

    @Reference
    private DynamicQueryBatchIndexingActionableFactory
            _dynamicQueryBatchIndexingActionableFactory;

    private ModelIndexerWriterContributor<LayoutPageTemplateEntry>
            _modelIndexWriterContributor;
}
