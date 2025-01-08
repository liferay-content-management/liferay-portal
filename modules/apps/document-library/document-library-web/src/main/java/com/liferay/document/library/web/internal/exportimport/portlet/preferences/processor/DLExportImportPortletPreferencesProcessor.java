/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.exportimport.portlet.preferences.processor;

import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.model.DLFileShortcut;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.exportimport.kernel.lar.ExportImportHelper;
import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.PortletDataException;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.exportimport.kernel.lar.PortletDataHandlerKeys;
import com.liferay.exportimport.kernel.lar.StagedModelDataHandlerUtil;
import com.liferay.exportimport.kernel.staging.MergeLayoutPrototypesThreadLocal;
import com.liferay.exportimport.kernel.staging.StagingURLHelperUtil;
import com.liferay.exportimport.portlet.preferences.processor.Capability;
import com.liferay.exportimport.portlet.preferences.processor.ExportImportPortletPreferencesProcessor;
import com.liferay.exportimport.staged.model.repository.StagedModelRepository;
import com.liferay.exportimport.staged.model.repository.StagedModelRepositoryRegistryUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.lang.ThreadContextClassLoaderUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.auth.HttpPrincipal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RepositoryLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.service.http.GroupServiceHttp;
import com.liferay.portlet.documentlibrary.constants.DLConstants;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Máté Thurzó
 */
