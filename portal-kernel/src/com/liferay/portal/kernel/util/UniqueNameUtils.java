/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.LanguageUtil;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Akhash R
 */
public class UniqueNameUtils {

	public static String getCopyName(
		String sourceName, Function<String, Boolean> existsFunction,
		String style) {

		String copy = LanguageUtil.get(LocaleUtil.getSiteDefault(), "copy");

		for (int i = 0;; i++) {
			String candidate = _buildCopyName(sourceName, style, copy, i);

			if (!existsFunction.apply(candidate)) {
				return candidate;
			}
		}
	}

	public static String transformCopyStyle(
		String sourceName, String fromStyle, String toStyle) {

		String copy = LanguageUtil.get(LocaleUtil.getSiteDefault(), "copy");

		Pattern pattern;

		if (Objects.equals(fromStyle, StringPool.CLOSE_PARENTHESIS)) {
			pattern = Pattern.compile(
				"^(.*?)\\s*\\(" + Pattern.quote(copy) + "(?:\\s+(\\d+))?\\)$",
				Pattern.CASE_INSENSITIVE);
		}
		else if (Objects.equals(fromStyle, StringPool.DASH)) {
			pattern = Pattern.compile(
				"^(.*?)-" + Pattern.quote(copy) + "(?:-(\\d+))?-?$",
				Pattern.CASE_INSENSITIVE);
		}
		else {
			throw new IllegalArgumentException(
				"Unsupported fromStyle: " + fromStyle);
		}

		String baseName = sourceName;
		int currentIndex = 0;

		Matcher matcher = pattern.matcher(sourceName);

		if (matcher.find()) {
			baseName = matcher.group(
				1
			).trim();

			String indexStr = matcher.group(2);

			if ((indexStr != null) && !indexStr.isEmpty()) {
				currentIndex = GetterUtil.getInteger(indexStr);
			}
		}

		return _buildCopyName(baseName, toStyle, copy, currentIndex);
	}

	private static String _buildCopyName(
		String baseName, String style, String copy, int index) {

		if (Objects.equals(style, StringPool.CLOSE_PARENTHESIS)) {
			if (index == 0) {
				return StringUtil.appendParentheticalSuffix(baseName, copy);
			}

			return StringUtil.appendParentheticalSuffix(
				baseName, copy + StringPool.SPACE + index);
		}
		else if (Objects.equals(style, StringPool.DASH)) {
			if (index == 0) {
				return StringBundler.concat(
					baseName, StringPool.DASH, copy, StringPool.DASH);
			}

			return StringBundler.concat(
				baseName, StringPool.DASH, copy, StringPool.DASH,
				String.valueOf(index), StringPool.DASH);
		}

		throw new IllegalArgumentException("Unsupported style: " + style);
	}

}