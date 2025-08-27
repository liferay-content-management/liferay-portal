package com.liferay.layout.utility.page.internal.search.spi.query.contributor;

import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.search.query.QueryHelper;
import com.liferay.portal.search.spi.model.query.contributor.KeywordQueryContributor;
import com.liferay.portal.search.spi.model.query.contributor.helper.KeywordQueryContributorHelper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Juan Pablo Montero
 */

@Component(
        property = "indexer.class.name=com.liferay.layout.utility.page.model.LayoutUtilityPageEntry",
        service = KeywordQueryContributor.class
)
public class LayoutUtilityPageEntryModelKeywordQueryContributor implements KeywordQueryContributor {

    @Override
    public void contribute(String keywords, BooleanQuery booleanQuery, KeywordQueryContributorHelper keywordQueryContributorHelper) {

        SearchContext searchContext =
                keywordQueryContributorHelper.getSearchContext();

        _queryHelper.addSearchTerm(
                booleanQuery, searchContext, "externalReferenceCode", false);
        _queryHelper.addSearchTerm(
                booleanQuery, searchContext, Field.NAME, false);
        _queryHelper.addSearchTerm(booleanQuery, searchContext, Field.TYPE, false);


    }

    @Reference
    private QueryHelper _queryHelper;

}
