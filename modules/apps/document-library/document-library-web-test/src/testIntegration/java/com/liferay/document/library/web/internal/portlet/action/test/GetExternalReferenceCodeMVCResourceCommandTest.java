/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.portlet.MockLiferayResourceRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayResourceResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Attila Bakay
 */
@RunWith(Arquillian.class)
public class GetExternalReferenceCodeMVCResourceCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_folder = _dlAppLocalService.addFolder(
			null, TestPropsValues.getUserId(), _group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId()));
	}

	@Test
	public void testGetHomeFolderExternalReferenceCode() throws Exception {
		MockLiferayResourceResponse mockLiferayResourceResponse =
			new MockLiferayResourceResponse();

		_mvcResourceCommand.serveResource(
			_getMockLiferayResourceRequest(_group.getGroupId(), 0),
			mockLiferayResourceResponse);

		JSONObject jsonObject = _getResponseJSONObject(
			mockLiferayResourceResponse);

		Assert.assertEquals(
			_group.getGroupId(), jsonObject.getLong("repositoryGroupId"));
		Assert.assertEquals(
			_group.getExternalReferenceCode(),
			jsonObject.getString("selectedRepositoryExternalReferenceCode"));
		Assert.assertEquals(
			StringPool.BLANK,
			jsonObject.getString("rootFolderExternalReferenceCode"));
	}

	@Test
	public void testGetSpecificFolderExternalReferenceCode() throws Exception {
		MockLiferayResourceResponse mockLiferayResourceResponse =
			new MockLiferayResourceResponse();

		_mvcResourceCommand.serveResource(
			_getMockLiferayResourceRequest(
				_group.getGroupId(), _folder.getFolderId()),
			mockLiferayResourceResponse);

		JSONObject jsonObject = _getResponseJSONObject(
			mockLiferayResourceResponse);

		Assert.assertEquals(
			_group.getGroupId(), jsonObject.getLong("repositoryGroupId"));
		Assert.assertEquals(
			_group.getExternalReferenceCode(),
			jsonObject.getString("selectedRepositoryExternalReferenceCode"));
		Assert.assertEquals(
			_folder.getExternalReferenceCode(),
			jsonObject.getString("rootFolderExternalReferenceCode"));
	}

	private MockLiferayResourceRequest _getMockLiferayResourceRequest(
			long selectedRepositoryId, long rootFolderId)
		throws Exception {

		MockLiferayResourceRequest mockLiferayResourceRequest =
			new MockLiferayResourceRequest();

		mockLiferayResourceRequest.setAttribute(
			WebKeys.THEME_DISPLAY, new ThemeDisplay());

		mockLiferayResourceRequest.setParameter(
			"selectedRepositoryId", String.valueOf(selectedRepositoryId));
		mockLiferayResourceRequest.setParameter(
			"rootFolderId", String.valueOf(rootFolderId));

		return mockLiferayResourceRequest;
	}

	private JSONObject _getResponseJSONObject(
			MockLiferayResourceResponse mockLiferayResourceResponse)
		throws Exception {

		ByteArrayOutputStream byteArrayOutputStream =
			(ByteArrayOutputStream)
				mockLiferayResourceResponse.getPortletOutputStream();

		return JSONFactoryUtil.createJSONObject(
			new String(byteArrayOutputStream.toByteArray()));
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	private Folder _folder;

	@DeleteAfterTestRun
	private Group _group;

	@Inject(
		filter = "mvc.command.name=/document_library/get_external_reference_code"
	)
	private MVCResourceCommand _mvcResourceCommand;

}