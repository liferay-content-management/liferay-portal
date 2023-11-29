/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.permission.template.model.impl;

import com.liferay.permission.template.model.PermissionTemplate;
import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.MVCCModel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The cache model class for representing PermissionTemplate in entity cache.
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
public class PermissionTemplateCacheModel
	implements CacheModel<PermissionTemplate>, Externalizable, MVCCModel {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof PermissionTemplateCacheModel)) {
			return false;
		}

		PermissionTemplateCacheModel permissionTemplateCacheModel =
			(PermissionTemplateCacheModel)object;

		if ((permissionTemplateId ==
				permissionTemplateCacheModel.permissionTemplateId) &&
			(mvccVersion == permissionTemplateCacheModel.mvccVersion)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, permissionTemplateId);

		return HashUtil.hash(hashCode, mvccVersion);
	}

	@Override
	public long getMvccVersion() {
		return mvccVersion;
	}

	@Override
	public void setMvccVersion(long mvccVersion) {
		this.mvccVersion = mvccVersion;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(17);

		sb.append("{mvccVersion=");
		sb.append(mvccVersion);
		sb.append(", ctCollectionId=");
		sb.append(ctCollectionId);
		sb.append(", permissionTemplateId=");
		sb.append(permissionTemplateId);
		sb.append(", groupId=");
		sb.append(groupId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", classNameId=");
		sb.append(classNameId);
		sb.append(", classPK=");
		sb.append(classPK);
		sb.append(", permissionTemplateEnabled=");
		sb.append(permissionTemplateEnabled);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public PermissionTemplate toEntityModel() {
		PermissionTemplateImpl permissionTemplateImpl =
			new PermissionTemplateImpl();

		permissionTemplateImpl.setMvccVersion(mvccVersion);
		permissionTemplateImpl.setCtCollectionId(ctCollectionId);
		permissionTemplateImpl.setPermissionTemplateId(permissionTemplateId);
		permissionTemplateImpl.setGroupId(groupId);
		permissionTemplateImpl.setCompanyId(companyId);
		permissionTemplateImpl.setClassNameId(classNameId);
		permissionTemplateImpl.setClassPK(classPK);
		permissionTemplateImpl.setPermissionTemplateEnabled(
			permissionTemplateEnabled);

		permissionTemplateImpl.resetOriginalValues();

		return permissionTemplateImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		mvccVersion = objectInput.readLong();

		ctCollectionId = objectInput.readLong();

		permissionTemplateId = objectInput.readLong();

		groupId = objectInput.readLong();

		companyId = objectInput.readLong();

		classNameId = objectInput.readLong();

		classPK = objectInput.readLong();

		permissionTemplateEnabled = objectInput.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(mvccVersion);

		objectOutput.writeLong(ctCollectionId);

		objectOutput.writeLong(permissionTemplateId);

		objectOutput.writeLong(groupId);

		objectOutput.writeLong(companyId);

		objectOutput.writeLong(classNameId);

		objectOutput.writeLong(classPK);

		objectOutput.writeBoolean(permissionTemplateEnabled);
	}

	public long mvccVersion;
	public long ctCollectionId;
	public long permissionTemplateId;
	public long groupId;
	public long companyId;
	public long classNameId;
	public long classPK;
	public boolean permissionTemplateEnabled;

}