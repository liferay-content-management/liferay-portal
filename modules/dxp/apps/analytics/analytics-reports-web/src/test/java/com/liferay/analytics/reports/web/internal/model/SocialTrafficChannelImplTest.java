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

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author David Arques
 */
public class SocialTrafficChannelImplTest {

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Test
	public void testToJSONObject() {
		SocialTrafficChannelImpl socialTrafficChannelImpl =
			new SocialTrafficChannelImpl(
				RandomTestUtil.randomInt(), RandomTestUtil.randomDouble());

		Assert.assertEquals(
			JSONUtil.put(
				"helpMessage", socialTrafficChannelImpl.getHelpMessageKey()
			).put(
				"name", socialTrafficChannelImpl.getName()
			).put(
				"share",
				String.format(
					"%.1f", socialTrafficChannelImpl.getTrafficShare())
			).put(
				"title", socialTrafficChannelImpl.getName()
			).put(
				"value",
				Math.toIntExact(socialTrafficChannelImpl.getTrafficAmount())
			).toString(),
			String.valueOf(
				socialTrafficChannelImpl.toJSONObject(
					LocaleUtil.US,
					_getResourceBundle(socialTrafficChannelImpl))));
	}

	@Test
	public void testToJSONObjectWithError() {
		SocialTrafficChannelImpl socialTrafficChannelImpl =
			new SocialTrafficChannelImpl(true);

		Assert.assertEquals(
			JSONUtil.put(
				"helpMessage", socialTrafficChannelImpl.getHelpMessageKey()
			).put(
				"name", socialTrafficChannelImpl.getName()
			).put(
				"title", socialTrafficChannelImpl.getName()
			).toString(),
			String.valueOf(
				socialTrafficChannelImpl.toJSONObject(
					LocaleUtil.US,
					_getResourceBundle(socialTrafficChannelImpl))));
	}

	private ResourceBundle _getResourceBundle(TrafficChannel trafficChannel) {
		return new ResourceBundle() {

			@Override
			public Enumeration<String> getKeys() {
				return Collections.enumeration(
					Arrays.asList(
						trafficChannel.getName(),
						trafficChannel.getHelpMessageKey()));
			}

			@Override
			protected Object handleGetObject(String key) {
				return key;
			}

		};
	}

}