@Component(
	property = "javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY,
	service = ExportImportPortletPreferencesProcessor.class
)
public class DLExportImportPortletPreferencesProcessor
	implements ExportImportPortletPreferencesProcessor {

	@Override
	public List<Capability> getExportCapabilities() {
		return ListUtil.fromArray(
			_dlCommentsAndRatingsExporterImporterCapability, _exportCapability);
	}

	@Override
	public List<Capability> getImportCapabilities() {
		return ListUtil.fromArray(
			_dlCommentsAndRatingsExporterImporterCapability, _importCapability);
	}

	@Override
	public PortletPreferences processExportPortletPreferences(
			PortletDataContext portletDataContext,
			PortletPreferences portletPreferences)
		throws PortletDataException {

		if (!MapUtil.getBoolean(
				portletDataContext.getParameterMap(),
				PortletDataHandlerKeys.PORTLET_DATA) &&
			MergeLayoutPrototypesThreadLocal.isInProgress()) {

			return portletPreferences;
		}

		// Root folder ID is set, only export that

		long rootFolderId = GetterUtil.getLong(
			portletPreferences.getValue("rootFolderId", null));

		// Root folder ID is set, only export that

		if (FeatureFlagManagerUtil.isEnabled(
				portletDataContext.getCompanyId(), "LPD-27566")) {

			String rootFolderExternalReferenceCode =
				portletPreferences.getValue(
					"rootFolderExternalReferenceCode", null);

			if (rootFolderExternalReferenceCode == null) {
				rootFolderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;
			}
			else {
				String repositoryGroupExternalReferenceCode =
					portletPreferences.getValue(
						"repositoryGroupExternalReferenceCode", null);

				Group group =
					_groupLocalService.fetchGroupByExternalReferenceCode(
						repositoryGroupExternalReferenceCode,
						portletDataContext.getCompanyId());

				DLFolder dlFolder =
					_dlFolderLocalService.fetchDLFolderByExternalReferenceCode(
						rootFolderExternalReferenceCode, group.getGroupId());

				if (dlFolder != null) {
					rootFolderId = dlFolder.getFolderId();
				}
			}
		}

		if (rootFolderId != DLFolderConstants.DEFAULT_PARENT_FOLDER_ID) {
			try {
				Folder folder = _getFolder(rootFolderId, portletDataContext);

				if (folder != null) {
					if (!FeatureFlagManagerUtil.isEnabled(
							portletDataContext.getCompanyId(), "LPD-27566")) {

						portletPreferences.setValue(
							"selectedRepositoryId",
							String.valueOf(folder.getRepositoryId()));

						if ((folder.getGroupId() ==
								portletDataContext.getGroupId()) ||
							!ExportImportThreadLocal.isStagingInProcess()) {

							StagedModelDataHandlerUtil.
								exportReferenceStagedModel(
									portletDataContext,
									portletDataContext.getPortletId(), folder);
						}
						else {
							_saveStagingPreferencesMapping(
								folder.getRepositoryId(), folder.getUuid(),
								portletDataContext);
						}
					}
					else {
						String selectedRepositoryExternalReferenceCode =
							StringPool.BLANK;

						Repository repository =
							_repositoryLocalService.fetchRepository(
								folder.getRepositoryId());

						if (repository != null) {
							selectedRepositoryExternalReferenceCode =
								repository.getExternalReferenceCode();
						}

						portletPreferences.setValue(
							"selectedRepositoryExternalReferenceCode",
							selectedRepositoryExternalReferenceCode);

						if ((folder.getGroupId() ==
								portletDataContext.getGroupId()) ||
							!ExportImportThreadLocal.isStagingInProcess()) {

							StagedModelDataHandlerUtil.
								exportReferenceStagedModel(
									portletDataContext,
									portletDataContext.getPortletId(), folder);
						}
						else {
							Group group = _groupLocalService.getGroup(
								folder.getGroupId());

							_saveStagingPreferencesMapping(
								selectedRepositoryExternalReferenceCode,
								group.getExternalReferenceCode(),
								folder.getExternalReferenceCode(),
								portletDataContext);
						}
					}
				}

				if (FeatureFlagManagerUtil.isEnabled(
						portletDataContext.getCompanyId(), "LPD-27566")) {

					String repositoryGroupExternalReferenceCode =
						portletPreferences.getValue(
							"repositoryGroupExternalReferenceCode", null);

					portletPreferences.setValue(
						"repositoryGroupExternalReferenceCode",
						_getGroupExportPortletPreferencesExternalReferenceCode(
							portletDataContext,
							repositoryGroupExternalReferenceCode));
				}

				return portletPreferences;
			}
			catch (ReadOnlyException readOnlyException) {
				throw new PortletDataException(
					"Unable to update portlet preferences during import",
					readOnlyException);
			}
			catch (PortalException portalException) {
				throw new RuntimeException(portalException);
			}
		}

		if (!FeatureFlagManagerUtil.isEnabled(
				portletDataContext.getCompanyId(), "LPD-27566")) {

			long selectedRepositoryId = GetterUtil.getLong(
				portletPreferences.getValue("selectedRepositoryId", null));

			if (!_exportImportHelper.isExportPortletData(portletDataContext) ||
				(selectedRepositoryId != portletDataContext.getGroupId())) {

				if (ExportImportThreadLocal.isStagingInProcess() &&
					(selectedRepositoryId > 0)) {

					_saveStagingPreferencesMapping(
						selectedRepositoryId, null, portletDataContext);
				}

				return portletPreferences;
			}
		}
		else {
			String repositoryGroupExternalReferenceCode =
				portletPreferences.getValue(
					"repositoryGroupExternalReferenceCode", null);

			if (repositoryGroupExternalReferenceCode != null) {
				Group group =
					_groupLocalService.fetchGroupByExternalReferenceCode(
						repositoryGroupExternalReferenceCode,
						portletDataContext.getCompanyId());

				if (!_exportImportHelper.isExportPortletData(
						portletDataContext) ||
					(group.getGroupId() != portletDataContext.getGroupId())) {

					if (ExportImportThreadLocal.isStagingInProcess() &&
						(repositoryGroupExternalReferenceCode !=
							StringPool.BLANK)) {

						String selectedRepositoryExternalReferenceCode =
							portletPreferences.getValue(
								"selectedRepositoryExternalReferenceCode",
								null);

						_saveStagingPreferencesMapping(
							selectedRepositoryExternalReferenceCode,
							repositoryGroupExternalReferenceCode, null,
							portletDataContext);
					}

					return portletPreferences;
				}
			}
		}

		// Root folder ID is not set, we need to export everything

		try {
			portletDataContext.addPortletPermissions(DLConstants.RESOURCE_NAME);
		}
		catch (PortalException portalException) {
			PortletDataException portletDataException =
				new PortletDataException(portalException);

			portletDataException.setPortletId(DLPortletKeys.DOCUMENT_LIBRARY);
			portletDataException.setType(
				PortletDataException.EXPORT_PORTLET_PERMISSIONS);

			throw portletDataException;
		}

		try {
			if (portletDataContext.getBooleanParameter(
					_dlPortletDataHandler.getNamespace(), "folders")) {

				StagedModelRepository<?> stagedModelRepository =
					StagedModelRepositoryRegistryUtil.getStagedModelRepository(
						DLFolder.class.getName());

				ActionableDynamicQuery folderActionableDynamicQuery =
					stagedModelRepository.getExportActionableDynamicQuery(
						portletDataContext);

				folderActionableDynamicQuery.setPerformActionMethod(
					(DLFolder dlFolder) -> {
						if (dlFolder.isInTrash()) {
							return;
						}

						StagedModelDataHandlerUtil.exportReferenceStagedModel(
							portletDataContext,
							portletDataContext.getPortletId(),
							_dlAppLocalService.getFolder(
								dlFolder.getFolderId()));
					});

				folderActionableDynamicQuery.performActions();
			}

			if (portletDataContext.getBooleanParameter(
					_dlPortletDataHandler.getNamespace(), "documents")) {

				StagedModelRepository<?> stagedModelRepository =
					StagedModelRepositoryRegistryUtil.getStagedModelRepository(
						DLFileEntry.class.getName());

				ActionableDynamicQuery fileEntryActionableDynamicQuery =
					stagedModelRepository.getExportActionableDynamicQuery(
						portletDataContext);

				fileEntryActionableDynamicQuery.setPerformActionMethod(
					(DLFileEntry dlFileEntry) ->
						StagedModelDataHandlerUtil.exportReferenceStagedModel(
							portletDataContext,
							portletDataContext.getPortletId(),
							_dlAppLocalService.getFileEntry(
								dlFileEntry.getFileEntryId())));

				fileEntryActionableDynamicQuery.performActions();
			}

			if (portletDataContext.getBooleanParameter(
					_dlPortletDataHandler.getNamespace(), "document-types")) {

				StagedModelRepository<?> stagedModelRepository =
					StagedModelRepositoryRegistryUtil.getStagedModelRepository(
						DLFileEntryType.class.getName());

				ActionableDynamicQuery fileEntryTypeActionableDynamicQuery =
					stagedModelRepository.getExportActionableDynamicQuery(
						portletDataContext);

				fileEntryTypeActionableDynamicQuery.setPerformActionMethod(
					(DLFileEntryType dlFileEntryType) -> {
						if (dlFileEntryType.isExportable()) {
							StagedModelDataHandlerUtil.
								exportReferenceStagedModel(
									portletDataContext,
									portletDataContext.getPortletId(),
									dlFileEntryType);
						}
					});

				fileEntryTypeActionableDynamicQuery.performActions();
			}

			if (portletDataContext.getBooleanParameter(
					_dlPortletDataHandler.getNamespace(), "repositories")) {

				StagedModelRepository<?> stagedModelRepository =
					StagedModelRepositoryRegistryUtil.getStagedModelRepository(
						Repository.class.getName());

				ActionableDynamicQuery repositoryActionableDynamicQuery =
					stagedModelRepository.getExportActionableDynamicQuery(
						portletDataContext);

				repositoryActionableDynamicQuery.setPerformActionMethod(
					(Repository repository) ->
						StagedModelDataHandlerUtil.exportReferenceStagedModel(
							portletDataContext,
							portletDataContext.getPortletId(), repository));

				repositoryActionableDynamicQuery.performActions();
			}

			if (portletDataContext.getBooleanParameter(
					_dlPortletDataHandler.getNamespace(), "shortcuts")) {

				StagedModelRepository<?> stagedModelRepository =
					StagedModelRepositoryRegistryUtil.getStagedModelRepository(
						DLFileShortcut.class.getName());

				ActionableDynamicQuery fileShortcutActionableDynamicQuery =
					stagedModelRepository.getExportActionableDynamicQuery(
						portletDataContext);

				fileShortcutActionableDynamicQuery.setPerformActionMethod(
					(DLFileShortcut dlFileShortcut) ->
						StagedModelDataHandlerUtil.exportReferenceStagedModel(
							portletDataContext,
							portletDataContext.getPortletId(),
							_dlAppLocalService.getFileShortcut(
								dlFileShortcut.getFileShortcutId())));

				fileShortcutActionableDynamicQuery.performActions();
			}
		}
		catch (PortalException portalException) {
			PortletDataException portletDataException =
				new PortletDataException(portalException);

			portletDataException.setPortletId(DLPortletKeys.DOCUMENT_LIBRARY);
			portletDataException.setType(
				PortletDataException.EXPORT_PORTLET_DATA);

			throw portletDataException;
		}

		return portletPreferences;
	}

	@Override
	public PortletPreferences processImportPortletPreferences(
			PortletDataContext portletDataContext,
			PortletPreferences portletPreferences)
		throws PortletDataException {

		JSONObject stagingPreferencesMappingJSONObject =
			_fetchStagingPreferencesMappingJSONObject(portletDataContext);

		if (stagingPreferencesMappingJSONObject != null) {
			try {
				if (!FeatureFlagManagerUtil.isEnabled(
						portletDataContext.getCompanyId(), "LPD-27566")) {

					long folderRepositoryId =
						stagingPreferencesMappingJSONObject.getLong(
							"folderRepositoryId");
					String folderUuid =
						stagingPreferencesMappingJSONObject.getString(
							"folderUuid");

					long folderId = DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;

					if (Validator.isNotNull(folderUuid)) {
						DLFolder dlFolder =
							_dlFolderLocalService.getDLFolderByUuidAndGroupId(
								folderUuid, folderRepositoryId);

						folderId = dlFolder.getFolderId();
					}

					portletPreferences.setValue(
						"rootFolderId", String.valueOf(folderId));
					portletPreferences.setValue(
						"selectedRepositoryId",
						String.valueOf(folderRepositoryId));

					return portletPreferences;
				}

				String folderExternalReferenceCode =
					stagingPreferencesMappingJSONObject.getString(
						"folderExternalReferenceCode");

				String repositoryExternalReferenceCode =
					stagingPreferencesMappingJSONObject.getString(
						"repositoryExternalReferenceCode");

				String repositoryGroupExternalReferenceCode =
					stagingPreferencesMappingJSONObject.getString(
						"repositoryGroupExternalReferenceCode");

				portletPreferences.setValue(
					"folderExternalReferenceCode", folderExternalReferenceCode);
				portletPreferences.setValue(
					"selectedRepositoryExternalReferenceCode",
					repositoryExternalReferenceCode);
				portletPreferences.setValue(
					"repositoryGroupExternalReferenceCode",
					repositoryGroupExternalReferenceCode);

				return portletPreferences;
			}
			catch (PortalException | ReadOnlyException exception) {
				throw new PortletDataException(exception);
			}
		}

		// Root folder ID is set, only import that

		if (!FeatureFlagManagerUtil.isEnabled(
				portletDataContext.getCompanyId(), "LPD-27566")) {

			long rootFolderId = GetterUtil.getLong(
				portletPreferences.getValue("rootFolderId", null));

			if (rootFolderId != DLFolderConstants.DEFAULT_PARENT_FOLDER_ID) {
				Element foldersElement =
					portletDataContext.getImportDataGroupElement(
						DLFolder.class);

				List<Element> folderElements = foldersElement.elements();

				if (!folderElements.isEmpty()) {
					try {
						StagedModelDataHandlerUtil.importStagedModel(
							portletDataContext, folderElements.get(0));

						Map<Long, Long> folderIds =
							(Map<Long, Long>)
								portletDataContext.getNewPrimaryKeysMap(
									Folder.class +
										".folderIdsAndRepositoryEntryIds");

						long importedRootFolderId = MapUtil.getLong(
							folderIds, rootFolderId, rootFolderId);

						portletPreferences.setValue(
							"rootFolderId",
							String.valueOf(importedRootFolderId));

						Folder folder = _getFolder(
							importedRootFolderId, portletDataContext);

						if (folder != null) {
							portletPreferences.setValue(
								"selectedRepositoryId",
								String.valueOf(folder.getRepositoryId()));
						}

						return portletPreferences;
					}
					catch (ReadOnlyException readOnlyException) {
						throw new PortletDataException(
							"Unable to update portlet preferences during " +
								"import",
							readOnlyException);
					}
				}
			}

			try {
				long selectedRepositoryId = GetterUtil.getLong(
					portletPreferences.getValue("selectedRepositoryId", null));

				if (selectedRepositoryId ==
						portletDataContext.getSourceGroupId()) {

					portletPreferences.setValue(
						"selectedRepositoryId",
						String.valueOf(portletDataContext.getGroupId()));
				}
			}
			catch (ReadOnlyException readOnlyException) {
				throw new PortletDataException(
					"Unable to update portlet preferences during import",
					readOnlyException);
			}
		}

		// Root folder is not set, need to import everything

		try {
			portletDataContext.importPortletPermissions(
				DLConstants.RESOURCE_NAME);
		}
		catch (PortalException portalException) {
			PortletDataException portletDataException =
				new PortletDataException(portalException);

			portletDataException.setPortletId(DLPortletKeys.DOCUMENT_LIBRARY);
			portletDataException.setType(
				PortletDataException.IMPORT_PORTLET_PERMISSIONS);

			throw portletDataException;
		}

		if (portletDataContext.getBooleanParameter(
				_dlPortletDataHandler.getNamespace(), "folders")) {

			Element foldersElement =
				portletDataContext.getImportDataGroupElement(DLFolder.class);

			for (Element folderElement : foldersElement.elements()) {
				StagedModelDataHandlerUtil.importStagedModel(
					portletDataContext, folderElement);
			}
		}

		if (portletDataContext.getBooleanParameter(
				_dlPortletDataHandler.getNamespace(), "documents")) {

			Element fileEntriesElement =
				portletDataContext.getImportDataGroupElement(DLFileEntry.class);

			for (Element fileEntryElement : fileEntriesElement.elements()) {
				StagedModelDataHandlerUtil.importStagedModel(
					portletDataContext, fileEntryElement);
			}
		}

		if (portletDataContext.getBooleanParameter(
				_dlPortletDataHandler.getNamespace(), "document-types")) {

			Element fileEntryTypesElement =
				portletDataContext.getImportDataGroupElement(
					DLFileEntryType.class);

			for (Element fileEntryTypeElement :
					fileEntryTypesElement.elements()) {

				StagedModelDataHandlerUtil.importStagedModel(
					portletDataContext, fileEntryTypeElement);
			}
		}

		if (portletDataContext.getBooleanParameter(
				_dlPortletDataHandler.getNamespace(), "repositories")) {

			Element repositoriesElement =
				portletDataContext.getImportDataGroupElement(Repository.class);

			for (Element repositoryElement : repositoriesElement.elements()) {
				StagedModelDataHandlerUtil.importStagedModel(
					portletDataContext, repositoryElement);
			}
		}

		if (portletDataContext.getBooleanParameter(
				_dlPortletDataHandler.getNamespace(), "shortcuts")) {

			Element fileShortcutsElement =
				portletDataContext.getImportDataGroupElement(
					DLFileShortcut.class);

			for (Element fileShortcutElement :
					fileShortcutsElement.elements()) {

				StagedModelDataHandlerUtil.importStagedModel(
					portletDataContext, fileShortcutElement);
			}
		}

		return portletPreferences;
	}

	private JSONObject _fetchStagingPreferencesMappingJSONObject(
			PortletDataContext portletDataContext)
		throws PortletDataException {

		try {
			String stagingPreferencesMappingJSON =
				portletDataContext.getZipEntryAsString(
					String.format(
						"%s/staging-preferences-mapping.json",
						portletDataContext.getPortletId()));

			if (Validator.isNull(stagingPreferencesMappingJSON)) {
				return null;
			}

			return _jsonFactory.createJSONObject(stagingPreferencesMappingJSON);
		}
		catch (JSONException jsonException) {
			throw new PortletDataException(jsonException);
		}
	}

	private Folder _getFolder(
			long folderId, PortletDataContext portletDataContext)
		throws PortletDataException {

		Folder folder = null;

		try {
			folder = _dlAppLocalService.getFolder(folderId);

			DLFolder dlFolder = _dlFolderLocalService.getDLFolder(folderId);

			if (dlFolder.isInTrash()) {
				return null;
			}
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Portlet ", portletDataContext.getPortletId(),
						" refers to an invalid root folder ID ", folderId),
					portalException);
			}
		}

		return folder;
	}

	private String _getGroupExportPortletPreferencesExternalReferenceCode(
		PortletDataContext portletDataContext, String externalReferenceCode) {

		Group group = _groupLocalService.fetchGroupByExternalReferenceCode(
			externalReferenceCode, portletDataContext.getCompanyId());

		if (group == null) {
			return externalReferenceCode;
		}

		if (ExportImportThreadLocal.isStagingInProcess() &&
			group.isStagedRemotely()) {

			UnicodeProperties typeSettingsUnicodeProperties =
				group.getTypeSettingsProperties();

			String remoteGroupExternalReferenceCode =
				typeSettingsUnicodeProperties.get(
					"remoteGroupExternalReferenceCode");

			if (Validator.isNull(remoteGroupExternalReferenceCode)) {
				remoteGroupExternalReferenceCode =
					_getRemoteGroupExternalReferenceCode(
						typeSettingsUnicodeProperties);
			}

			if (Validator.isNotNull(remoteGroupExternalReferenceCode)) {
				externalReferenceCode = remoteGroupExternalReferenceCode;
			}
		}

		if (!group.isStagingGroup()) {
			return externalReferenceCode;
		}

		Group liveGroup = _groupLocalService.fetchGroup(group.getLiveGroupId());

		if (liveGroup == null) {
			return externalReferenceCode;
		}

		return liveGroup.getExternalReferenceCode();
	}

	private long _getMirrorRepositoryId(long repositoryId) {
		Group group = _groupLocalService.fetchGroup(repositoryId);

		if (group == null) {
			return repositoryId;
		}

		Group stagingGroup = group.getStagingGroup();

		if (stagingGroup != null) {
			return stagingGroup.getGroupId();
		}

		long liveGroupId = group.getLiveGroupId();

		if (group.isStagedRemotely()) {
			liveGroupId = group.getRemoteLiveGroupId();
		}

		if (liveGroupId == GroupConstants.DEFAULT_LIVE_GROUP_ID) {
			liveGroupId = group.getGroupId();
		}

		return liveGroupId;
	}

	private String _getRemoteGroupExternalReferenceCode(
		UnicodeProperties typeSettingsUnicodeProperties) {

		String remoteAddress = GetterUtil.getString(
			typeSettingsUnicodeProperties.get("remoteAddress"));
		long remoteGroupId = GetterUtil.getLong(
			typeSettingsUnicodeProperties.get("remoteGroupId"));

		if (Validator.isNull(remoteAddress) || (remoteGroupId <= 0)) {
			return null;
		}

		int remotePort = GetterUtil.getInteger(
			typeSettingsUnicodeProperties.get("remotePort"));
		String remotePathContext = GetterUtil.getString(
			typeSettingsUnicodeProperties.get("remotePathContext"));
		boolean secureConnection = GetterUtil.getBoolean(
			typeSettingsUnicodeProperties.get("secureConnection"));

		String remoteURL = StagingURLHelperUtil.buildRemoteURL(
			remoteAddress, remotePort, remotePathContext, secureConnection);

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		User user = permissionChecker.getUser();

		try {
			HttpPrincipal httpPrincipal = new HttpPrincipal(
				remoteURL, user.getLogin(), user.getPassword(),
				user.isPasswordEncrypted());

			try (SafeCloseable safeCloseable =
					ThreadContextClassLoaderUtil.swap(
						PortalClassLoaderUtil.getClassLoader())) {

				Group group = GroupServiceHttp.getGroup(
					httpPrincipal, remoteGroupId);

				return group.getExternalReferenceCode();
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		return null;
	}

	private void _saveStagingPreferencesMapping(
		long folderRepositoryId, String folderUuid,
		PortletDataContext portletDataContext) {

		if (ExportImportThreadLocal.isStagingInProcess()) {
			portletDataContext.addZipEntry(
				String.format(
					"%s/staging-preferences-mapping.json",
					portletDataContext.getPortletId()),
				JSONUtil.put(
					"folderRepositoryId",
					_getMirrorRepositoryId(folderRepositoryId)
				).put(
					"folderUuid", folderUuid
				).toString());
		}
	}

	private void _saveStagingPreferencesMapping(
		String repositoryExternalReferenceCode,
		String repositoryGroupExternalReferenceCode,
		String folderExternalReferenceCode,
		PortletDataContext portletDataContext) {

		if (ExportImportThreadLocal.isStagingInProcess()) {
			portletDataContext.addZipEntry(
				String.format(
					"%s/staging-preferences-mapping.json",
					portletDataContext.getPortletId()),
				JSONUtil.put(
					"folderExternalReferenceCode", folderExternalReferenceCode
				).put(
					"repositoryExternalReferenceCode",
					repositoryExternalReferenceCode
				).put(
					"repositoryGroupExternalReferenceCode",
					_getGroupExportPortletPreferencesExternalReferenceCode(
						portletDataContext,
						repositoryGroupExternalReferenceCode)
				).toString());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DLExportImportPortletPreferencesProcessor.class);

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference(
		target = "(component.name=com.liferay.document.library.web.internal.exportimport.portlet.preferences.processor.DLCommentsAndRatingsExporterImporterCapability)"
	)
	private Capability _dlCommentsAndRatingsExporterImporterCapability;

	@Reference
	private DLFolderLocalService _dlFolderLocalService;

	@Reference(
		target = "(javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY + ")"
	)
	private PortletDataHandler _dlPortletDataHandler;

	@Reference(target = "(name=PortletDisplayTemplateExporter)")
	private Capability _exportCapability;

	@Reference
	private ExportImportHelper _exportImportHelper;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference(target = "(name=PortletDisplayTemplateImporter)")
	private Capability _importCapability;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private RepositoryLocalService _repositoryLocalService;

}