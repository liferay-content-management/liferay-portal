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
import com.liferay.frontend.taglib.clay.servlet.taglib.ButtonTag;
import com.liferay.item.selector.taglib.internal.servlet.taglib.BaseContainerTag;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.servlet.BrowserSnifferUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.taglib.util.TagResourceBundleUtil;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.portlet.PortletResponse;

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
			_imageCropRegion = ParamUtil.getString(
				request, _paramName + "CropRegion");
		}

		return super.doStartTag();
	}

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link #getImageCropDirection()}
	 */
	@Deprecated
	public String getDraggableImage() {
		return getImageCropDirection();
	}

	public long getFileEntryId() {
		return _fileEntryId;
	}

	public String getImageCropDirection() {
		return _imageCropDirection;
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

	public String getNamespace() {
		if (_namespace != null) {
			return _namespace;
		}

		PortletResponse portletResponse = (PortletResponse)request.getAttribute(
			JavaConstants.JAVAX_PORTLET_RESPONSE);

		if (portletResponse != null) {
			_namespace = portletResponse.getNamespace();
		}

		return _namespace;
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

	/**
	 * @deprecated As of Cavanaugh (7.4.x), replaced by {@link #setImageCropDirection()}
	 */
	@Deprecated
	public void setDraggableImage(String draggableImage) {
		setImageCropDirection(draggableImage);
	}

	public void setFileEntryId(long fileEntryId) {
		_fileEntryId = fileEntryId;
	}

	public void setImageCropDirection(String imageCropDirection) {
		_imageCropDirection = imageCropDirection;
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

	public void setNamespace(String namespace) {
		_namespace = namespace;
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

		_fileEntryId = 0;
		_imageCropDirection = "none";
		_imageCropRegion = null;
		_imageURL = null;
		_itemSelectorEventName = null;
		_itemSelectorURL = null;
		_maxFileSize = 0;
		_namespace = null;
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
		props.put("imageCropDirection", _imageCropDirection);
		props.put("imageCropRegion", _imageCropRegion);
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

		if (!_imageCropDirection.equals("none")) {
			cssClasses.add("draggable-image");
			cssClasses.add(_imageCropDirection);
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

		ResourceBundle resourceBundle = TagResourceBundleUtil.getResourceBundle(
			pageContext);

		JspWriter jspWriter = pageContext.getOut();

		String namespace = getNamespace();

		jspWriter.write("<input name=\"");
		jspWriter.write(namespace);
		jspWriter.write(_paramName);
		jspWriter.write("Id\" type=\"hidden\" value=\"");
		jspWriter.write(String.valueOf(_fileEntryId));
		jspWriter.write("\">");

		jspWriter.write("<input name=\"");
		jspWriter.write(namespace);
		jspWriter.write(_paramName);
		jspWriter.write("CropRegion\" type=\"hidden\" value=\"");
		jspWriter.write(_imageCropRegion);
		jspWriter.write("\">");

		if (Validator.isNotNull(_imageURL)) {
			jspWriter.write("<div class=\"image-wrapper");

			if (!_imageCropDirection.equals("none")) {
				jspWriter.write(" cropper");
			}

			jspWriter.write("\" ><img alt=\"");
			jspWriter.write(LanguageUtil.get(resourceBundle, "current-image"));
			jspWriter.write("\" class=\"current-image\" id=\"");
			jspWriter.write(namespace);
			jspWriter.write("image\" src=\"");
			jspWriter.write(_imageURL);
			jspWriter.write("\" /></div>");
		}

		if (_fileEntryId == 0) {
			jspWriter.write("<div class=\"browse-image-controls\"><div ");
			jspWriter.write("class=\"drag-drop-label\">");

			if (Validator.isNotNull(_itemSelectorEventName) &&
				Validator.isNotNull(_itemSelectorURL)) {

				String dragAndDropToUploadButton =
					"<button class=\"btn btn-secondary\" type=\"button\">" +
						LanguageUtil.get(resourceBundle, "select-file") +
							"</button>";

				if (BrowserSnifferUtil.isMobile(request)) {
					jspWriter.write(dragAndDropToUploadButton);
				}
				else {
					jspWriter.write("<span class=\"pr-1\">");
					jspWriter.write(
						LanguageUtil.format(
							resourceBundle, "drag-and-drop-to-upload-or-x",
							new Object[] {dragAndDropToUploadButton}));
					jspWriter.write("</span>");
				}
			}
			else {
				jspWriter.write(
					LanguageUtil.get(
						resourceBundle, "drag-and-drop-to-upload"));
			}

			jspWriter.write("</div><div class=\"file-validation-info\">");

			if (Validator.isNotNull(_validExtensions)) {
				jspWriter.write("<strong>");
				jspWriter.write(_validExtensions);
				jspWriter.write("</strong>");
			}

			if (_maxFileSize != 0) {
				ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
					WebKeys.THEME_DISPLAY);

				jspWriter.write("<span class=\"pl-1\">");
				jspWriter.write(
					LanguageUtil.format(
						resourceBundle, "maximum-size-x",
						new Object[] {
							LanguageUtil.formatStorageSize(
								_maxFileSize, themeDisplay.getLocale())
						}));

				jspWriter.write("</span>");
			}

			jspWriter.write("</div></div>");
		}
		else {
			jspWriter.write("<div class=\"change-image-controls\">");

			ButtonTag buttonTag = new ButtonTag();

			buttonTag.setDisplayType("secondary");
			buttonTag.setDynamicAttribute(
				StringPool.BLANK, "title",
				HtmlUtil.escape(
					LanguageUtil.get(resourceBundle, "change-image")));
			buttonTag.setIcon("picture");
			buttonTag.setMonospaced(true);
			buttonTag.doTag(pageContext);

			buttonTag.setCssClass("ml-1");
			buttonTag.setDisplayType("secondary");
			buttonTag.setDynamicAttribute(
				StringPool.BLANK, "title",
				HtmlUtil.escape(
					LanguageUtil.get(resourceBundle, "remove-image")));
			buttonTag.setIcon("trash");
			buttonTag.setMonospaced(true);
			buttonTag.doTag(pageContext);

			jspWriter.write("</div>");
		}

		return SKIP_BODY;
	}

	private static final String _ATTRIBUTE_NAMESPACE =
		"item-selector:image-selector:";

	private static final Log _log = LogFactoryUtil.getLog(
		ImageSelectorTag.class);

	private long _fileEntryId;
	private String _imageCropDirection = "none";
	private String _imageCropRegion;
	private String _imageURL;
	private String _itemSelectorEventName;
	private String _itemSelectorURL;
	private long _maxFileSize;
	private String _namespace;
	private String _paramName = "imageSelectorFileEntryId";
	private String _uploadURL;
	private String _validExtensions;

}