package com.liferay.layout.page.template.internal.search.spi.index.contributor;

import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import org.osgi.service.component.annotations.Component;

/**
 * @author Juan Pablo Montero
 */
@Component(
        property = "indexer.class.name=com.liferay.layout.page.template.model.LayoutPageTemplateEntry",
        service = ModelDocumentContributor.class
)
public class LayoutPageTemplateEntryModelDocumentContributor implements
        ModelDocumentContributor<LayoutPageTemplateEntry>{
    @Override
    public void contribute(Document document, LayoutPageTemplateEntry layoutPageTemplateEntry) {

        document.addText(Field.NAME, layoutPageTemplateEntry.getName());
        document.addKeyword(Field.TYPE, layoutPageTemplateEntry.getType());
        document.addKeyword(Field.CLASS_NAME_ID, layoutPageTemplateEntry.getClassNameId());
        document.addNumber(Field.STATUS, layoutPageTemplateEntry.getStatus());
        document.addKeyword("externalReferenceCode", layoutPageTemplateEntry.getExternalReferenceCode());
    }
}
