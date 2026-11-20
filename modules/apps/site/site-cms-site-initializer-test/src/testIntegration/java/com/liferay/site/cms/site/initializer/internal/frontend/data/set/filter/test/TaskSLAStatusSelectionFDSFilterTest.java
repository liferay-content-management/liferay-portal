/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.frontend.data.set.filter.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.frontend.data.set.filter.FDSFilter;
import com.liferay.frontend.data.set.filter.FDSFilterRegistry;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.site.cms.site.initializer.internal.constants.CMSSiteInitializerFDSNames;

import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class TaskSLAStatusSelectionFDSFilterTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testGetFDSFilters() {
		FDSFilter fdsFilter = null;

		for (FDSFilter curFDSFilter :
				_fdsFilterRegistry.getFDSFilters(
					CMSSiteInitializerFDSNames.TASKS_SECTION)) {

			if (Objects.equals(curFDSFilter.getId(), "slaBreached")) {
				fdsFilter = curFDSFilter;

				break;
			}
		}

		Assert.assertTrue(fdsFilter.isEnabled());
		Assert.assertEquals("sla-status", fdsFilter.getLabel());
	}

	@Inject
	private FDSFilterRegistry _fdsFilterRegistry;

}