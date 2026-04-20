/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.internal.exportimport.data.handler.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.exception.NoSuchFileEntryException;
import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.exportimport.test.util.lar.BaseStagedModelDataHandlerTestCase;
import com.liferay.fragment.constants.FragmentPortletKeys;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentComposition;
import com.liferay.fragment.service.FragmentCompositionLocalService;
import com.liferay.fragment.test.util.FragmentTestUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.model.StagedModel;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@RunWith(Arquillian.class)
public class FragmentCompositionStagedModelDataHandlerTest
	extends BaseStagedModelDataHandlerTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testDeletePreviewFileEntryWithStagingEnabled()
		throws Exception {

		Map<String, List<StagedModel>> dependentStagedModelsMap =
			addDependentStagedModelsMap(stagingGroup);

		StagedModel stagedModel = addStagedModel(
			stagingGroup, dependentStagedModelsMap);

		FragmentComposition fragmentComposition =
			(FragmentComposition)stagedModel;

		Repository repository = PortletFileRepositoryUtil.addPortletRepository(
			stagingGroup.getGroupId(), FragmentPortletKeys.FRAGMENT,
			ServiceContextTestUtil.getServiceContext(
				stagingGroup.getGroupId(), TestPropsValues.getUserId()));

		Class<?> clazz = getClass();

		ClassLoader classLoader = clazz.getClassLoader();

		FileEntry fileEntry = PortletFileRepositoryUtil.addPortletFileEntry(
			null, stagingGroup.getGroupId(), TestPropsValues.getUserId(),
			FragmentComposition.class.getName(),
			fragmentComposition.getFragmentCompositionId(),
			FragmentPortletKeys.FRAGMENT, repository.getDlFolderId(),
			classLoader.getResourceAsStream(
				"com/liferay/fragment/dependencies/liferay.png"),
			RandomTestUtil.randomString(), ContentTypes.IMAGE_PNG, false);

		stagedModel =
			_fragmentCompositionLocalService.updateFragmentComposition(
				fragmentComposition.getFragmentCompositionId(),
				fileEntry.getFileEntryId());

		_exportImportStagedModel(stagedModel);

		StagedModel importedStagedModel = getStagedModel(
			stagedModel.getUuid(), liveGroup);

		FragmentComposition importedFragmentComposition =
			(FragmentComposition)importedStagedModel;

		long importedPreviewFileEntryId =
			importedFragmentComposition.getPreviewFileEntryId();

		fileEntry = PortletFileRepositoryUtil.getPortletFileEntry(
			importedPreviewFileEntryId);

		Assert.assertNotNull(fileEntry);

		PortletFileRepositoryUtil.deletePortletFileEntry(
			fileEntry.getFileEntryId());

		stagedModel =
			_fragmentCompositionLocalService.updateFragmentComposition(
				fragmentComposition.getFragmentCompositionId(), 0);

		_exportImportStagedModel(stagedModel);

		importedStagedModel = getStagedModel(stagedModel.getUuid(), liveGroup);

		importedFragmentComposition = (FragmentComposition)importedStagedModel;

		Assert.assertEquals(
			0, importedFragmentComposition.getPreviewFileEntryId());

		fileEntry = null;

		try {
			fileEntry = PortletFileRepositoryUtil.getPortletFileEntry(
				importedPreviewFileEntryId);
		}
		catch (NoSuchFileEntryException noSuchFileEntryException) {
			Assert.assertEquals(
				StringBundler.concat(
					"No FileEntry exists with the key {fileEntryId=",
					importedPreviewFileEntryId, "}"),
				noSuchFileEntryException.getMessage());
		}

		Assert.assertNull(fileEntry);
	}

	@Test
	public void testImportRemapsFragmentCollectionId() throws Exception {
		Map<String, List<StagedModel>> dependentStagedModelsMap =
			addDependentStagedModelsMap(stagingGroup);

		FragmentComposition fragmentComposition =
			(FragmentComposition)addStagedModel(
				stagingGroup, dependentStagedModelsMap);

		_exportImportStagedModel(fragmentComposition);

		FragmentComposition importedFragmentComposition =
			(FragmentComposition)getStagedModel(
				fragmentComposition.getUuid(), liveGroup);

		Assert.assertNotEquals(
			fragmentComposition.getFragmentCollectionId(),
			importedFragmentComposition.getFragmentCollectionId());
	}

	@Override
	protected StagedModel addStagedModel(
			Group group,
			Map<String, List<StagedModel>> dependentStagedModelsMap)
		throws Exception {

		FragmentCollection fragmentCollection =
			FragmentTestUtil.addFragmentCollection(group.getGroupId());

		return _fragmentCompositionLocalService.addFragmentComposition(
			null, TestPropsValues.getUserId(), group.getGroupId(),
			fragmentCollection.getFragmentCollectionId(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), _SAMPLE_DATA, 0,
			WorkflowConstants.STATUS_APPROVED,
			ServiceContextTestUtil.getServiceContext(
				group.getGroupId(), TestPropsValues.getUserId()));
	}

	@Override
	protected StagedModel getStagedModel(String uuid, Group group)
		throws PortalException {

		return _fragmentCompositionLocalService.
			getFragmentCompositionByUuidAndGroupId(uuid, group.getGroupId());
	}

	@Override
	protected Class<? extends StagedModel> getStagedModelClass() {
		return FragmentComposition.class;
	}

	@Override
	protected void validateImportedStagedModel(
			StagedModel stagedModel, StagedModel importedStagedModel)
		throws Exception {

		super.validateImportedStagedModel(stagedModel, importedStagedModel);

		FragmentComposition fragmentComposition =
			(FragmentComposition)stagedModel;
		FragmentComposition importedFragmentComposition =
			(FragmentComposition)importedStagedModel;

		Assert.assertEquals(
			fragmentComposition.getName(),
			importedFragmentComposition.getName());
		Assert.assertEquals(
			fragmentComposition.getDescription(),
			importedFragmentComposition.getDescription());
		Assert.assertEquals(
			fragmentComposition.getData(),
			importedFragmentComposition.getData());
		Assert.assertEquals(
			fragmentComposition.getFragmentCompositionKey(),
			importedFragmentComposition.getFragmentCompositionKey());
		Assert.assertEquals(
			fragmentComposition.getStatus(),
			importedFragmentComposition.getStatus());
	}

	private void _exportImportStagedModel(StagedModel... stagedModels)
		throws Exception {

		ExportImportThreadLocal.setPortletImportInProcess(true);

		try {
			for (StagedModel stagedModel : stagedModels) {
				exportImportStagedModel(stagedModel);
			}
		}
		finally {
			ExportImportThreadLocal.setPortletImportInProcess(false);
		}
	}

	private static final String _SAMPLE_DATA =
		"{\"version\":1,\"items\":{\"root\":{\"children\":[],\"type\":" +
			"\"root\"}},\"rootItems\":{\"main\":\"root\"}}";

	@Inject
	private FragmentCompositionLocalService _fragmentCompositionLocalService;

}