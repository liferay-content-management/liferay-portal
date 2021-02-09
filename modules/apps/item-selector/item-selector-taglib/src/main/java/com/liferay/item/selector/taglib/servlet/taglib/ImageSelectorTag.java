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

package com.liferay.item.selector.taglib.servlet.taglib;

import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.item.selector.taglib.internal.servlet.taglib.BaseContainerTag;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * @author Sergio González
 * @author Roberto Díaz
 * @author Carlos Lancha
 */
public class ImageSelectorTag extends BaseContainerTag {

	@Override
	public int doStartTag() throws JspException {
		setAttributeNamespace(_ATTRIBUTE_NAMESPACE);

		if (_fileEntryId != 0) {
			try {
				FileEntry fileEntry = DLAppLocalServiceUtil.getFileEntry(
					_fileEntryId);

				ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
					WebKeys.THEME_DISPLAY);

				_imageURL = DLURLHelperUtil.getPreviewURL(
					fileEntry, fileEntry.getFileVersion(), themeDisplay,
					StringPool.BLANK);
			}
			catch (Exception exception) {
				_log.error(
					"Unable to get HTML preview entry image URL", exception);
			}
		}

		if (Validator.isNotNull(_paramName)) {
			_cropRegion = ParamUtil.getString(
				request, _paramName + "CropRegion");
		}

		return super.doStartTag();
	}

	public String getDraggableImage() {
		return _draggableImage;
	}

	public long getFileEntryId() {
		return _fileEntryId;
	}

	public String getItemSelectorEventName() {
		return _itemSelectorEventName;
	}

	public String getItemSelectorURL() {
		return _itemSelectorURL;
	}

	public long getMaxFileSize() {
		return _maxFileSize;
	}

	public String getParamName() {
		return _paramName;
	}

	public String getUploadURL() {
		return _uploadURL;
	}

	public String getValidExtensions() {
		return _validExtensions;
	}

	public void setDraggableImage(String draggableImage) {
		_draggableImage = draggableImage;
	}

	public void setFileEntryId(long fileEntryId) {
		_fileEntryId = fileEntryId;
	}

	public void setItemSelectorEventName(String itemSelectorEventName) {
		_itemSelectorEventName = itemSelectorEventName;
	}

	public void setItemSelectorURL(String itemSelectorURL) {
		_itemSelectorURL = itemSelectorURL;
	}

	public void setMaxFileSize(long maxFileSize) {
		_maxFileSize = maxFileSize;
	}

	public void setParamName(String paramName) {
		_paramName = paramName;
	}

	public void setUploadURL(String uploadURL) {
		_uploadURL = uploadURL;
	}

	public void setValidExtensions(String validExtensions) {
		_validExtensions = validExtensions;
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();

		_cropRegion = null;
		_draggableImage = "none";
		_fileEntryId = 0;
		_imageURL = null;
		_itemSelectorEventName = null;
		_itemSelectorURL = null;
		_maxFileSize = 0;
		_paramName = "imageSelectorFileEntryId";
		_uploadURL = null;
		_validExtensions = null;
	}

	@Override
	protected String getHydratedModuleName() {
		return "item-selector-taglib/image_selector/ImageSelector";
	}

	@Override
	protected Map<String, Object> prepareProps(Map<String, Object> props) {
		props.put("cropRegion", _cropRegion);
		props.put("draggableImage", _draggableImage);
		props.put("fileEntryId", _fileEntryId);
		props.put("imageURL", _imageURL);
		props.put("itemSelectorEventName", _itemSelectorEventName);
		props.put("itemSelectorURL", _itemSelectorURL);
		props.put("maxFileSize", _maxFileSize);
		props.put("paramName", _paramName);
		props.put("uploadURL", _uploadURL);
		props.put("validExtensions", _validExtensions);

		return super.prepareProps(props);
	}

	@Override
	protected String processCssClasses(Set<String> cssClasses) {
		cssClasses.add("drop-zone");

		if (!_draggableImage.equals("none")) {
			cssClasses.add("draggable-image");
		}

		if (_fileEntryId == 0) {
			cssClasses.add("drop-enabled");
		}

		cssClasses.add("taglib-image-selector");

		return super.processCssClasses(cssClasses);
	}

	@Override
	protected int processStartTag() throws Exception {
		super.processStartTag();

		JspWriter jspWriter = pageContext.getOut();

		jspWriter.write("temporary markup");

		return SKIP_BODY;
	}

	private static final String _ATTRIBUTE_NAMESPACE =
		"item-selector:image-selector:";

	private static final Log _log = LogFactoryUtil.getLog(
		ImageSelectorTag.class);

	private String _cropRegion;
	private String _draggableImage = "none";
	private long _fileEntryId;
	private String _imageURL;
	private String _itemSelectorEventName;
	private String _itemSelectorURL;
	private long _maxFileSize;
	private String _paramName = "imageSelectorFileEntryId";
	private String _uploadURL;
	private String _validExtensions;

}