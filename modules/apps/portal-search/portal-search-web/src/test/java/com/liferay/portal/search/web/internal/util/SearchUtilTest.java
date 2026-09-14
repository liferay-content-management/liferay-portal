/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.util;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.service.AssetEntryLocalServiceUtil;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.portlet.PortletURL;
import jakarta.portlet.RenderRequest;
import jakarta.portlet.RenderResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Roselaine Marques
 */
public class SearchUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_assetEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
			AssetEntryLocalServiceUtil.class);
		_assetRendererFactoryRegistryUtilMockedStatic = Mockito.mockStatic(
			AssetRendererFactoryRegistryUtil.class);
		_portalUtilMockedStatic = Mockito.mockStatic(PortalUtil.class);

		_setUpAssetEntryLocalServiceUtil();
		_setUpAssetRendererFactoryRegistryUtil();
		_setUpRenderResponse();
	}

	@After
	public void tearDown() {
		_assetEntryLocalServiceUtilMockedStatic.close();
		_assetRendererFactoryRegistryUtilMockedStatic.close();
		_portalUtilMockedStatic.close();
	}

	@Test
	public void testGetSearchResultViewURLWhenDocumentURLIsDisplayPage()
		throws Exception {

		_setUpAssetRenderer(_DISPLAY_PAGE_URL);

		Assert.assertEquals(
			_DISPLAY_PAGE_URL,
			_getSearchResultViewURL(DLFileEntryConstants.getClassName(), true));
	}

	@Test
	public void testGetSearchResultViewURLWhenDocumentURLIsNull()
		throws Exception {

		_setUpAssetRenderer(null);

		Assert.assertEquals(
			_VIEW_CONTENT_URL,
			_getSearchResultViewURL(DLFileEntryConstants.getClassName(), true));
	}

	@Test
	public void testGetSearchResultViewURLWhenDocumentURLIsRedirect()
		throws Exception {

		_setUpAssetRenderer(
			"http://localhost:8080/c/document_library/find_file_entry?" +
				"p_l_id=1&noSuchEntryRedirect=http%3A%2F%2Flocalhost&" +
					"fileEntryId=2");

		Assert.assertEquals(
			_VIEW_CONTENT_URL,
			_getSearchResultViewURL(DLFileEntryConstants.getClassName(), true));
	}

	@Test
	public void testGetSearchResultViewURLWhenFolderURLIsRedirect()
		throws Exception {

		_setUpAssetRenderer(
			"http://localhost:8080/c/document_library/find_folder?p_l_id=1&" +
				"noSuchEntryRedirect=http%3A%2F%2Flocalhost&folderId=2");

		Assert.assertEquals(
			_VIEW_CONTENT_URL,
			_getSearchResultViewURL(DLFolderConstants.getClassName(), true));
	}

	@Test
	public void testGetSearchResultViewURLWhenNotDocumentURLIsRedirect()
		throws Exception {

		_setUpAssetRenderer(_FIND_ARTICLE_URL);

		Assert.assertEquals(
			_FIND_ARTICLE_URL,
			_getSearchResultViewURL(
				"com.liferay.journal.model.JournalArticle", true));
	}

	@Test
	public void testGetSearchResultViewURLWhenViewInContextIsDisabled()
		throws Exception {

		_setUpAssetRenderer(_DISPLAY_PAGE_URL);

		Assert.assertEquals(
			_VIEW_CONTENT_URL,
			_getSearchResultViewURL(
				DLFileEntryConstants.getClassName(), false));
	}

	private String _getSearchResultViewURL(
		String className, boolean viewInContext) {

		return SearchUtil.getSearchResultViewURL(
			_renderRequest, _renderResponse, className, 1234, viewInContext);
	}

	private void _setUpAssetEntryLocalServiceUtil() throws Exception {
		AssetEntry assetEntry = Mockito.mock(AssetEntry.class);

		_assetEntryLocalServiceUtilMockedStatic.when(
			() -> AssetEntryLocalServiceUtil.getEntry(
				Mockito.anyString(), Mockito.anyLong())
		).thenReturn(
			assetEntry
		);
	}

	private void _setUpAssetRenderer(String urlViewInContext) throws Exception {
		AssetRenderer<?> assetRenderer = Mockito.mock(AssetRenderer.class);

		Mockito.doReturn(
			urlViewInContext
		).when(
			assetRenderer
		).getURLViewInContext(
			Mockito.any(), Mockito.any(), Mockito.anyString()
		);

		Mockito.doReturn(
			assetRenderer
		).when(
			_assetRendererFactory
		).getAssetRenderer(
			Mockito.anyLong()
		);
	}

	private void _setUpAssetRendererFactoryRegistryUtil() {
		_assetRendererFactory = Mockito.mock(AssetRendererFactory.class);

		Mockito.doReturn(
			"document"
		).when(
			_assetRendererFactory
		).getType();

		_assetRendererFactoryRegistryUtilMockedStatic.when(
			() ->
				AssetRendererFactoryRegistryUtil.
					getAssetRendererFactoryByClassName(Mockito.anyString())
		).thenReturn(
			_assetRendererFactory
		);
	}

	private void _setUpRenderResponse() {
		Mockito.doReturn(
			_VIEW_CONTENT_URL
		).when(
			_viewContentURL
		).toString();

		Mockito.doReturn(
			_viewContentURL
		).when(
			_renderResponse
		).createRenderURL();
	}

	private static final String _DISPLAY_PAGE_URL =
		"http://localhost:8080/web/guest/d/sample-document";

	private static final String _FIND_ARTICLE_URL =
		"http://localhost:8080/c/journal/find_article?p_l_id=1&" +
			"noSuchEntryRedirect=http%3A%2F%2Flocalhost&articleId=2";

	private static final String _VIEW_CONTENT_URL = "VIEW_CONTENT_URL";

	private MockedStatic<AssetEntryLocalServiceUtil>
		_assetEntryLocalServiceUtilMockedStatic;
	private AssetRendererFactory<?> _assetRendererFactory;
	private MockedStatic<AssetRendererFactoryRegistryUtil>
		_assetRendererFactoryRegistryUtilMockedStatic;
	private MockedStatic<PortalUtil> _portalUtilMockedStatic;
	private final RenderRequest _renderRequest = Mockito.mock(
		RenderRequest.class);
	private final RenderResponse _renderResponse = Mockito.mock(
		RenderResponse.class);
	private final PortletURL _viewContentURL = Mockito.mock(PortletURL.class);

}