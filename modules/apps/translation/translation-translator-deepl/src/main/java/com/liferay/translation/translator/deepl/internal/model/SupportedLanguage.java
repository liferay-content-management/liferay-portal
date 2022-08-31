/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.translation.translator.deepl.internal.model;

/**
 * @author Yasuyuki Takeo
 */
public class SupportedLanguage {

	public String getLanguage() {
		return language;
	}

	public String getName() {
		return name;
	}

	public Boolean getSupports_formality() {
		return supports_formality;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSupports_formality(Boolean supports_formality) {
		this.supports_formality = supports_formality;
	}

	public String language;
	public String name;
	public Boolean supports_formality;

}