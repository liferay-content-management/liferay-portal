/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.diff.internal;

import com.liferay.diff.DiffHtml;
import com.liferay.petra.io.unsync.UnsyncStringWriter;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.lang.ThreadContextClassLoaderUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.security.xml.SecureXMLFactoryProviderUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.Reader;

import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.outerj.daisy.diff.helper.NekoHtmlParser;
import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;

import org.osgi.service.component.annotations.Component;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author Julio Camarero
 */
@Component(service = DiffHtml.class)
public class DiffHtmlImpl implements DiffHtml {

	@Override
	public String diff(Reader source, Reader target) throws Exception {
		if (source == null) {
			throw new NullPointerException("Source is null");
		}

		if (target == null) {
			throw new NullPointerException("Target is null");
		}

		UnsyncStringWriter unsyncStringWriter = new UnsyncStringWriter();

		try (SafeCloseable safeCloseable = ThreadContextClassLoaderUtil.swap(
				DiffHtmlImpl.class.getClassLoader())) {

			SAXTransformerFactory saxTransformerFactory =
				(SAXTransformerFactory)
					SecureXMLFactoryProviderUtil.newTransformerFactory();

			TransformerHandler transformerHandler =
				saxTransformerFactory.newTransformerHandler();

			Transformer transformer = transformerHandler.getTransformer();

			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformerHandler.setResult(new StreamResult(unsyncStringWriter));

			ContentHandler contentHandler = new ImageOverlayFilter(
				transformerHandler);

			NekoHtmlParser nekoHtmlParser = new NekoHtmlParser();

			DomTreeBuilder oldDomTreeBuilder = new DomTreeBuilder();

			nekoHtmlParser.parse(new InputSource(source), oldDomTreeBuilder);

			Locale locale = LocaleUtil.getDefault();

			TextNodeComparator leftTextNodeComparator = new TextNodeComparator(
				oldDomTreeBuilder, locale);

			DomTreeBuilder newDomTreeBuilder = new DomTreeBuilder();

			nekoHtmlParser.parse(new InputSource(target), newDomTreeBuilder);

			TextNodeComparator rightTextNodeComparator = new TextNodeComparator(
				newDomTreeBuilder, locale);

			contentHandler.startDocument();

			contentHandler.startElement(
				StringPool.BLANK, _DIFF, _DIFF, new AttributesImpl());

			HtmlSaxDiffOutput htmlSaxDiffOutput = new HtmlSaxDiffOutput(
				contentHandler, _DIFF);

			HTMLDiffer htmlDiffer = new HTMLDiffer(htmlSaxDiffOutput);

			htmlDiffer.diff(leftTextNodeComparator, rightTextNodeComparator);

			contentHandler.endElement(StringPool.BLANK, _DIFF, _DIFF);

			contentHandler.endDocument();

			unsyncStringWriter.flush();

			String string = unsyncStringWriter.toString();

			if (string.startsWith("<?xml")) {
				int index = string.indexOf("?>");

				string = string.substring(index + 2);
			}

			if (string.startsWith(_DIFF_OPEN_TAG) &&
				string.endsWith(_DIFF_CLOSE_TAG)) {

				string = string.substring(
					_DIFF_OPEN_TAG.length(),
					string.length() - _DIFF_CLOSE_TAG.length());
			}

			return string;
		}
	}

	@Override
	public String replaceStyles(String html) {
		return StringUtil.replace(
			html,
			new String[] {
				"changeType=\"diff-added-image\"",
				"changeType=\"diff-changed-image\"",
				"changeType=\"diff-removed-image\"",
				"class=\"diff-html-added\"", "class=\"diff-html-changed\"",
				"class=\"diff-html-removed\""
			},
			new String[] {
				"style=\"border: 10px solid #CFC;\"",
				"style=\"border: 10px solid blue;\"",
				"style=\"border: 10px solid #FDC6C6;\"",
				"style=\"background-color: #CFC;\"",
				"style=\"border-bottom: 2px dotted blue;\"",
				"style=\"background-color: #FDC6C6; text-decoration: " +
					"line-through;\""
			});
	}

	private static final String _DIFF = "diff";

	private static final String _DIFF_CLOSE_TAG = "</" + _DIFF + ">";

	private static final String _DIFF_OPEN_TAG = "<" + _DIFF + ">";

	private static class ImageOverlayFilter extends XMLFilterImpl {

		public ImageOverlayFilter(ContentHandler contentHandler) {
			setContentHandler(contentHandler);
		}

		@Override
		public void startElement(
				String uri, String localName, String qName, Attributes atts)
			throws SAXException {

			if (!"img".equalsIgnoreCase(localName)) {
				super.startElement(uri, localName, qName, atts);

				return;
			}

			String changeType = atts.getValue("changeType");

			if (!"diff-added-image".equals(changeType) &&
				!"diff-removed-image".equals(changeType)) {

				super.startElement(uri, localName, qName, atts);

				return;
			}

			AttributesImpl newAtts = new AttributesImpl(atts);

			newAtts.addAttribute(
				StringPool.BLANK, "onLoad", "onLoad", "CDATA",
				"updateOverlays()");
			newAtts.addAttribute(
				StringPool.BLANK, "onError", "onError", "CDATA",
				"updateOverlays()");
			newAtts.addAttribute(
				StringPool.BLANK, "onAbort", "onAbort", "CDATA",
				"updateOverlays()");

			super.startElement(uri, localName, qName, newAtts);
		}

	}

}
