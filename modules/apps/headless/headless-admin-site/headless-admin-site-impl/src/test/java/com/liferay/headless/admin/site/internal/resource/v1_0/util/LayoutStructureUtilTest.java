/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.internal.resource.v1_0.util;

import com.liferay.headless.admin.site.dto.v1_0.BasicFragmentInstancePageElementDefinition;
import com.liferay.headless.admin.site.dto.v1_0.PageElement;
import com.liferay.headless.admin.site.dto.v1_0.PageElementDefinition;
import com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer.LayoutStructureItemImporter;
import com.liferay.headless.admin.site.internal.resource.v1_0.layout.structure.item.importer.context.LayoutStructureItemImporterContext;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.osgi.util.ServiceTrackerFactory;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

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
public class LayoutStructureUtilTest {

	@ClassRule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() throws Exception {
		_serviceTrackerFactoryMockedStatic = Mockito.mockStatic(
			ServiceTrackerFactory.class);

		_serviceTrackerFactoryMockedStatic.when(
			() -> ServiceTrackerFactory.open(
				Mockito.any(), Mockito.any(Class.class))
		).thenReturn(
			null
		);

		_layoutStructureItemImporterUtilMockedStatic = Mockito.mockStatic(
			LayoutStructureItemImporterUtil.class);

		_layoutStructureItemImporterUtilMockedStatic.when(
			() ->
				LayoutStructureItemImporterUtil.getLayoutStructureItemImporter(
					Mockito.any(PageElementDefinition.class))
		).thenReturn(
			_layoutStructureItemImporter
		);

		Mockito.when(
			_layoutStructureItemImporter.addLayoutStructureItem(
				Mockito.any(LayoutStructure.class),
				Mockito.any(LayoutStructureItemImporterContext.class),
				Mockito.any(PageElement.class))
		).thenReturn(
			Mockito.mock(LayoutStructureItem.class)
		);
	}

	@AfterClass
	public static void tearDownClass() {
		_layoutStructureItemImporterUtilMockedStatic.close();
		_serviceTrackerFactoryMockedStatic.close();
	}

	@Before
	public void setUp() {
		Mockito.clearInvocations(_layoutStructureItemImporter);
	}

	@Test
	public void testAddLayoutStructureItemSkipsAllNullDefinitionChildren()
		throws Exception {

		PageElement pageElement = _createPageElement(
			"parent", _createPageElementDefinition());

		pageElement.setPageElements(
			new PageElement[] {
				_createPageElement("child-null-1", null),
				_createPageElement("child-null-2", null)
			});

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				LayoutStructureUtil.class.getName(), LoggerTestUtil.WARN)) {

			LayoutStructureUtil.addLayoutStructureItem(
				Mockito.mock(LayoutStructure.class),
				Mockito.mock(LayoutStructureItemImporterContext.class),
				pageElement);

			_assertLogEntries(logCapture, "child-null-1", "child-null-2");
		}

		Mockito.verify(
			_layoutStructureItemImporter, Mockito.times(1)
		).addLayoutStructureItem(
			Mockito.any(), Mockito.any(), Mockito.any()
		);
	}

	@Test
	public void testAddLayoutStructureItemSkipsMixedNullAndValidChildren()
		throws Exception {

		PageElement pageElement = _createPageElement(
			"parent", _createPageElementDefinition());

		pageElement.setPageElements(
			new PageElement[] {
				_createPageElement(
					"child-valid-1", _createPageElementDefinition()),
				_createPageElement("child-null", null),
				_createPageElement(
					"child-valid-2", _createPageElementDefinition())
			});

		LayoutStructureItem layoutStructureItem = null;

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				LayoutStructureUtil.class.getName(), LoggerTestUtil.WARN)) {

			layoutStructureItem = LayoutStructureUtil.addLayoutStructureItem(
				Mockito.mock(LayoutStructure.class),
				Mockito.mock(LayoutStructureItemImporterContext.class),
				pageElement);

			_assertLogEntries(logCapture, "child-null");
		}

		Assert.assertNotNull(layoutStructureItem);

		Mockito.verify(
			_layoutStructureItemImporter, Mockito.times(3)
		).addLayoutStructureItem(
			Mockito.any(), Mockito.any(), Mockito.any()
		);
	}

	@Test
	public void testAddLayoutStructureItemWithoutChildren() throws Exception {
		PageElement pageElement = _createPageElement(
			"parent", _createPageElementDefinition());

		LayoutStructureItem layoutStructureItem = null;

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				LayoutStructureUtil.class.getName(), LoggerTestUtil.WARN)) {

			layoutStructureItem = LayoutStructureUtil.addLayoutStructureItem(
				Mockito.mock(LayoutStructure.class),
				Mockito.mock(LayoutStructureItemImporterContext.class),
				pageElement);

			_assertLogEntries(logCapture);
		}

		Assert.assertNotNull(layoutStructureItem);

		Mockito.verify(
			_layoutStructureItemImporter, Mockito.times(1)
		).addLayoutStructureItem(
			Mockito.any(), Mockito.any(), Mockito.any()
		);
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

	private static final LayoutStructureItemImporter
		_layoutStructureItemImporter = Mockito.mock(
			LayoutStructureItemImporter.class);
	private static MockedStatic<LayoutStructureItemImporterUtil>
		_layoutStructureItemImporterUtilMockedStatic;
	private static MockedStatic<ServiceTrackerFactory>
		_serviceTrackerFactoryMockedStatic;

}