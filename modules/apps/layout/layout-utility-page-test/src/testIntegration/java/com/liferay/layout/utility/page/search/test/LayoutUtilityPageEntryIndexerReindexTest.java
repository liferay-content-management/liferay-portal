package com.liferay.layout.utility.page.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.utility.page.kernel.constants.LayoutUtilityPageEntryConstants;
import com.liferay.layout.utility.page.model.LayoutUtilityPageEntry;
import com.liferay.layout.utility.page.service.LayoutUtilityPageEntryLocalService;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.search.test.rule.SearchTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Juan Pablo Montero
 */

@RunWith(Arquillian.class)
public class LayoutUtilityPageEntryIndexerReindexTest {
    @ClassRule
    @Rule
    public static final AggregateTestRule aggregateTestRule =
            new LiferayIntegrationTestRule();

    @Before
    public void setUp() throws Exception {
        _group = GroupTestUtil.addGroup();
        _serviceContext = ServiceContextTestUtil.getServiceContext(
                _group.getGroupId(), TestPropsValues.getUserId());
        _user = TestPropsValues.getUser();
    }

    @Test
    public void testReindex() throws Exception {
        String externalReferenceCode = RandomTestUtil.randomString(8);
        Assert.assertEquals(0L, search(externalReferenceCode).getCount());
        _layoutUtilityPageEntryLocalService.addLayoutUtilityPageEntry(
                externalReferenceCode, _user.getUserId(), _group.getGroupId(),
                0, 0, false, "testName",
                LayoutUtilityPageEntryConstants.TYPE_COOKIE_POLICY, 0L, _serviceContext);
        Assert.assertEquals(1L, search(externalReferenceCode).getCount());

    }

    protected SearchResponse search(String searchTerm) {
        return searcher.search(
                _searchRequestBuilderFactory.builder(
                ).companyId(
                        _user.getCompanyId()
                ).groupIds(
                        _group.getGroupId()
                ).fields(
                        StringPool.STAR
                ).modelIndexerClasses(
                        MODEL_INDEXER_CLASS
                ).queryString(
                        searchTerm
                ).build());
    }
    @Rule
    public SearchTestRule searchTestRule = new SearchTestRule();

    @DeleteAfterTestRun
    private Group _group;

    @Inject
    private Searcher searcher;

    @Inject
    private SearchRequestBuilderFactory _searchRequestBuilderFactory;

    @Inject
    private LayoutUtilityPageEntryLocalService _layoutUtilityPageEntryLocalService;

    private User _user;
    private ServiceContext _serviceContext;
    private static final Class<?> MODEL_INDEXER_CLASS = LayoutUtilityPageEntry.class;
}
