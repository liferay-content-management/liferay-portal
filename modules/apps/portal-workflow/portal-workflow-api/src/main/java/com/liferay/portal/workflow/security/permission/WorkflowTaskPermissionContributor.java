/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.security.permission;

import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.workflow.WorkflowTask;

/**
 * Grants access to workflow tasks that the default workflow task permission
 * does not grant. A contributor can only grant access, never revoke it.
 *
 * @author Mikel Lorza
 */
public interface WorkflowTaskPermissionContributor {

	public boolean contains(
		PermissionChecker permissionChecker, WorkflowTask workflowTask,
		long groupId);

}