/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.repository.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.counter.kernel.model.Counter;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.document.library.test.util.DLTestUtil;
import com.liferay.portal.kernel.db.partition.DBPartition;
import com.liferay.portal.kernel.exception.NoSuchRepositoryException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.Repository;
import com.liferay.portal.kernel.repository.RepositoryFactory;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.RepositoryLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.repository.liferayrepository.LiferayRepository;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Adolfo Pérez
 */
@RunWith(Arquillian.class)
public class RepositoryFactoryTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testAddFolderInRepositoryWhenSameRepositoryIdExistsInDifferentPartition()
		throws Exception {

		if (!DBPartition.isPartitionEnabled()) {
			return;
		}

		Company company = CompanyTestUtil.addCompany();
		com.liferay.portal.kernel.model.Repository repository1;

		long companyId = CompanyThreadLocal.getCompanyId();
		String userId = PrincipalThreadLocal.getName();

		CompanyThreadLocal.setCompanyId(company.getCompanyId());

		try {
			User user = UserTestUtil.addUser(company);

			PrincipalThreadLocal.setName(user.getUserId());

			Group group = GroupTestUtil.addGroupToCompany(
				company.getCompanyId());

			repository1 = _addRepository(group, user.getUserId());

			_repositoryFactory.createRepository(repository1.getRepositoryId());
		}
		finally {
			CompanyThreadLocal.setCompanyId(companyId);
			PrincipalThreadLocal.setName(userId);
		}

		com.liferay.portal.kernel.model.Repository repository2 = _addRepository(
			_group, TestPropsValues.getUserId(), repository1.getRepositoryId());

		Assert.assertEquals(
			repository1.getRepositoryId(), repository2.getRepositoryId());

		Repository repository = _repositoryFactory.createRepository(
			repository2.getRepositoryId());

		repository.addFolder(
			null, TestPropsValues.getUserId(), repository2.getDlFolderId(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext(
				_group, TestPropsValues.getUserId()));
	}

	@Test
	public void testCreateLocalRepositoryFromExistingRepositoryId()
		throws Exception {

		DLFolder dlFolder = DLTestUtil.addDLFolder(_group.getGroupId());

		_repositoryFactory.createLocalRepository(dlFolder.getRepositoryId());
	}

	@Test(expected = NoSuchRepositoryException.class)
	public void testCreateLocalRepositoryFromNonexistentRepositoryId()
		throws Exception {

		long repositoryId = RandomTestUtil.nextLong();

		_repositoryFactory.createLocalRepository(repositoryId);
	}

	@Test
	public void testCreateRepositoryFromExistingRepositoryId()
		throws Exception {

		DLFolder dlFolder = DLTestUtil.addDLFolder(_group.getGroupId());

		_repositoryFactory.createRepository(dlFolder.getRepositoryId());
	}

	@Test(expected = NoSuchRepositoryException.class)
	public void testCreateRepositoryFromNonexistentRepositoryId()
		throws Exception {

		long repositoryId = RandomTestUtil.randomLong();

		_repositoryFactory.createRepository(repositoryId);
	}

	private com.liferay.portal.kernel.model.Repository _addRepository(
			Group group, long userId)
		throws PortalException {

		return _addRepository(group, userId, 0);
	}

	private com.liferay.portal.kernel.model.Repository _addRepository(
			Group group, long userId, long nextId)
		throws PortalException {

		DLFolder dlFolder = _dlFolderLocalService.addFolder(
			null, userId, group.getGroupId(), group.getGroupId(), false,
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), false,
			new ServiceContext());

		if (nextId > 0) {
			_counterLocalService.reset(Counter.class.getName(), nextId - 1);
		}

		return _repositoryLocalService.addRepository(
			userId, group.getGroupId(),
			_portal.getClassNameId(LiferayRepository.class.getName()),
			dlFolder.getFolderId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), "Test Portlet",
			new UnicodeProperties(), true,
			ServiceContextTestUtil.getServiceContext(group, userId));
	}

	@Inject
	private CounterLocalService _counterLocalService;

	@Inject
	private DLFolderLocalService _dlFolderLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private Portal _portal;

	@Inject
	private RepositoryFactory _repositoryFactory;

	@Inject
	private RepositoryLocalService _repositoryLocalService;

}