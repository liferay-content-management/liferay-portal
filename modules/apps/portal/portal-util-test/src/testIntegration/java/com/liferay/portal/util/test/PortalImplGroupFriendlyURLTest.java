/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.util.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.test.util.PropsValuesReplacer;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cristina González
 */
@RunWith(Arquillian.class)
public class PortalImplGroupFriendlyURLTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testGetGroupFriendlyURL() throws Exception {
		try (PropsValuesReplacer propsValuesReplacer = new PropsValuesReplacer(
				"LOCALE_PREPEND_FRIENDLY_URL_STYLE", 3)) {

			String groupFriendlyURL = _portal.getGroupFriendlyURL(
				_group.getPublicLayoutSet(), _getThemeDisplay(LocaleUtil.US),
				LocaleUtil.SPAIN);

			Assert.assertTrue(groupFriendlyURL.startsWith("/es"));
		}
	}

	private ThemeDisplay _getThemeDisplay(Locale locale) {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setI18nLanguageId(LocaleUtil.toLanguageId(locale));
		themeDisplay.setI18nPath(
			StringPool.SLASH.concat(LocaleUtil.toW3cLanguageId(locale)));

		themeDisplay.setSiteGroupId(_group.getGroupId());

		return themeDisplay;
	}

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private Portal _portal;

}