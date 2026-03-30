/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.model.listener;

import com.liferay.asset.kernel.model.AssetVocabularyGroupRel;
import com.liferay.asset.kernel.service.AssetVocabularyGroupRelLocalService;
import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.constants.DepotRolesConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.object.constants.ObjectActionKeys;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.field.attachment.AttachmentManager;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectEntryFolder;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.ResourceAction;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.site.cms.site.initializer.internal.util.ObjectEntryFolderUtil;
import com.liferay.site.cms.site.initializer.util.CMSDefaultPermissionUtil;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mikel Lorza
 * @author Jürgen Kappler
 * @author Roberto Díaz
 */
@Component(service = ModelListener.class)
public class DepotEntryModelListener extends BaseModelListener<DepotEntry> {

	@Override
	public void onAfterCreate(DepotEntry depotEntry)
		throws ModelListenerException {

		try {
			_onAfterCreate(depotEntry);
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Override
	public void onBeforeRemove(DepotEntry depotEntry)
		throws ModelListenerException {

		try {
			_onBeforeRemove(depotEntry);
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	private void _addCMSDefaultPermissions(Group group) throws Exception {
		ObjectDefinition cmsDefaultPermissionObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_CMS_DEFAULT_PERMISSION", group.getCompanyId());

		if (cmsDefaultPermissionObjectDefinition == null) {
			return;
		}

		String[] actionIds = TransformUtil.transformToArray(
			_resourceActionLocalService.getResourceActions(
				ObjectEntryFolder.class.getName()),
			ResourceAction::getActionId, String.class);

		CMSDefaultPermissionUtil.addOrUpdateObjectEntry(
			null, group.getCompanyId(), group.getCreatorUserId(),
			group.getExternalReferenceCode(), DepotEntry.class.getName(),
			JSONUtil.put(
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS,
				_getObjectEntryDefaultPermissionJSONObject(
					group.getCompanyId(), "L_CMS_BASIC_WEB_CONTENT")
			).put(
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_FILES,
				_getObjectEntryDefaultPermissionJSONObject(
					group.getCompanyId(), "L_CMS_BASIC_DOCUMENT")
			).put(
				"OBJECT_ENTRY_FOLDERS",
				JSONUtil.put(
					DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR,
					new String[] {
						ActionKeys.ADD_ENTRY,
						ObjectActionKeys.ADD_OBJECT_ENTRY_FOLDER,
						ActionKeys.DELETE, ActionKeys.PERMISSIONS,
						ActionKeys.UPDATE, ActionKeys.SUBSCRIBE, ActionKeys.VIEW
					}
				).put(
					DepotRolesConstants.ASSET_LIBRARY_CONTENT_REVIEWER,
					new String[] {
						ActionKeys.ADD_ENTRY,
						ObjectActionKeys.ADD_OBJECT_ENTRY_FOLDER,
						ActionKeys.DELETE, ActionKeys.PERMISSIONS,
						ActionKeys.UPDATE, ActionKeys.SUBSCRIBE, ActionKeys.VIEW
					}
				).put(
					DepotRolesConstants.ASSET_LIBRARY_MEMBER,
					new String[] {
						ActionKeys.ADD_DISCUSSION, ActionKeys.VIEW,
						ActionKeys.SUBSCRIBE
					}
				).put(
					RoleConstants.CMS_ADMINISTRATOR, JSONUtil.putAll(actionIds)
				).put(
					RoleConstants.OWNER, JSONUtil.putAll(actionIds)
				).put(
					RoleConstants.USER,
					new String[] {ActionKeys.VIEW, ActionKeys.SUBSCRIBE}
				)
			),
			group.getGroupId(), StringPool.BLANK);
	}

	private void _deleteAssetVocabularyGroupRels(DepotEntry depotEntry)
		throws Exception {

		List<AssetVocabularyGroupRel> assetVocabularyGroupRels =
			_assetVocabularyGroupRelLocalService.
				getAssetVocabularyGroupRelsByGroupId(depotEntry.getGroupId());

		for (AssetVocabularyGroupRel assetVocabularyGroupRel :
				assetVocabularyGroupRels) {

			long count =
				_assetVocabularyGroupRelLocalService.
					getAssetVocabularyGroupRelsCount(
						assetVocabularyGroupRel.getVocabularyId());

			if (count == 1) {
				_assetVocabularyGroupRelLocalService.addAssetVocabularyGroupRel(
					GroupConstants.ANY_PARENT_GROUP_ID,
					assetVocabularyGroupRel.getVocabularyId());
			}
		}
	}

	private void _deleteCMSDefaultPermissions(Group group) throws Exception {
		ObjectDefinition cmsDefaultPermissionObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_CMS_DEFAULT_PERMISSION", group.getCompanyId());

		if (cmsDefaultPermissionObjectDefinition == null) {
			return;
		}

		ObjectEntry objectEntry = CMSDefaultPermissionUtil.fetchObjectEntry(
			group.getCompanyId(), group.getCreatorUserId(),
			group.getExternalReferenceCode(), DepotEntry.class.getName(),
			_filterFactory);

		if (objectEntry == null) {
			return;
		}

		_objectEntryLocalService.deleteObjectEntry(
			objectEntry.getObjectEntryId());
	}

	private JSONObject _getObjectEntryDefaultPermissionJSONObject(
			long companyId, String externalReferenceCode)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					externalReferenceCode, companyId);

		String[] actionIds = TransformUtil.transformToArray(
			_resourceActionLocalService.getResourceActions(
				objectDefinition.getClassName()),
			ResourceAction::getActionId, String.class);

		return JSONUtil.put(
			DepotRolesConstants.ASSET_LIBRARY_ADMINISTRATOR,
			new String[] {
				ActionKeys.ADD_DISCUSSION, ActionKeys.DELETE,
				ActionKeys.DELETE_DISCUSSION, ActionKeys.PERMISSIONS,
				ActionKeys.UPDATE, ActionKeys.UPDATE_DISCUSSION, ActionKeys.VIEW
			}
		).put(
			DepotRolesConstants.ASSET_LIBRARY_CONTENT_REVIEWER,
			new String[] {
				ActionKeys.ADD_DISCUSSION, ActionKeys.DELETE,
				ActionKeys.DELETE_DISCUSSION, ActionKeys.PERMISSIONS,
				ActionKeys.UPDATE, ActionKeys.UPDATE_DISCUSSION, ActionKeys.VIEW
			}
		).put(
			DepotRolesConstants.ASSET_LIBRARY_MEMBER,
			new String[] {
				ActionKeys.ADD_DISCUSSION,
				ObjectActionKeys.OBJECT_ENTRY_HISTORY, ActionKeys.VIEW
			}
		).put(
			RoleConstants.CMS_ADMINISTRATOR, actionIds
		).put(
			RoleConstants.OWNER, actionIds
		).put(
			RoleConstants.USER, new String[] {ActionKeys.VIEW}
		);
	}

	private void _onAfterCreate(DepotEntry depotEntry) throws Exception {
		if (!FeatureFlagManagerUtil.isEnabled(
				depotEntry.getCompanyId(), "LPD-17564") ||
			(depotEntry.getType() != DepotConstants.TYPE_SPACE)) {

			return;
		}

		_addCMSDefaultPermissions(depotEntry.getGroup());

		ObjectEntryFolderUtil.addObjectEntryFolders(
			depotEntry, _attachmentManager);
	}

	private void _onBeforeRemove(DepotEntry depotEntry) throws Exception {
		if (depotEntry.getType() != DepotConstants.TYPE_SPACE) {
			return;
		}

		ObjectEntryFolderUtil.deleteObjectEntryFolders(depotEntry);
		_deleteAssetVocabularyGroupRels(depotEntry);
		_deleteCMSDefaultPermissions(depotEntry.getGroup());
	}

	@Reference
	private AssetVocabularyGroupRelLocalService
		_assetVocabularyGroupRelLocalService;

	@Reference
	private AttachmentManager _attachmentManager;

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ResourceActionLocalService _resourceActionLocalService;

}