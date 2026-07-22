/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similarity;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Mikel Lorza
 */
public class TextSimilaritySignatureUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testBlankTextYieldsNoBands() {
		Assert.assertEquals(
			0, TextSimilaritySignatureUtil.getBandSignatures(null).length);
		Assert.assertEquals(
			0, TextSimilaritySignatureUtil.getBandSignatures("").length);
		Assert.assertEquals(
			0, TextSimilaritySignatureUtil.getBandSignatures("   ").length);
	}

	@Test
	public void testBlankTextYieldsNoSignature() {
		Assert.assertEquals(
			0, TextSimilaritySignatureUtil.getSignature(null).length);
		Assert.assertEquals(
			0, TextSimilaritySignatureUtil.getSignature("").length);
		Assert.assertEquals(
			0, TextSimilaritySignatureUtil.getSignature("   ").length);
	}

	@Test
	public void testDeterministic() {
		Assert.assertArrayEquals(
			TextSimilaritySignatureUtil.getBandSignatures(_A),
			TextSimilaritySignatureUtil.getBandSignatures(_A));
	}

	@Test
	public void testDistinctTextSharesNoBands() {
		Assert.assertEquals(0, _sharedBandCount(_A, _DISTINCT));
	}

	@Test
	public void testNearDuplicateSharesMoreThanDistinct() {
		Assert.assertTrue(
			_sharedBandCount(_A, _A + " 2") > _sharedBandCount(_A, _DISTINCT));
	}

	@Test
	public void testNearDuplicateSharesMostBands() {

		// "_A" and "_A" + a trailing token differ by one shingle only, so their
		// signatures must collide in nearly every band. This is the case an
		// exact fingerprint would miss.

		int sharedBandCount = _sharedBandCount(_A, _A + " 2");

		Assert.assertTrue(
			"Near-duplicate texts must share most of the 32 bands, shared: " +
				sharedBandCount,
			sharedBandCount >= 20);
	}

	@Test
	public void testSignatureDeterministic() {
		Assert.assertArrayEquals(
			TextSimilaritySignatureUtil.getSignature(_A),
			TextSimilaritySignatureUtil.getSignature(_A));
	}

	@Test
	public void testSignatureHasFixedSize() {
		Assert.assertEquals(
			128, TextSimilaritySignatureUtil.getSignature(_A).length);
	}

	@Test
	public void testSignatureNearDuplicateMatchesMorePositionsThanDistinct() {
		Assert.assertTrue(
			_matchingPositionCount(_A, _A + " 2") > _matchingPositionCount(
				_A, _DISTINCT));
	}

	private int _matchingPositionCount(String text1, String text2) {
		Map<String, String> valuesByPosition = new HashMap<>();

		for (String token : TextSimilaritySignatureUtil.getSignature(text1)) {
			int index = token.indexOf('_');

			valuesByPosition.put(
				token.substring(0, index), token.substring(index + 1));
		}

		int count = 0;

		for (String token : TextSimilaritySignatureUtil.getSignature(text2)) {
			int index = token.indexOf('_');

			String value = valuesByPosition.get(token.substring(0, index));

			if ((value != null) && value.equals(token.substring(index + 1))) {
				count++;
			}
		}

		return count;
	}

	private int _sharedBandCount(String text1, String text2) {
		Set<String> bands = new HashSet<>();

		for (String band :
				TextSimilaritySignatureUtil.getBandSignatures(text1)) {

			bands.add(band);
		}

		int count = 0;

		for (String band :
				TextSimilaritySignatureUtil.getBandSignatures(text2)) {

			if (bands.contains(band)) {
				count++;
			}
		}

		return count;
	}

	private static final String _A =
		"If you forgot your password, go to the login page and click the " +
			"forgot password link. Enter your email address and you will " +
				"receive an email with instructions to create a new password.";

	private static final String _DISTINCT =
		"The quarterly sales report shows strong revenue growth across the " +
			"European market. Product categories in retail and wholesale " +
				"increased during the last fiscal period.";

}