/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.resource.v1_0.util;

import com.liferay.fragment.processor.FragmentEntryProcessorRegistry;
import com.liferay.headless.admin.site.dto.v1_0.BasicFragmentInstancePageElementDefinition;
import com.liferay.headless.admin.site.dto.v1_0.PageElement;
import com.liferay.headless.admin.site.dto.v1_0.PageElementDefinition;
import com.liferay.headless.admin.site.dto.v1_0.PageExperience;
import com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer.context.LayoutStructureItemImporterContext;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Method;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Petteri Karttunen
 */
public class SegmentsExperienceUtilTest {

	@ClassRule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() throws Exception {
		_layoutStructureUtilMockedStatic = Mockito.mockStatic(
			LayoutStructureUtil.class);

		_layoutStructureUtilMockedStatic.when(
			() -> LayoutStructureUtil.addLayoutStructureItem(
				Mockito.any(LayoutStructure.class),
				Mockito.any(LayoutStructureItemImporterContext.class),
				Mockito.any(PageElement.class))
		).thenReturn(
			null
		);

		_userLocalServiceUtilMockedStatic = Mockito.mockStatic(
			UserLocalServiceUtil.class);

		_userLocalServiceUtilMockedStatic.when(
			() -> UserLocalServiceUtil.getUser(Mockito.anyLong())
		).thenReturn(
			Mockito.mock(User.class)
		);

		_getDataMethod = SegmentsExperienceUtil.class.getDeclaredMethod(
			"_getData", FragmentEntryProcessorRegistry.class,
			InfoItemServiceRegistry.class, Layout.class, PageExperience.class,
			long.class, ServiceContext.class);

		_getDataMethod.setAccessible(true);
	}

	@AfterClass
	public static void tearDownClass() {
		_layoutStructureUtilMockedStatic.close();
		_userLocalServiceUtilMockedStatic.close();
	}

	@Before
	public void setUp() {
		_layoutStructureUtilMockedStatic.clearInvocations();
	}

	@Test
	public void testGetDataSkipsAllNullDefinitionPageElements()
		throws Exception {

		PageExperience pageExperience = new PageExperience();

		pageExperience.setPageElements(
			new PageElement[] {
				_createPageElement("page-element-null-1", null),
				_createPageElement("page-element-null-2", null)
			});

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				SegmentsExperienceUtil.class.getName(), LoggerTestUtil.WARN)) {

			_invokeGetData(pageExperience);

			_assertLogEntries(
				logCapture, "page-element-null-1", "page-element-null-2");
		}

		_layoutStructureUtilMockedStatic.verify(
			() -> LayoutStructureUtil.addLayoutStructureItem(
				Mockito.any(), Mockito.any(), Mockito.any()),
			Mockito.never());
	}

	@Test
	public void testGetDataSkipsMixedNullAndValidPageElements()
		throws Exception {

		PageExperience pageExperience = new PageExperience();

		pageExperience.setPageElements(
			new PageElement[] {
				_createPageElement(
					"page-element-valid-1", _createPageElementDefinition()),
				_createPageElement("page-element-null", null),
				_createPageElement(
					"page-element-valid-2", _createPageElementDefinition())
			});

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				SegmentsExperienceUtil.class.getName(), LoggerTestUtil.WARN)) {

			_invokeGetData(pageExperience);

			_assertLogEntries(logCapture, "page-element-null");
		}

		_layoutStructureUtilMockedStatic.verify(
			() -> LayoutStructureUtil.addLayoutStructureItem(
				Mockito.any(), Mockito.any(), Mockito.any()),
			Mockito.times(2));
	}

	@Test
	public void testGetDataWithEmptyPageElements() throws Exception {
		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				SegmentsExperienceUtil.class.getName(), LoggerTestUtil.WARN)) {

			_invokeGetData(new PageExperience());

			_assertLogEntries(logCapture);
		}

		_layoutStructureUtilMockedStatic.verify(
			() -> LayoutStructureUtil.addLayoutStructureItem(
				Mockito.any(), Mockito.any(), Mockito.any()),
			Mockito.never());
	}

	private void _assertLogEntries(
		LogCapture logCapture, String... externalReferenceCodes) {

		List<LogEntry> logEntries = logCapture.getLogEntries();

		Assert.assertEquals(
			logEntries.toString(), externalReferenceCodes.length,
			logEntries.size());

		for (int i = 0; i < externalReferenceCodes.length; i++) {
			LogEntry logEntry = logEntries.get(i);

			Assert.assertEquals(LoggerTestUtil.WARN, logEntry.getPriority());
			Assert.assertEquals(
				"Skipping page element " + externalReferenceCodes[i] +
					" with null definition",
				logEntry.getMessage());
		}
	}

	private PageElement _createPageElement(
		String externalReferenceCode,
		PageElementDefinition pageElementDefinition) {

		PageElement pageElement = new PageElement();

		pageElement.setExternalReferenceCode(externalReferenceCode);
		pageElement.setPageElementDefinition(pageElementDefinition);

		return pageElement;
	}

	private PageElementDefinition _createPageElementDefinition() {
		BasicFragmentInstancePageElementDefinition pageElementDefinition =
			new BasicFragmentInstancePageElementDefinition();

		pageElementDefinition.setType(
			PageElementDefinition.Type.BASIC_FRAGMENT);

		return pageElementDefinition;
	}

	private void _invokeGetData(PageExperience pageExperience)
		throws Exception {

		Layout layout = Mockito.mock(Layout.class);

		Mockito.when(
			layout.getGroupId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		ServiceContext serviceContext = Mockito.mock(ServiceContext.class);

		Mockito.when(
			serviceContext.getCompanyId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		_getDataMethod.invoke(
			null, Mockito.mock(FragmentEntryProcessorRegistry.class),
			Mockito.mock(InfoItemServiceRegistry.class), layout, pageExperience,
			RandomTestUtil.randomLong(), serviceContext);
	}

	private static Method _getDataMethod;
	private static MockedStatic<LayoutStructureUtil>
		_layoutStructureUtilMockedStatic;
	private static MockedStatic<UserLocalServiceUtil>
		_userLocalServiceUtilMockedStatic;

}