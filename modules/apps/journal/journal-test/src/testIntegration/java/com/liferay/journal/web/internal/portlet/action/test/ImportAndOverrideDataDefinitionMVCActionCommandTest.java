/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.journal.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.data.engine.rest.dto.v2_0.DataDefinition;
import com.liferay.data.engine.rest.dto.v2_0.DataDefinitionField;
import com.liferay.data.engine.rest.test.util.DataDefinitionTestUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Mariano Álvaro Sáiz
 */
@RunWith(Arquillian.class)
public class ImportAndOverrideDataDefinitionMVCActionCommandTest
	extends BaseDataDefinitionMVCActionCommandTestCase {

	public MVCActionCommand getMVCActionCommand() {
		return _mvcActionCommand;
	}

	@Test
	public void testProcessAction() throws Exception {
		DataDefinition dataDefinition =
			DataDefinitionTestUtil.addDataDefinition(
				"journal", dataDefinitionResourceFactory, group.getGroupId(),
				_read("previous_version_valid_data_definition.json"),
				TestPropsValues.getUser());

		_processAction(
			"previous_version_valid_data_definition.json", dataDefinition);

		DataDefinition importedDataDefinition = getImportedDataDefinition();

		_assertDataDefinitionKey(
			dataDefinition.getDataDefinitionKey(), importedDataDefinition);

		_assertDataDefinitionFields(
			importedDataDefinition.getDataDefinitionFields(), "Text1");

		_assertSuffix(
			importedDataDefinition.getDataDefinitionFields(), "Text1");
	}

	@Test
	public void testProcessActionOverrideDataDefinitionKey() throws Exception {
		DataDefinition dataDefinition =
			DataDefinitionTestUtil.addDataDefinition(
				"journal", dataDefinitionResourceFactory, group.getGroupId(),
				_read("previous_version_valid_data_definition.json"),
				TestPropsValues.getUser());

		_processAction(
			"previous_version_valid_data_definition_with_data_definition_key." +
				"json",
			dataDefinition);

		DataDefinition importedDataDefinition = getImportedDataDefinition();

		_assertDataDefinitionKey(
			"CUSTOM_DATA_DEFINITION_KEY", importedDataDefinition);

		_assertDataDefinitionFields(
			importedDataDefinition.getDataDefinitionFields(), "Text1");

		_assertSuffix(
			importedDataDefinition.getDataDefinitionFields(), "Text1");
	}

	@Test
	public void testProcessActionOverrideWithValidDataDefinition()
		throws Exception {

		DataDefinition dataDefinition =
			DataDefinitionTestUtil.addDataDefinition(
				"journal", dataDefinitionResourceFactory, group.getGroupId(),
				_read("previous_version_valid_data_definition.json"),
				TestPropsValues.getUser());

		_processAction(
			"valid_data_definition_with_data_definition_key_and_external_" +
				"reference_code.json",
			dataDefinition);

		DataDefinition importedDataDefinition = getImportedDataDefinition();

		_assertDataDefinitionKey(
			"CUSTOM_DATA_DEFINITION_KEY", importedDataDefinition);

		_assertDataDefinitionFields(
			importedDataDefinition.getDataDefinitionFields(), "Text32861154");

		_assertEmptySuffix(
			importedDataDefinition.getDataDefinitionFields(), "Text32861154");
	}

	private void _assertDataDefinitionFields(
		DataDefinitionField[] dataDefinitionFields,
		String previousTextFieldName) {

		Assert.assertTrue(
			StringUtil.startsWith(
				dataDefinitionFields[0].getName(), previousTextFieldName));
	}

	private void _assertDataDefinitionKey(
		String expectedDataDefinitionKey, DataDefinition dataDefinition) {

		Assert.assertEquals(
			expectedDataDefinitionKey, dataDefinition.getDataDefinitionKey());
	}

	private void _assertEmptySuffix(
		DataDefinitionField[] dataDefinitionFields,
		String previousTextFieldName) {

		String suffix = StringUtil.removeSubstring(
			dataDefinitionFields[0].getName(), previousTextFieldName);

		Assert.assertTrue(Validator.isBlank(suffix));
	}

	private void _assertSuffix(
		DataDefinitionField[] dataDefinitionFields,
		String previousTextFieldName) {

		String suffix = StringUtil.removeSubstring(
			dataDefinitionFields[0].getName(), previousTextFieldName);

		Assert.assertTrue(Validator.isNumber(suffix));
	}

	private void _processAction(String fileName, DataDefinition dataDefinition)
		throws Exception {

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			createMockLiferayPortletActionRequest(
				fileName, "Imported Structure", dataDefinition.getId());

		setUpUploadPortletRequest(mockLiferayPortletActionRequest);

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());

		Assert.assertNull(
			SessionMessages.get(
				mockLiferayPortletActionRequest,
				portal.getPortletId(mockLiferayPortletActionRequest) +
					SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE));
		Assert.assertNull(
			SessionErrors.get(
				mockLiferayPortletActionRequest,
				"importDataDefinitionErrorMessage"));
	}

	private String _read(String fileName) throws Exception {
		return new String(
			FileUtil.getBytes(getClass(), "dependencies/" + fileName));
	}

	@Inject(
		filter = "mvc.command.name=/journal/import_and_override_data_definition"
	)
	private MVCActionCommand _mvcActionCommand;

}