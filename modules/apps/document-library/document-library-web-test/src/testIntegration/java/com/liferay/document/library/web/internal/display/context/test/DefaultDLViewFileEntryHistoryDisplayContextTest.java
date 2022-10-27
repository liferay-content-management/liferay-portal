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

package com.liferay.document.library.web.internal.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.document.library.display.context.DLDisplayContextProvider;
import com.liferay.document.library.display.context.DLViewFileEntryHistoryDisplayContext;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.model.DLVersionNumberIncrease;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletURL;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Mikel Lorza
 */
@RunWith(Arquillian.class)
public class DefaultDLViewFileEntryHistoryDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_company = _companyLocalService.getCompany(_group.getCompanyId());
		_layout = LayoutTestUtil.addTypePortletLayout(_group);
	}

	@Test
	public void testGetActionDropdownItemsOneVersion() throws Exception {
		FileEntry fileEntry = _getFileEntry(1);

		List<DropdownItem> dropdownItemList = _getActionDropdownItems(
			fileEntry.getLatestFileVersion());

		Assert.assertEquals(
			dropdownItemList.toString(), 2, dropdownItemList.size());

		DropdownItem dropdownItemListGroup = dropdownItemList.get(0);

		List<DropdownItem> dropdownItemListFirstGroup =
			(List<DropdownItem>)dropdownItemListGroup.get("items");

		Assert.assertEquals(
			dropdownItemListFirstGroup.toString(), 3,
			dropdownItemListFirstGroup.size());

		dropdownItemListGroup = dropdownItemList.get(1);

		List<DropdownItem> dropdownItemListSecondGroup =
			(List<DropdownItem>)dropdownItemListGroup.get("items");

		Assert.assertEquals(
			dropdownItemListSecondGroup.toString(), 0,
			dropdownItemListSecondGroup.size());
	}

	@Test
	public void testGetActionDropdownItemsThreeVersions() throws Exception {
		FileEntry fileEntry = _getFileEntry(3);

		FileVersion fileVersion = fileEntry.getFileVersion("1.1");

		List<DropdownItem> dropdownItemList = _getActionDropdownItems(
			fileVersion);

		Assert.assertEquals(
			dropdownItemList.toString(), 2, dropdownItemList.size());

		DropdownItem dropdownItemListGroup = dropdownItemList.get(0);

		List<DropdownItem> dropdownItemListFirstGroup =
			(List<DropdownItem>)dropdownItemListGroup.get("items");

		Assert.assertEquals(
			dropdownItemListFirstGroup.toString(), 4,
			dropdownItemListFirstGroup.size());

		dropdownItemListGroup = dropdownItemList.get(1);

		List<DropdownItem> dropdownItemListSecondGroup =
			(List<DropdownItem>)dropdownItemListGroup.get("items");

		Assert.assertEquals(
			dropdownItemListSecondGroup.toString(), 1,
			dropdownItemListSecondGroup.size());

		DropdownItem dropdownItemDownload = dropdownItemListFirstGroup.get(0);

		Assert.assertEquals("download", dropdownItemDownload.get("icon"));
		Assert.assertNotNull(dropdownItemDownload.get("href"));
		Assert.assertEquals(
			"Download (0 B)", dropdownItemDownload.get("label"));
		Assert.assertEquals(
			"com.liferay.document.library.display.context." +
				"DLUIItemKeys#download",
			dropdownItemDownload.get("key"));

		DropdownItem dropdownItemView = dropdownItemListFirstGroup.get(1);

		Assert.assertEquals("download", dropdownItemDownload.get("icon"));

		String viewHref = (String)dropdownItemView.get("href");

		Assert.assertNotNull(viewHref);
		Assert.assertTrue(
			viewHref.contains(
				"param_fileEntryId=" + fileEntry.getFileEntryId()));
		Assert.assertTrue(
			viewHref.contains("param_version=" + fileVersion.getVersion()));
		Assert.assertTrue(
			viewHref.contains(
				"mvcRenderCommandName=/document_library/view_file_entry"));
		Assert.assertTrue(
			viewHref.contains("param_redirect=" + new MockLiferayPortletURL()));
		Assert.assertTrue(
			viewHref.contains("param_backURL=" + new MockLiferayPortletURL()));

		Assert.assertEquals("View", dropdownItemView.get("label"));

		DropdownItem dropdownItemRevert = dropdownItemListFirstGroup.get(2);

		String revertHref = (String)dropdownItemRevert.get("href");

		Assert.assertNotNull(viewHref);
		Assert.assertTrue(
			revertHref.contains(
				"param_fileEntryId=" + fileEntry.getFileEntryId()));
		Assert.assertTrue(
			revertHref.contains("param_version=" + fileVersion.getVersion()));
		Assert.assertTrue(
			revertHref.contains(
				"param_javax.portlet.action=/document_library" +
					"/edit_file_entry"));
		Assert.assertTrue(revertHref.contains("param_cmd=revert"));
		Assert.assertTrue(
			revertHref.contains(
				"param_redirect=" + new MockLiferayPortletURL()));
		Assert.assertEquals("Revert", dropdownItemRevert.get("label"));

		DropdownItem dropdownItemCompare = dropdownItemListFirstGroup.get(3);

		Assert.assertEquals("Compare to...", dropdownItemCompare.get("label"));

		Map<String, String> dropdownItemCompareToData =
			(Map)dropdownItemCompare.get("data");

		String selectFileVersionURL = dropdownItemCompareToData.get(
			"selectFileVersionURL");

		Assert.assertNotNull(selectFileVersionURL);
		Assert.assertTrue(
			selectFileVersionURL.contains(
				"param_fileEntryId=" + fileEntry.getFileEntryId()));
		Assert.assertTrue(
			selectFileVersionURL.contains(
				"param_version=" + fileVersion.getVersion()));
		Assert.assertTrue(
			selectFileVersionURL.contains(
				"param_mvcRenderCommandName=/document_library" +
					"/view_file_entry"));
		Assert.assertTrue(
			selectFileVersionURL.contains(
				"param_redirect=" + new MockLiferayPortletURL()));

		String compareVersionURL = dropdownItemCompareToData.get(
			"compareVersionURL");

		Assert.assertNotNull(compareVersionURL);

		Assert.assertTrue(
			selectFileVersionURL.contains(
				"param_fileEntryId=" + fileEntry.getFileEntryId()));
		Assert.assertTrue(
			compareVersionURL.contains(
				"param_mvcRenderCommandName=/document_library" +
					"/compare_versions"));
		Assert.assertTrue(
			compareVersionURL.contains(
				"param_backURL=" + new MockLiferayPortletURL()));

		DropdownItem dropdownItemDelete = dropdownItemListSecondGroup.get(0);

		Map<String, String> dropdownItemDeleteData =
			(Map)dropdownItemDelete.get("data");

		String deleteURL = dropdownItemDeleteData.get("deleteURL");

		Assert.assertNotNull(viewHref);
		Assert.assertTrue(
			deleteURL.contains(
				"param_fileEntryId=" + fileEntry.getFileEntryId()));
		Assert.assertTrue(
			deleteURL.contains("param_version=" + fileVersion.getVersion()));
		Assert.assertTrue(
			deleteURL.contains(
				"param_javax.portlet.action=/document_library" +
					"/edit_file_entry"));
		Assert.assertTrue(deleteURL.contains("param_cmd=delete"));
		Assert.assertTrue(
			deleteURL.contains(
				"param_redirect=" + new MockLiferayPortletURL()));
		Assert.assertEquals("Delete", dropdownItemDelete.get("label"));
		Assert.assertEquals("trash", dropdownItemDelete.get("icon"));
	}

	private List<DropdownItem> _getActionDropdownItems(FileVersion fileVersion)
		throws Exception {

		DLViewFileEntryHistoryDisplayContext
			dlViewFileEntryHistoryDisplayContext = ReflectionTestUtil.invoke(
				_dlDisplayContextProvider,
				"getDLViewFileEntryHistoryDisplayContext",
				new Class<?>[] {
					HttpServletRequest.class, HttpServletResponse.class,
					FileVersion.class
				},
				_getMockLiferayPortletRenderRequest().getHttpServletRequest(),
				new MockHttpServletResponse(), fileVersion);

		return ReflectionTestUtil.invoke(
			dlViewFileEntryHistoryDisplayContext, "getActionDropdownItems",
			new Class<?>[0], null);
	}

	private FileEntry _getFileEntry(int numVersions) throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(_group.getGroupId());

		FileEntry fileEntry = _dlAppLocalService.addFileEntry(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			_group.getGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			"example.txt",
			MimeTypesUtil.getExtensionContentType(ContentTypes.IMAGE_JPEG),
			new byte[0], null, null, serviceContext);

		fileEntry = _dlAppLocalService.updateFileEntry(
			fileEntry.getUserId(), fileEntry.getFileEntryId(),
			fileEntry.getFileName(), fileEntry.getMimeType(),
			fileEntry.getTitle(), StringUtil.randomString(), "description",
			RandomTestUtil.randomString(), DLVersionNumberIncrease.NONE,
			fileEntry.getContentStream(), fileEntry.getSize(),
			fileEntry.getExpirationDate(), fileEntry.getReviewDate(),
			serviceContext);

		if (numVersions > 1) {
			for (int i = 1; i < numVersions; i++) {
				fileEntry = _dlAppLocalService.updateFileEntry(
					fileEntry.getUserId(), fileEntry.getFileEntryId(),
					fileEntry.getFileName(), fileEntry.getMimeType(),
					fileEntry.getTitle(), StringUtil.randomString(),
					fileEntry.getDescription(), RandomTestUtil.randomString(),
					DLVersionNumberIncrease.MINOR, fileEntry.getContentStream(),
					fileEntry.getSize(), fileEntry.getExpirationDate(),
					fileEntry.getReviewDate(), serviceContext);
			}
		}

		return fileEntry;
	}

	private MockLiferayPortletRenderRequest
			_getMockLiferayPortletRenderRequest()
		throws Exception {

		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			new MockLiferayPortletRenderRequest();

		mockLiferayPortletRenderRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_CONFIG, null);
		mockLiferayPortletRenderRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_REQUEST,
			mockLiferayPortletRenderRequest);
		mockLiferayPortletRenderRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_RESPONSE,
			new MockLiferayPortletActionResponse());
		mockLiferayPortletRenderRequest.setAttribute(
			WebKeys.THEME_DISPLAY, _getThemeDisplay());
		mockLiferayPortletRenderRequest.setAttribute(
			StringBundler.concat(
				mockLiferayPortletRenderRequest.getPortletName(), "-",
				WebKeys.CURRENT_PORTLET_URL),
			new MockLiferayPortletURL());

		return mockLiferayPortletRenderRequest;
	}

	private ThemeDisplay _getThemeDisplay() throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		portletDisplay.setPortletName(DLPortletKeys.DOCUMENT_LIBRARY_ADMIN);

		themeDisplay.setCompany(_company);
		themeDisplay.setLayout(_layout);
		themeDisplay.setLocale(LocaleUtil.getDefault());
		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());
		themeDisplay.setRealUser(TestPropsValues.getUser());
		themeDisplay.setScopeGroupId(_layout.getGroupId());
		themeDisplay.setSiteGroupId(_layout.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		return themeDisplay;
	}

	private Company _company;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	@Inject(
		filter = "component.name=com.liferay.document.library.web.internal.display.context.DLDisplayContextProviderImpl",
		type = Inject.NoType.class
	)
	private DLDisplayContextProvider _dlDisplayContextProvider;

	@DeleteAfterTestRun
	private Group _group;

	private Layout _layout;

	@Inject
	private Portal _portal;

}