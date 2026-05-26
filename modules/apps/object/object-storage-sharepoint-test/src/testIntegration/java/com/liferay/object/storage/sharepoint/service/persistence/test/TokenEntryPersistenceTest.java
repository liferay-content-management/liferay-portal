/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.service.persistence.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.storage.sharepoint.exception.NoSuchTokenEntryException;
import com.liferay.object.storage.sharepoint.model.TokenEntry;
import com.liferay.object.storage.sharepoint.service.TokenEntryLocalServiceUtil;
import com.liferay.object.storage.sharepoint.service.persistence.TokenEntryPersistence;
import com.liferay.object.storage.sharepoint.service.persistence.TokenEntryUtil;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @generated
 */
@RunWith(Arquillian.class)
public class TokenEntryPersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED,
				"com.liferay.object.storage.sharepoint.service"));

	@Before
	public void setUp() {
		_persistence = TokenEntryUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<TokenEntry> iterator = _tokenEntries.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		TokenEntry tokenEntry = _persistence.create(pk);

		Assert.assertNotNull(tokenEntry);

		Assert.assertEquals(tokenEntry.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		TokenEntry newTokenEntry = addTokenEntry();

		_persistence.remove(newTokenEntry);

		TokenEntry existingTokenEntry = _persistence.fetchByPrimaryKey(
			newTokenEntry.getPrimaryKey());

		Assert.assertNull(existingTokenEntry);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addTokenEntry();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		TokenEntry newTokenEntry = _persistence.create(pk);

		newTokenEntry.setMvccVersion(RandomTestUtil.nextLong());

		newTokenEntry.setGroupId(RandomTestUtil.nextLong());

		newTokenEntry.setCompanyId(RandomTestUtil.nextLong());

		newTokenEntry.setUserId(RandomTestUtil.nextLong());

		newTokenEntry.setUserName(RandomTestUtil.randomString());

		newTokenEntry.setCreateDate(RandomTestUtil.nextDate());

		newTokenEntry.setAccessToken(RandomTestUtil.randomString());

		newTokenEntry.setExpirationDate(RandomTestUtil.nextDate());

		newTokenEntry.setRefreshToken(RandomTestUtil.randomString());

		_tokenEntries.add(_persistence.update(newTokenEntry));

		TokenEntry existingTokenEntry = _persistence.findByPrimaryKey(
			newTokenEntry.getPrimaryKey());

		Assert.assertEquals(
			existingTokenEntry.getMvccVersion(),
			newTokenEntry.getMvccVersion());
		Assert.assertEquals(
			existingTokenEntry.getTokenEntryId(),
			newTokenEntry.getTokenEntryId());
		Assert.assertEquals(
			existingTokenEntry.getGroupId(), newTokenEntry.getGroupId());
		Assert.assertEquals(
			existingTokenEntry.getCompanyId(), newTokenEntry.getCompanyId());
		Assert.assertEquals(
			existingTokenEntry.getUserId(), newTokenEntry.getUserId());
		Assert.assertEquals(
			existingTokenEntry.getUserName(), newTokenEntry.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(existingTokenEntry.getCreateDate()),
			Time.getShortTimestamp(newTokenEntry.getCreateDate()));
		Assert.assertEquals(
			existingTokenEntry.getAccessToken(),
			newTokenEntry.getAccessToken());
		Assert.assertEquals(
			Time.getShortTimestamp(existingTokenEntry.getExpirationDate()),
			Time.getShortTimestamp(newTokenEntry.getExpirationDate()));
		Assert.assertEquals(
			existingTokenEntry.getRefreshToken(),
			newTokenEntry.getRefreshToken());
	}

	@Test
	public void testCountByUserId() throws Exception {
		_persistence.countByUserId(RandomTestUtil.nextLong());

		_persistence.countByUserId(0L);
	}

	@Test
	public void testCountByG_U() throws Exception {
		_persistence.countByG_U(
			RandomTestUtil.nextLong(), RandomTestUtil.nextLong());

		_persistence.countByG_U(0L, 0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		TokenEntry newTokenEntry = addTokenEntry();

		TokenEntry existingTokenEntry = _persistence.findByPrimaryKey(
			newTokenEntry.getPrimaryKey());

		Assert.assertEquals(existingTokenEntry, newTokenEntry);
	}

	@Test(expected = NoSuchTokenEntryException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<TokenEntry> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"OSSharepoint_TokenEntry", "mvccVersion", true, "tokenEntryId",
			true, "groupId", true, "companyId", true, "userId", true,
			"userName", true, "createDate", true, "expirationDate", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		TokenEntry newTokenEntry = addTokenEntry();

		TokenEntry existingTokenEntry = _persistence.fetchByPrimaryKey(
			newTokenEntry.getPrimaryKey());

		Assert.assertEquals(existingTokenEntry, newTokenEntry);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		TokenEntry missingTokenEntry = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingTokenEntry);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		TokenEntry newTokenEntry1 = addTokenEntry();
		TokenEntry newTokenEntry2 = addTokenEntry();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newTokenEntry1.getPrimaryKey());
		primaryKeys.add(newTokenEntry2.getPrimaryKey());

		Map<Serializable, TokenEntry> tokenEntries =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, tokenEntries.size());
		Assert.assertEquals(
			newTokenEntry1, tokenEntries.get(newTokenEntry1.getPrimaryKey()));
		Assert.assertEquals(
			newTokenEntry2, tokenEntries.get(newTokenEntry2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, TokenEntry> tokenEntries =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(tokenEntries.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		TokenEntry newTokenEntry = addTokenEntry();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newTokenEntry.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, TokenEntry> tokenEntries =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, tokenEntries.size());
		Assert.assertEquals(
			newTokenEntry, tokenEntries.get(newTokenEntry.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, TokenEntry> tokenEntries =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(tokenEntries.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		TokenEntry newTokenEntry = addTokenEntry();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newTokenEntry.getPrimaryKey());

		Map<Serializable, TokenEntry> tokenEntries =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, tokenEntries.size());
		Assert.assertEquals(
			newTokenEntry, tokenEntries.get(newTokenEntry.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			TokenEntryLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<TokenEntry>() {

				@Override
				public void performAction(TokenEntry tokenEntry) {
					Assert.assertNotNull(tokenEntry);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		TokenEntry newTokenEntry = addTokenEntry();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			TokenEntry.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"tokenEntryId", newTokenEntry.getTokenEntryId()));

		List<TokenEntry> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		TokenEntry existingTokenEntry = result.get(0);

		Assert.assertEquals(existingTokenEntry, newTokenEntry);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			TokenEntry.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"tokenEntryId", RandomTestUtil.nextLong()));

		List<TokenEntry> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		TokenEntry newTokenEntry = addTokenEntry();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			TokenEntry.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("tokenEntryId"));

		Object newTokenEntryId = newTokenEntry.getTokenEntryId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"tokenEntryId", new Object[] {newTokenEntryId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingTokenEntryId = result.get(0);

		Assert.assertEquals(existingTokenEntryId, newTokenEntryId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			TokenEntry.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("tokenEntryId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"tokenEntryId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testResetOriginalValues() throws Exception {
		TokenEntry newTokenEntry = addTokenEntry();

		_persistence.clearCache();

		_assertOriginalValues(
			_persistence.findByPrimaryKey(newTokenEntry.getPrimaryKey()));
	}

	@Test
	public void testResetOriginalValuesWithDynamicQueryLoadFromDatabase()
		throws Exception {

		_testResetOriginalValuesWithDynamicQuery(true);
	}

	@Test
	public void testResetOriginalValuesWithDynamicQueryLoadFromSession()
		throws Exception {

		_testResetOriginalValuesWithDynamicQuery(false);
	}

	private void _testResetOriginalValuesWithDynamicQuery(boolean clearSession)
		throws Exception {

		TokenEntry newTokenEntry = addTokenEntry();

		if (clearSession) {
			Session session = _persistence.openSession();

			session.flush();

			session.clear();
		}

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			TokenEntry.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"tokenEntryId", newTokenEntry.getTokenEntryId()));

		List<TokenEntry> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		_assertOriginalValues(result.get(0));
	}

	private void _assertOriginalValues(TokenEntry tokenEntry) {
		Assert.assertEquals(
			Long.valueOf(tokenEntry.getGroupId()),
			ReflectionTestUtil.<Long>invoke(
				tokenEntry, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "groupId"));
		Assert.assertEquals(
			Long.valueOf(tokenEntry.getUserId()),
			ReflectionTestUtil.<Long>invoke(
				tokenEntry, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "userId"));
	}

	protected TokenEntry addTokenEntry() throws Exception {
		long pk = RandomTestUtil.nextLong();

		TokenEntry tokenEntry = _persistence.create(pk);

		tokenEntry.setMvccVersion(RandomTestUtil.nextLong());

		tokenEntry.setGroupId(RandomTestUtil.nextLong());

		tokenEntry.setCompanyId(RandomTestUtil.nextLong());

		tokenEntry.setUserId(RandomTestUtil.nextLong());

		tokenEntry.setUserName(RandomTestUtil.randomString());

		tokenEntry.setCreateDate(RandomTestUtil.nextDate());

		tokenEntry.setAccessToken(RandomTestUtil.randomString());

		tokenEntry.setExpirationDate(RandomTestUtil.nextDate());

		tokenEntry.setRefreshToken(RandomTestUtil.randomString());

		_tokenEntries.add(_persistence.update(tokenEntry));

		return tokenEntry;
	}

	private List<TokenEntry> _tokenEntries = new ArrayList<TokenEntry>();
	private TokenEntryPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}
// LIFERAY-SERVICE-BUILDER-HASH:1497981740