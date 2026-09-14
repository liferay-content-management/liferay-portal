/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.internal.asset;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutType;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.service.PortletPreferencesLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.portlet.MutableRenderParameters;

import java.util.Collections;

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
public class AssetURLViewProviderImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_portalUtilMockedStatic = Mockito.mockStatic(PortalUtil.class);

		_portalUtilMockedStatic.when(
			() -> PortalUtil.stripURLAnchor(
				Mockito.anyString(), Mockito.anyString())
		).thenAnswer(
			invocation -> new String[] {
				invocation.getArgument(0), StringPool.BLANK
			}
		);

		ReflectionTestUtil.setFieldValue(
			_assetURLViewProviderImpl, "_assetEntryLocalService",
			_assetEntryLocalService);
		ReflectionTestUtil.setFieldValue(
			_assetURLViewProviderImpl, "_portal", _portal);
		ReflectionTestUtil.setFieldValue(
			_assetURLViewProviderImpl, "_portletPreferencesLocalService",
			_portletPreferencesLocalService);

		_setUpAssetEntryLocalService();
		_setUpAssetRendererFactory();
		_setUpLiferayPortletRequest();
		_setUpLiferayPortletResponse();
		_setUpPortletPreferencesLocalService();
	}

	@After
	public void tearDown() {
		_portalUtilMockedStatic.close();
	}

	@Test
	public void testGetAssetURLViewWhenDocumentURLIsDisplayPage()
		throws Exception {

		Assert.assertTrue(
			_getAssetURLView(
				DLFileEntryConstants.getClassName(), _DISPLAY_PAGE_URL
			).startsWith(
				_DISPLAY_PAGE_URL
			));
	}

	@Test
	public void testGetAssetURLViewWhenDocumentURLIsNull() throws Exception {
		Assert.assertTrue(
			_getAssetURLView(
				DLFileEntryConstants.getClassName(), null
			).startsWith(
				_VIEW_CONTENT_URL
			));
	}

	@Test
	public void testGetAssetURLViewWhenDocumentURLIsRedirect()
		throws Exception {

		Assert.assertTrue(
			_getAssetURLView(
				DLFileEntryConstants.getClassName(),
				"http://localhost:8080/c/document_library/find_file_entry?" +
					"p_l_id=1&noSuchEntryRedirect=http%3A%2F%2Flocalhost&" +
						"fileEntryId=2"
			).startsWith(
				_VIEW_CONTENT_URL
			));
	}

	@Test
	public void testGetAssetURLViewWhenFolderURLIsRedirect() throws Exception {
		Assert.assertTrue(
			_getAssetURLView(
				DLFolderConstants.getClassName(),
				"http://localhost:8080/c/document_library/find_folder?" +
					"p_l_id=1&noSuchEntryRedirect=http%3A%2F%2Flocalhost&" +
						"folderId=2"
			).startsWith(
				_VIEW_CONTENT_URL
			));
	}

	@Test
	public void testGetAssetURLViewWhenNotDocumentURLIsRedirect()
		throws Exception {

		Assert.assertTrue(
			_getAssetURLView(
				"com.liferay.journal.model.JournalArticle", _FIND_ARTICLE_URL
			).startsWith(
				_FIND_ARTICLE_URL
			));
	}

	private String _getAssetURLView(String className, String urlViewInContext)
		throws Exception {

		AssetRenderer<?> assetRenderer = Mockito.mock(AssetRenderer.class);

		Mockito.doReturn(
			urlViewInContext
		).when(
			assetRenderer
		).getURLViewInContext(
			Mockito.any(LiferayPortletRequest.class),
			Mockito.any(LiferayPortletResponse.class), Mockito.anyString()
		);

		return _assetURLViewProviderImpl.getAssetURLView(
			assetRenderer, _assetRendererFactory, className, 1234,
			_liferayPortletRequest, _liferayPortletResponse);
	}

	private void _setUpAssetEntryLocalService() throws Exception {
		AssetEntry assetEntry = Mockito.mock(AssetEntry.class);

		Mockito.doReturn(
			assetEntry
		).when(
			_assetEntryLocalService
		).getEntry(
			Mockito.anyString(), Mockito.anyLong()
		);
	}

	private void _setUpAssetRendererFactory() {
		Mockito.doReturn(
			"document"
		).when(
			_assetRendererFactory
		).getType();
	}

	private void _setUpLiferayPortletRequest() {
		Layout layout = Mockito.mock(Layout.class);

		Mockito.doReturn(
			Mockito.mock(LayoutType.class)
		).when(
			layout
		).getLayoutType();

		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Mockito.doReturn(
			layout
		).when(
			themeDisplay
		).getLayout();

		Mockito.doReturn(
			LocaleUtil.US
		).when(
			themeDisplay
		).getLocale();

		Mockito.doReturn(
			themeDisplay
		).when(
			_liferayPortletRequest
		).getAttribute(
			WebKeys.THEME_DISPLAY
		);
	}

	private void _setUpLiferayPortletResponse() {
		LiferayPortletURL liferayPortletURL = Mockito.mock(
			LiferayPortletURL.class, _VIEW_CONTENT_URL);

		Mockito.doReturn(
			Mockito.mock(MutableRenderParameters.class)
		).when(
			liferayPortletURL
		).getRenderParameters();

		Mockito.doReturn(
			liferayPortletURL
		).when(
			_liferayPortletResponse
		).createLiferayPortletURL(
			Mockito.anyString(), Mockito.anyString()
		);
	}

	private void _setUpPortletPreferencesLocalService() {
		Mockito.doReturn(
			Collections.emptyList()
		).when(
			_portletPreferencesLocalService
		).getPortletPreferencesByPlid(
			Mockito.anyLong()
		);
	}

	private static final String _DISPLAY_PAGE_URL =
		"http://localhost:8080/web/guest/d/sample-document";

	private static final String _FIND_ARTICLE_URL =
		"http://localhost:8080/c/journal/find_article?p_l_id=1&" +
			"noSuchEntryRedirect=http%3A%2F%2Flocalhost&articleId=2";

	private static final String _VIEW_CONTENT_URL = "VIEW_CONTENT_URL";

	private final AssetEntryLocalService _assetEntryLocalService = Mockito.mock(
		AssetEntryLocalService.class);
	private final AssetRendererFactory<?> _assetRendererFactory = Mockito.mock(
		AssetRendererFactory.class);
	private final AssetURLViewProviderImpl _assetURLViewProviderImpl =
		new AssetURLViewProviderImpl();
	private final LiferayPortletRequest _liferayPortletRequest = Mockito.mock(
		LiferayPortletRequest.class);
	private final LiferayPortletResponse _liferayPortletResponse = Mockito.mock(
		LiferayPortletResponse.class);
	private final Portal _portal = Mockito.mock(Portal.class);
	private MockedStatic<PortalUtil> _portalUtilMockedStatic;
	private final PortletPreferencesLocalService
		_portletPreferencesLocalService = Mockito.mock(
			PortletPreferencesLocalService.class);

}