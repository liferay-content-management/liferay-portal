/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.service.persistence.impl;

import com.liferay.object.storage.sharepoint.exception.NoSuchTokenEntryException;
import com.liferay.object.storage.sharepoint.model.TokenEntry;
import com.liferay.object.storage.sharepoint.model.TokenEntryTable;
import com.liferay.object.storage.sharepoint.model.impl.TokenEntryImpl;
import com.liferay.object.storage.sharepoint.model.impl.TokenEntryModelImpl;
import com.liferay.object.storage.sharepoint.service.persistence.TokenEntryPersistence;
import com.liferay.object.storage.sharepoint.service.persistence.TokenEntryUtil;
import com.liferay.object.storage.sharepoint.service.persistence.impl.constants.OSSharepointPersistenceConstants;
import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.dao.orm.SessionFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.service.persistence.impl.CollectionPersistenceFinder;
import com.liferay.portal.kernel.service.persistence.impl.FinderColumn;
import com.liferay.portal.kernel.service.persistence.impl.UniquePersistenceFinder;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ProxyUtil;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The persistence implementation for the token entry service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Jürgen Kappler
 * @generated
 */
@Component(service = TokenEntryPersistence.class)
public class TokenEntryPersistenceImpl
	extends BasePersistenceImpl<TokenEntry, NoSuchTokenEntryException>
	implements TokenEntryPersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>TokenEntryUtil</code> to access the token entry persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		TokenEntryImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private CollectionPersistenceFinder<TokenEntry>
		_collectionPersistenceFinderByUserId;

	/**
	 * Returns an ordered range of all the token entries where userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>TokenEntryModelImpl</code>.
	 * </p>
	 *
	 * @param userId the user ID
	 * @param start the lower bound of the range of token entries
	 * @param end the upper bound of the range of token entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching token entries
	 */
	@Override
	public List<TokenEntry> findByUserId(
		long userId, int start, int end,
		OrderByComparator<TokenEntry> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByUserId.find(
			finderCache, new Object[] {userId}, start, end, orderByComparator,
			useFinderCache);
	}

	/**
	 * Returns the first token entry in the ordered set where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching token entry
	 * @throws NoSuchTokenEntryException if a matching token entry could not be found
	 */
	@Override
	public TokenEntry findByUserId_First(
			long userId, OrderByComparator<TokenEntry> orderByComparator)
		throws NoSuchTokenEntryException {

		TokenEntry tokenEntry = fetchByUserId_First(userId, orderByComparator);

		if (tokenEntry != null) {
			return tokenEntry;
		}

		throw new NoSuchTokenEntryException(
			_collectionPersistenceFinderByUserId.buildNoSuchKeyMessage(
				_NO_SUCH_ENTITY_WITH_KEY, new Object[] {userId}));
	}

	/**
	 * Returns the first token entry in the ordered set where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching token entry, or <code>null</code> if a matching token entry could not be found
	 */
	@Override
	public TokenEntry fetchByUserId_First(
		long userId, OrderByComparator<TokenEntry> orderByComparator) {

		return _collectionPersistenceFinderByUserId.fetchFirst(
			finderCache, new Object[] {userId}, orderByComparator);
	}

	/**
	 * Removes all the token entries where userId = &#63; from the database.
	 *
	 * @param userId the user ID
	 */
	@Override
	public void removeByUserId(long userId) {
		_collectionPersistenceFinderByUserId.remove(
			finderCache, new Object[] {userId});
	}

	/**
	 * Returns the number of token entries where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @return the number of matching token entries
	 */
	@Override
	public int countByUserId(long userId) {
		return _collectionPersistenceFinderByUserId.count(
			finderCache, new Object[] {userId});
	}

	private UniquePersistenceFinder<TokenEntry> _uniquePersistenceFinderByG_U;

	/**
	 * Returns the token entry where groupId = &#63; and userId = &#63; or throws a <code>NoSuchTokenEntryException</code> if it could not be found.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the matching token entry
	 * @throws NoSuchTokenEntryException if a matching token entry could not be found
	 */
	@Override
	public TokenEntry findByG_U(long groupId, long userId)
		throws NoSuchTokenEntryException {

		TokenEntry tokenEntry = fetchByG_U(groupId, userId);

		if (tokenEntry == null) {
			String message =
				_uniquePersistenceFinderByG_U.buildNoSuchKeyMessage(
					_NO_SUCH_ENTITY_WITH_KEY, new Object[] {groupId, userId});

			if (_log.isDebugEnabled()) {
				_log.debug(message);
			}

			throw new NoSuchTokenEntryException(message);
		}

		return tokenEntry;
	}

	/**
	 * Returns the token entry where groupId = &#63; and userId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching token entry, or <code>null</code> if a matching token entry could not be found
	 */
	@Override
	public TokenEntry fetchByG_U(
		long groupId, long userId, boolean useFinderCache) {

		return _uniquePersistenceFinderByG_U.fetch(
			finderCache, new Object[] {groupId, userId}, useFinderCache);
	}

	/**
	 * Removes the token entry where groupId = &#63; and userId = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the token entry that was removed
	 */
	@Override
	public TokenEntry removeByG_U(long groupId, long userId)
		throws NoSuchTokenEntryException {

		TokenEntry tokenEntry = findByG_U(groupId, userId);

		return remove(tokenEntry);
	}

	/**
	 * Returns the number of token entries where groupId = &#63; and userId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the number of matching token entries
	 */
	@Override
	public int countByG_U(long groupId, long userId) {
		return _uniquePersistenceFinderByG_U.count(
			finderCache, new Object[] {groupId, userId});
	}

	public TokenEntryPersistenceImpl() {
		setModelClass(TokenEntry.class);

		setModelImplClass(TokenEntryImpl.class);
		setModelPKClass(long.class);

		setTable(TokenEntryTable.INSTANCE);
	}

	/**
	 * Creates a new token entry with the primary key. Does not add the token entry to the database.
	 *
	 * @param tokenEntryId the primary key for the new token entry
	 * @return the new token entry
	 */
	@Override
	public TokenEntry create(long tokenEntryId) {
		TokenEntry tokenEntry = new TokenEntryImpl();

		tokenEntry.setNew(true);
		tokenEntry.setPrimaryKey(tokenEntryId);

		tokenEntry.setCompanyId(CompanyThreadLocal.getCompanyId());

		return tokenEntry;
	}

	/**
	 * Removes the token entry with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry that was removed
	 * @throws NoSuchTokenEntryException if a token entry with the primary key could not be found
	 */
	@Override
	public TokenEntry remove(long tokenEntryId)
		throws NoSuchTokenEntryException {

		return remove((Serializable)tokenEntryId);
	}

	@Override
	protected TokenEntry removeImpl(TokenEntry tokenEntry) {
		Session session = null;

		try {
			session = openSession();

			if (!session.contains(tokenEntry)) {
				tokenEntry = (TokenEntry)session.get(
					TokenEntryImpl.class, tokenEntry.getPrimaryKeyObj());
			}

			if (tokenEntry != null) {
				session.delete(tokenEntry);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (tokenEntry != null) {
			clearCache(tokenEntry);
		}

		return tokenEntry;
	}

	@Override
	public TokenEntry updateImpl(TokenEntry tokenEntry) {
		boolean isNew = tokenEntry.isNew();

		if (!(tokenEntry instanceof TokenEntryModelImpl)) {
			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(tokenEntry.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(tokenEntry);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in tokenEntry proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom TokenEntry implementation " +
					tokenEntry.getClass());
		}

		TokenEntryModelImpl tokenEntryModelImpl =
			(TokenEntryModelImpl)tokenEntry;

		if (isNew && (tokenEntry.getCreateDate() == null)) {
			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			Date date = new Date();

			if (serviceContext == null) {
				tokenEntry.setCreateDate(date);
			}
			else {
				tokenEntry.setCreateDate(serviceContext.getCreateDate(date));
			}
		}

		Session session = null;

		try {
			session = openSession();

			if (isNew) {
				session.save(tokenEntry);
			}
			else {
				tokenEntry = (TokenEntry)session.merge(tokenEntry);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		cacheUniqueFindersResult(tokenEntry, false);

		if (isNew) {
			tokenEntry.setNew(false);
		}

		tokenEntry.resetOriginalValues();

		return tokenEntry;
	}

	/**
	 * Returns the token entry with the primary key or throws a <code>NoSuchTokenEntryException</code> if it could not be found.
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry
	 * @throws NoSuchTokenEntryException if a token entry with the primary key could not be found
	 */
	@Override
	public TokenEntry findByPrimaryKey(long tokenEntryId)
		throws NoSuchTokenEntryException {

		return findByPrimaryKey((Serializable)tokenEntryId);
	}

	/**
	 * Returns the token entry with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry, or <code>null</code> if a token entry with the primary key could not be found
	 */
	@Override
	public TokenEntry fetchByPrimaryKey(long tokenEntryId) {
		return fetchByPrimaryKey((Serializable)tokenEntryId);
	}

	@Override
	protected EntityCache getEntityCache() {
		return entityCache;
	}

	@Override
	protected String getPKDBName() {
		return "tokenEntryId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_TOKENENTRY;
	}

	@Override
	protected Map<String, Integer> getTableColumnsMap() {
		return TokenEntryModelImpl.TABLE_COLUMNS_MAP;
	}

	/**
	 * Initializes the token entry persistence.
	 */
	@Activate
	public void activate() {
		_collectionPersistenceFinderByUserId =
			new CollectionPersistenceFinder<>(
				this,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByUserId",
					new String[] {
						Long.class.getName(), Integer.class.getName(),
						Integer.class.getName(),
						OrderByComparator.class.getName()
					},
					new String[] {"userId"}, true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByUserId",
					new String[] {Long.class.getName()},
					new String[] {"userId"}, true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByUserId",
					new String[] {Long.class.getName()},
					new String[] {"userId"}, false),
				_SQL_SELECT_TOKENENTRY_WHERE, _SQL_COUNT_TOKENENTRY_WHERE,
				TokenEntryModelImpl.ORDER_BY_JPQL, _ENTITY_ALIAS_PREFIX, "",
				new FinderColumn<>(
					"tokenEntry.", "userId", FinderColumn.Type.LONG, "=", true,
					true, TokenEntry::getUserId));

		_uniquePersistenceFinderByG_U = new UniquePersistenceFinder<>(
			this,
			createUniqueFinderPath(
				FINDER_CLASS_NAME_ENTITY, "fetchByG_U",
				new String[] {Long.class.getName(), Long.class.getName()},
				new String[] {"groupId", "userId"}, 0, 0, false,
				TokenEntry::getGroupId, TokenEntry::getUserId),
			_SQL_SELECT_TOKENENTRY_WHERE, "",
			new FinderColumn<>(
				"tokenEntry.", "groupId", FinderColumn.Type.LONG, "=", true,
				true, TokenEntry::getGroupId),
			new FinderColumn<>(
				"tokenEntry.", "userId", FinderColumn.Type.LONG, "=", true,
				true, TokenEntry::getUserId));

		TokenEntryUtil.setPersistence(this);
	}

	@Deactivate
	public void deactivate() {
		TokenEntryUtil.setPersistence(null);

		entityCache.removeCache(TokenEntryImpl.class.getName());
	}

	@Override
	@Reference(
		target = OSSharepointPersistenceConstants.SERVICE_CONFIGURATION_FILTER,
		unbind = "-"
	)
	public void setConfiguration(Configuration configuration) {
	}

	@Override
	@Reference(
		target = OSSharepointPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Override
	@Reference(
		target = OSSharepointPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@Reference
	protected EntityCache entityCache;

	@Reference
	protected FinderCache finderCache;

	private static final String _ENTITY_ALIAS_PREFIX =
		TokenEntryModelImpl.ENTITY_ALIAS + ".";

	private static final String _SQL_SELECT_TOKENENTRY =
		"SELECT tokenEntry FROM TokenEntry tokenEntry";

	private static final String _SQL_SELECT_TOKENENTRY_WHERE =
		"SELECT tokenEntry FROM TokenEntry tokenEntry WHERE ";

	private static final String _SQL_COUNT_TOKENENTRY_WHERE =
		"SELECT COUNT(tokenEntry) FROM TokenEntry tokenEntry WHERE ";

	private static final String _NO_SUCH_ENTITY_WITH_KEY =
		"No TokenEntry exists with the key {";

	private static final Log _log = LogFactoryUtil.getLog(
		TokenEntryPersistenceImpl.class);

	@Override
	protected FinderCache getFinderCache() {
		return finderCache;
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:-1721185089