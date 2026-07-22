/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.similarity;

import com.liferay.petra.string.StringBundler;

import java.util.HashSet;
import java.util.Set;

/**
 * Computes MinHash/LSH band signatures for a text so that near-duplicate
 * content can be grouped by an aggregation over an indexed keyword field,
 * without any per-document similarity query at read time.
 *
 * <p>
 * The text is reduced to a set of word shingles, condensed to a fixed-length
 * MinHash signature (an estimator of the Jaccard similarity between two shingle
 * sets), and split into LSH bands. Two texts that share at least one band token
 * are near-duplicate candidates; the higher their overlap, the more bands they
 * share. The {@code (bands, rows)} split controls the overlap threshold at which
 * two texts start sharing a band.
 * </p>
 *
 * <p>
 * The computation is deterministic: the same text always yields the same band
 * tokens across nodes and across reindexes, which is required for the tokens to
 * be comparable in the index.
 * </p>
 *
 * @author Mikel Lorza
 */
public class TextSimilaritySignatureUtil {

	/**
	 * Returns the LSH band tokens for the given text, to be indexed as a
	 * multi-valued keyword field. Returns an empty array when the text is blank
	 * or too short to yield any shingle.
	 */
	public static String[] getBandSignatures(String text) {
		if (text == null) {
			return new String[0];
		}

		Set<String> shingles = _getShingles(text);

		if (shingles.isEmpty()) {
			return new String[0];
		}

		long[] signature = _getMinHashSignature(shingles);

		String[] bands = new String[_BANDS];

		for (int band = 0; band < _BANDS; band++) {
			long bandHash = _FNV_OFFSET_BASIS;

			for (int row = 0; row < _ROWS; row++) {
				bandHash = _mix(bandHash, signature[(band * _ROWS) + row]);
			}

			bands[band] = StringBundler.concat(
				"b", band, "_", Long.toHexString(bandHash));
		}

		return bands;
	}

	/**
	 * Returns the raw MinHash signature for the given text as position-prefixed
	 * keyword tokens ({@code "p" + position + "_" + value}), to be indexed as a
	 * multi-valued keyword field. The prefix keeps each position identifiable
	 * regardless of
	 * the order the index returns the values in, so the fraction of matching
	 * positions between two signatures estimates their Jaccard similarity.
	 * Returns an empty array when the text is blank or too short to yield any
	 * shingle.
	 */
	public static String[] getSignature(String text) {
		if (text == null) {
			return new String[0];
		}

		Set<String> shingles = _getShingles(text);

		if (shingles.isEmpty()) {
			return new String[0];
		}

		long[] signature = _getMinHashSignature(shingles);

		String[] tokens = new String[_HASH_COUNT];

		for (int i = 0; i < _HASH_COUNT; i++) {
			tokens[i] = StringBundler.concat("p", i, "_", signature[i]);
		}

		return tokens;
	}

	private static long _fnv1a(String value) {
		long hash = _FNV_OFFSET_BASIS;

		for (int i = 0; i < value.length(); i++) {
			hash ^= value.charAt(i);
			hash *= _FNV_PRIME;
		}

		return hash & Long.MAX_VALUE;
	}

	private static long[] _getMinHashSignature(Set<String> shingles) {
		long[] signature = new long[_HASH_COUNT];

		for (int i = 0; i < _HASH_COUNT; i++) {
			signature[i] = Long.MAX_VALUE;
		}

		for (String shingle : shingles) {
			long baseHash = _fnv1a(shingle);

			for (int i = 0; i < _HASH_COUNT; i++) {

				// Derive each permutation from one base hash via a distinct
				// linear transform (a * h + b) mod prime, instead of hashing
				// every shingle once per permutation.

				long permuted =
					((_PERMUTATION_A[i] * baseHash) + _PERMUTATION_B[i]) %
						_MERSENNE_PRIME;

				if (permuted < 0) {
					permuted += _MERSENNE_PRIME;
				}

				if (permuted < signature[i]) {
					signature[i] = permuted;
				}
			}
		}

		return signature;
	}

	private static Set<String> _getShingles(String text) {
		Set<String> shingles = new HashSet<>();

		String[] tokens = text.toLowerCase(
		).replaceAll(
			"[^\\p{L}\\p{Nd}]+", " "
		).trim(
		).split(
			"\\s+"
		);

		if ((tokens.length == 1) && tokens[0].isEmpty()) {
			return shingles;
		}

		if (tokens.length < _SHINGLE_SIZE) {
			for (String token : tokens) {
				shingles.add(token);
			}

			return shingles;
		}

		for (int i = 0; i <= (tokens.length - _SHINGLE_SIZE); i++) {
			StringBuilder sb = new StringBuilder();

			for (int j = 0; j < _SHINGLE_SIZE; j++) {
				if (j > 0) {
					sb.append(' ');
				}

				sb.append(tokens[i + j]);
			}

			shingles.add(sb.toString());
		}

		return shingles;
	}

	private static long _mix(long hash, long value) {
		for (int shift = 0; shift < 64; shift += 8) {
			hash ^= (value >>> shift) & 0xff;
			hash *= _FNV_PRIME;
		}

		return hash;
	}

	private static final int _BANDS = 32;

	private static final long _FNV_OFFSET_BASIS = 0xcbf29ce484222325L;

	private static final long _FNV_PRIME = 0x100000001b3L;

	private static final int _HASH_COUNT = 128;

	private static final long _MERSENNE_PRIME = (1L << 61) - 1;

	private static final long[] _PERMUTATION_A = new long[_HASH_COUNT];

	private static final long[] _PERMUTATION_B = new long[_HASH_COUNT];

	private static final int _ROWS = 4;

	private static final int _SHINGLE_SIZE = 3;

	static {

		// Deterministically seed the permutation coefficients with a fixed
		// linear congruential generator so the signature is stable across nodes
		// and reindexes.

		long state = 0x9e3779b97f4a7c15L;

		for (int i = 0; i < _HASH_COUNT; i++) {
			state = (state * 6364136223846793005L) + 1442695040888963407L;

			_PERMUTATION_A[i] = ((state >>> 1) % (_MERSENNE_PRIME - 1)) + 1;

			state = (state * 6364136223846793005L) + 1442695040888963407L;

			_PERMUTATION_B[i] = (state >>> 1) % _MERSENNE_PRIME;
		}
	}

}