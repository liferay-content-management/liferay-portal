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

package com.liferay.document.library.web.internal.servlet.taglib.clay;

import com.liferay.document.library.display.context.IGViewFileVersionDisplayContext;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.document.library.web.internal.display.context.IGDisplayContextProvider;
import com.liferay.document.library.web.internal.display.context.helper.DLPortletInstanceSettingsHelper;
import com.liferay.frontend.taglib.clay.servlet.taglib.VerticalCard;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.util.PropsValues;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eudaldo Alonso
 */
public class FileEntryVerticalCard implements VerticalCard {

	public FileEntryVerticalCard(
		DLPortletInstanceSettingsHelper dlPortletInstanceSettingsHelper,
		FileEntry fileEntry, FileVersion fileVersion,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse,
		IGDisplayContextProvider igDisplayContextProvider) {

		_dlPortletInstanceSettingsHelper = dlPortletInstanceSettingsHelper;
		_fileEntry = fileEntry;
		_fileVersion = fileVersion;

		_igViewFileVersionDisplayContext =
			igDisplayContextProvider.getIGViewFileVersionActionsDisplayContext(
				httpServletRequest, httpServletResponse, fileVersion);
		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	@Override
	public List<DropdownItem> getActionDropdownItems() {
		if (!_dlPortletInstanceSettingsHelper.isShowActions()) {
			return null;
		}

		return _igViewFileVersionDisplayContext.getActionDropdownItems();
	}

	@Override
	public String getCssClass() {
		if (!_dlPortletInstanceSettingsHelper.isShowActions()) {
			return "card-interactive card-interactive-secondary";
		}

		return "card-interactive card-interactive-secondary";
	}

	@Override
	public String getIcon() {
		return "documents-and-media";
	}

	@Override
	public String getImageSrc() {
		if (PropsValues.DL_FILE_ENTRY_THUMBNAIL_ENABLED) {
			try {
				return DLURLHelperUtil.getThumbnailSrc(
					_fileEntry, _fileVersion, _themeDisplay);
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception);
				}
			}
		}

		return null;
	}

	@Override
	public String getTitle() {
		if (!_dlPortletInstanceSettingsHelper.isShowActions()) {
			return null;
		}

		return _fileEntry.getTitle();
	}

	@Override
	public Boolean isFlushHorizontal() {
		return true;
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FileEntryVerticalCard.class);

	private final DLPortletInstanceSettingsHelper
		_dlPortletInstanceSettingsHelper;
	private final FileEntry _fileEntry;
	private final FileVersion _fileVersion;
	private final IGViewFileVersionDisplayContext
		_igViewFileVersionDisplayContext;
	private final ThemeDisplay _themeDisplay;

}