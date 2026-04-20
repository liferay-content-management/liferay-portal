/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.internal.exportimport.staged.model.repository;

import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.StagedModelModifiedDateComparator;
import com.liferay.exportimport.staged.model.repository.StagedModelRepository;
import com.liferay.exportimport.staged.model.repository.StagedModelRepositoryHelper;
import com.liferay.fragment.model.FragmentComposition;
import com.liferay.fragment.service.FragmentCompositionLocalService;
import com.liferay.portal.kernel.dao.orm.ExportActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(
	property = "model.class.name=com.liferay.fragment.model.FragmentComposition",
	service = StagedModelRepository.class
)
public class FragmentCompositionStagedModelRepository
	implements StagedModelRepository<FragmentComposition> {

	@Override
	public FragmentComposition addStagedModel(
			PortletDataContext portletDataContext,
			FragmentComposition fragmentComposition)
		throws PortalException {

		long userId = portletDataContext.getUserId(
			fragmentComposition.getUserUuid());

		ServiceContext serviceContext = portletDataContext.createServiceContext(
			fragmentComposition);

		if (portletDataContext.isDataStrategyMirror()) {
			serviceContext.setUuid(fragmentComposition.getUuid());
		}

		return _fragmentCompositionLocalService.addFragmentComposition(
			fragmentComposition.getExternalReferenceCode(), userId,
			fragmentComposition.getGroupId(),
			fragmentComposition.getFragmentCollectionId(),
			fragmentComposition.getFragmentCompositionKey(),
			fragmentComposition.getName(), fragmentComposition.getDescription(),
			fragmentComposition.getData(),
			fragmentComposition.getPreviewFileEntryId(),
			fragmentComposition.getStatus(), serviceContext);
	}

	@Override
	public void deleteStagedModel(FragmentComposition fragmentComposition)
		throws PortalException {

		_fragmentCompositionLocalService.deleteFragmentComposition(
			fragmentComposition);
	}

	@Override
	public void deleteStagedModel(
			String uuid, long groupId, String className, String extraData)
		throws PortalException {

		FragmentComposition fragmentComposition =
			fetchStagedModelByUuidAndGroupId(uuid, groupId);

		if (fragmentComposition != null) {
			deleteStagedModel(fragmentComposition);
		}
	}

	@Override
	public void deleteStagedModels(PortletDataContext portletDataContext)
		throws PortalException {
	}

	@Override
	public FragmentComposition fetchMissingReference(
		String uuid, long groupId) {

		return (FragmentComposition)
			_stagedModelRepositoryHelper.fetchMissingReference(
				uuid, groupId, this);
	}

	@Override
	public FragmentComposition fetchStagedModelByUuidAndGroupId(
		String uuid, long groupId) {

		return _fragmentCompositionLocalService.
			fetchFragmentCompositionByUuidAndGroupId(uuid, groupId);
	}

	@Override
	public List<FragmentComposition> fetchStagedModelsByUuidAndCompanyId(
		String uuid, long companyId) {

		return _fragmentCompositionLocalService.
			getFragmentCompositionsByUuidAndCompanyId(
				uuid, companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				new StagedModelModifiedDateComparator<FragmentComposition>());
	}

	@Override
	public ExportActionableDynamicQuery getExportActionableDynamicQuery(
		PortletDataContext portletDataContext) {

		return _fragmentCompositionLocalService.getExportActionableDynamicQuery(
			portletDataContext);
	}

	@Override
	public FragmentComposition getStagedModel(long id) throws PortalException {
		return _fragmentCompositionLocalService.getFragmentComposition(id);
	}

	@Override
	public FragmentComposition saveStagedModel(
			FragmentComposition fragmentComposition)
		throws PortalException {

		return _fragmentCompositionLocalService.updateFragmentComposition(
			fragmentComposition);
	}

	@Override
	public FragmentComposition updateStagedModel(
			PortletDataContext portletDataContext,
			FragmentComposition fragmentComposition)
		throws PortalException {

		return _fragmentCompositionLocalService.updateFragmentComposition(
			portletDataContext.getUserId(fragmentComposition.getUserUuid()),
			fragmentComposition.getFragmentCompositionId(),
			fragmentComposition.getFragmentCollectionId(),
			fragmentComposition.getName(), fragmentComposition.getDescription(),
			fragmentComposition.getData(),
			fragmentComposition.getPreviewFileEntryId(),
			fragmentComposition.getStatus());
	}

	@Reference
	private FragmentCompositionLocalService _fragmentCompositionLocalService;

	@Reference
	private StagedModelRepositoryHelper _stagedModelRepositoryHelper;

}