/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.analytics.reports.web.internal.model;

import com.liferay.analytics.reports.web.internal.model.util.TrafficChannelUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @author David Arques
 */
public class SocialTrafficChannelImpl implements TrafficChannel {

	public SocialTrafficChannelImpl(boolean error) {
		_error = error;
		_trafficAmount = 0;
		_trafficShare = 0;
	}

	public SocialTrafficChannelImpl(long trafficAmount, double trafficShare) {
		_error = false;
		_trafficAmount = trafficAmount;
		_trafficShare = trafficShare;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof SocialTrafficChannelImpl)) {
			return false;
		}

		SocialTrafficChannelImpl socialTrafficChannelImpl =
			(SocialTrafficChannelImpl)object;

		if (Objects.equals(
				getHelpMessageKey(),
				socialTrafficChannelImpl.getHelpMessageKey()) &&
			Objects.equals(getName(), socialTrafficChannelImpl.getName()) &&
			Objects.equals(
				_trafficAmount, socialTrafficChannelImpl._trafficAmount) &&
			Objects.equals(
				_trafficShare, socialTrafficChannelImpl._trafficShare)) {

			return true;
		}

		return false;
	}

	@Override
	public String getHelpMessageKey() {
		return "this-is-the-number-of-page-views-generated-by-people-coming-" +
			"to-your-page-from-social-sites";
	}

	@Override
	public String getName() {
		return "social";
	}

	@Override
	public long getTrafficAmount() {
		return _trafficAmount;
	}

	@Override
	public double getTrafficShare() {
		return _trafficShare;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			getHelpMessageKey(), getName(), _trafficAmount, _trafficShare);
	}

	@Override
	public JSONObject toJSONObject(
		Locale locale, ResourceBundle resourceBundle) {

		return TrafficChannelUtil.toJSONObject(
			_error,
			ResourceBundleUtil.getString(resourceBundle, getHelpMessageKey()),
			getName(), ResourceBundleUtil.getString(resourceBundle, getName()),
			_trafficAmount, _trafficShare);
	}

	@Override
	public String toString() {
		return String.valueOf(
			TrafficChannelUtil.toJSONObject(
				_error, getHelpMessageKey(), getName(), getName(),
				_trafficAmount, _trafficShare));
	}

	private final boolean _error;
	private final long _trafficAmount;
	private final double _trafficShare;

}