/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.servlet;

import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.service.PortletLocalServiceUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Mariano Álvaro Sáiz
 */
public class GoogleGadgetServletTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		new PortalUtil(
		).setPortal(
			Mockito.mock(Portal.class)
		);

		PortletLocalServiceUtil.setService(_portletLocalService);
	}

	@Test
	public void testTitleXSSInjection() throws Exception {
		String xssInjection =
			"x\"/><x:script xmlns:x=\"http:&#x2f;&#x2f;www.w3.org/1999" +
				"/xhtml\">alert(document.domain)</x:script>";

		Mockito.when(
			_httpServletRequest.getPathInfo()
		).thenReturn(
			Portal.FRIENDLY_URL_SEPARATOR + xssInjection
		);

		Mockito.when(
			_portlet.getDisplayName()
		).thenReturn(
			xssInjection
		);

		Mockito.when(
			_portletLocalService.getPortletById(
				Mockito.anyLong(), Mockito.anyString())
		).thenReturn(
			_portlet
		);

		GoogleGadgetServlet googleGadgetServlet = new GoogleGadgetServlet();

		String content = googleGadgetServlet.getContent(_httpServletRequest);

		Assert.assertFalse(content.contains(xssInjection));
		Assert.assertTrue(content.contains(HtmlUtil.escape(xssInjection)));
	}

	private final HttpServletRequest _httpServletRequest = Mockito.mock(
		HttpServletRequest.class);
	private final Portlet _portlet = Mockito.mock(Portlet.class);
	private final PortletLocalService _portletLocalService = Mockito.mock(
		PortletLocalService.class);

}