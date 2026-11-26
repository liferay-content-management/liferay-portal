/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.metrics.sla.task;

import java.util.Date;

/**
 * @author Mikel Lorza
 */
public class WorkflowMetricsSLATaskResult {

	public WorkflowMetricsSLATaskResult(
		boolean breached, Date overdueDate, long slaDefinitionId) {

		_breached = breached;
		_overdueDate = overdueDate;
		_slaDefinitionId = slaDefinitionId;
	}

	public Date getOverdueDate() {
		return _overdueDate;
	}

	public long getSLADefinitionId() {
		return _slaDefinitionId;
	}

	public boolean isBreached() {
		return _breached;
	}

	private final boolean _breached;
	private final Date _overdueDate;
	private final long _slaDefinitionId;

}