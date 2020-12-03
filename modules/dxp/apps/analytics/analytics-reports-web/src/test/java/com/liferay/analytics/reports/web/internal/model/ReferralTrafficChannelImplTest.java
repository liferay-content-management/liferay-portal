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
public class ReferralTrafficChannelImplTest {

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Test
	public void testToJSONObject() {
		ReferralTrafficChannelImpl referralTrafficChannelImpl =
			new ReferralTrafficChannelImpl(
				Collections.singletonList(
					new ReferringURL(45, "https://www.liferay.com/")),
				Arrays.asList(
					new ReferringURL(15, "https://www.liferay.com/page1"),
					new ReferringURL(4, "https://www.liferay.com/page2")),
				RandomTestUtil.randomInt(), RandomTestUtil.randomDouble());

		Assert.assertEquals(
			JSONUtil.put(
				"helpMessage", referralTrafficChannelImpl.getHelpMessageKey()
			).put(
				"name", referralTrafficChannelImpl.getName()
			).put(
				"referringDomains",
				JSONUtil.putAll(
					JSONUtil.put(
						"trafficAmount", 45
					).put(
						"url", "https://www.liferay.com/"
					))
			).put(
				"referringPages",
				JSONUtil.putAll(
					JSONUtil.put(
						"trafficAmount", 15
					).put(
						"url", "https://www.liferay.com/page1"
					),
					JSONUtil.put(
						"trafficAmount", 4
					).put(
						"url", "https://www.liferay.com/page2"
					))
			).put(
				"share",
				String.format(
					"%.1f", referralTrafficChannelImpl.getTrafficShare())
			).put(
				"title", referralTrafficChannelImpl.getName()
			).put(
				"value",
				Math.toIntExact(referralTrafficChannelImpl.getTrafficAmount())
			).toString(),
			String.valueOf(
				referralTrafficChannelImpl.toJSONObject(
					LocaleUtil.US,
					_getResourceBundle(referralTrafficChannelImpl))));
	}

	@Test
	public void testToJSONObjectWithError() {
		ReferralTrafficChannelImpl referralTrafficChannelImpl =
			new ReferralTrafficChannelImpl(true);

		Assert.assertEquals(
			JSONUtil.put(
				"helpMessage", referralTrafficChannelImpl.getHelpMessageKey()
			).put(
				"name", referralTrafficChannelImpl.getName()
			).put(
				"title", referralTrafficChannelImpl.getName()
			).toString(),
			String.valueOf(
				referralTrafficChannelImpl.toJSONObject(
					LocaleUtil.US,
					_getResourceBundle(referralTrafficChannelImpl))));
	}

	@Test
	public void testToJSONObjectWithoutReferringDomainsAndWithoutReferringPages() {
		ReferralTrafficChannelImpl referralTrafficChannelImpl =
			new ReferralTrafficChannelImpl(
				null, null, RandomTestUtil.randomInt(),
				RandomTestUtil.randomDouble());

		Assert.assertEquals(
			JSONUtil.put(
				"helpMessage", referralTrafficChannelImpl.getHelpMessageKey()
			).put(
				"name", referralTrafficChannelImpl.getName()
			).put(
				"share",
				String.format(
					"%.1f", referralTrafficChannelImpl.getTrafficShare())
			).put(
				"title", referralTrafficChannelImpl.getName()
			).put(
				"value",
				Math.toIntExact(referralTrafficChannelImpl.getTrafficAmount())
			).toString(),
			String.valueOf(
				referralTrafficChannelImpl.toJSONObject(
					LocaleUtil.US,
					_getResourceBundle(referralTrafficChannelImpl))));
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