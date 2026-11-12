/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.search.spi.model.index.contributor;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Jan Brychta
 */
public class KaleoTaskInstanceTokenModelDocumentContributorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			_kaleoTaskInstanceTokenModelDocumentContributor,
			"_objectEntryLocalService", _objectEntryLocalService);

		_setUpKaleoTaskInstanceToken();
		_setUpObjectDefinition();
		_setUpObjectEntry();
		_setUpObjectEntryLocalService();
	}

	@Test
	public void testContributeWhenObjectDefinitionIsCMP() {
		Mockito.when(
			_objectDefinition.isCMP()
		).thenReturn(
			true
		);

		Document document = Mockito.mock(Document.class);

		_kaleoTaskInstanceTokenModelDocumentContributor.contribute(
			document, _kaleoTaskInstanceToken);

		Mockito.verify(
			document
		).addKeyword(
			"cmpAsset", true
		);
	}

	@Test
	public void testContributeWhenObjectDefinitionIsCMS() {
		Mockito.when(
			_objectDefinition.isCMS()
		).thenReturn(
			true
		);

		Document document = Mockito.mock(Document.class);

		_kaleoTaskInstanceTokenModelDocumentContributor.contribute(
			document, _kaleoTaskInstanceToken);

		Mockito.verify(
			document
		).addKeyword(
			"cmsAsset", true
		);
	}

	@Test
	public void testContributeWhenObjectDefinitionIsCMSAndCMP() {
		Mockito.when(
			_objectDefinition.isCMP()
		).thenReturn(
			true
		);

		Mockito.when(
			_objectDefinition.isCMS()
		).thenReturn(
			true
		);

		Document document = Mockito.mock(Document.class);

		_kaleoTaskInstanceTokenModelDocumentContributor.contribute(
			document, _kaleoTaskInstanceToken);

		Mockito.verify(
			document
		).addKeyword(
			"cmpAsset", true
		);

		Mockito.verify(
			document
		).addKeyword(
			"cmsAsset", true
		);
	}

	@Test
	public void testContributeWhenObjectDefinitionIsNeitherCMSNorCMP() {
		Document document = Mockito.mock(Document.class);

		_kaleoTaskInstanceTokenModelDocumentContributor.contribute(
			document, _kaleoTaskInstanceToken);

		Mockito.verifyNoInteractions(document);
	}

	@Test
	public void testContributeWhenObjectDefinitionIsNull() {
		Mockito.when(
			_objectEntry.getObjectDefinition()
		).thenReturn(
			null
		);

		Document document = Mockito.mock(Document.class);

		_kaleoTaskInstanceTokenModelDocumentContributor.contribute(
			document, _kaleoTaskInstanceToken);

		Mockito.verifyNoInteractions(document);
	}

	@Test
	public void testContributeWhenObjectEntryIsNull() {
		Mockito.when(
			_objectEntryLocalService.fetchObjectEntry(Mockito.anyLong())
		).thenReturn(
			null
		);

		Document document = Mockito.mock(Document.class);

		_kaleoTaskInstanceTokenModelDocumentContributor.contribute(
			document, _kaleoTaskInstanceToken);

		Mockito.verifyNoInteractions(document);
	}

	private void _setUpKaleoTaskInstanceToken() {
		Mockito.when(
			_kaleoTaskInstanceToken.getClassPK()
		).thenReturn(
			RandomTestUtil.randomLong()
		);
	}

	private void _setUpObjectDefinition() {
		Mockito.when(
			_objectDefinition.isCMP()
		).thenReturn(
			false
		);

		Mockito.when(
			_objectDefinition.isCMS()
		).thenReturn(
			false
		);
	}

	private void _setUpObjectEntry() {
		Mockito.when(
			_objectEntry.getObjectDefinition()
		).thenReturn(
			_objectDefinition
		);
	}

	private void _setUpObjectEntryLocalService() {
		Mockito.when(
			_objectEntryLocalService.fetchObjectEntry(Mockito.anyLong())
		).thenReturn(
			_objectEntry
		);
	}

	private final KaleoTaskInstanceToken _kaleoTaskInstanceToken = Mockito.mock(
		KaleoTaskInstanceToken.class);
	private final KaleoTaskInstanceTokenModelDocumentContributor
		_kaleoTaskInstanceTokenModelDocumentContributor =
			new KaleoTaskInstanceTokenModelDocumentContributor();
	private final ObjectDefinition _objectDefinition = Mockito.mock(
		ObjectDefinition.class);
	private final ObjectEntry _objectEntry = Mockito.mock(ObjectEntry.class);
	private final ObjectEntryLocalService _objectEntryLocalService =
		Mockito.mock(ObjectEntryLocalService.class);

}