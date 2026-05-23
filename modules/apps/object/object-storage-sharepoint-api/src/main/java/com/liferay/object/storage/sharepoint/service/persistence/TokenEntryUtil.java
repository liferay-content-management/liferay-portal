/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.service.persistence;

import com.liferay.object.storage.sharepoint.model.TokenEntry;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The persistence utility for the token entry service. This utility wraps <code>com.liferay.object.storage.sharepoint.service.persistence.impl.TokenEntryPersistenceImpl</code> and provides direct access to the database for CRUD operations. This utility should only be used by the service layer, as it must operate within a transaction. Never access this utility in a JSP, controller, model, or other front-end class.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Jürgen Kappler
 * @see TokenEntryPersistence
 * @generated
 */
public class TokenEntryUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#cacheResult(List)
	 */
	public static void cacheResult(List<TokenEntry> tokenEntries) {
		getPersistence().cacheResult(tokenEntries);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#cacheResult(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static void cacheResult(TokenEntry tokenEntry) {
		getPersistence().cacheResult(tokenEntry);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache()
	 */
	public static void clearCache() {
		getPersistence().clearCache();
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static void clearCache(TokenEntry tokenEntry) {
		getPersistence().clearCache(tokenEntry);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#countWithDynamicQuery(DynamicQuery)
	 */
	public static long countWithDynamicQuery(DynamicQuery dynamicQuery) {
		return getPersistence().countWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#fetchByPrimaryKeys(Set)
	 */
	public static Map<Serializable, TokenEntry> fetchByPrimaryKeys(
		Set<Serializable> primaryKeys) {

		return getPersistence().fetchByPrimaryKeys(primaryKeys);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery)
	 */
	public static List<TokenEntry> findWithDynamicQuery(
		DynamicQuery dynamicQuery) {

		return getPersistence().findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int)
	 */
	public static List<TokenEntry> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getPersistence().findWithDynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int, OrderByComparator)
	 */
	public static List<TokenEntry> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<TokenEntry> orderByComparator) {

		return getPersistence().findWithDynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static TokenEntry update(TokenEntry tokenEntry) {
		return getPersistence().update(tokenEntry);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel, ServiceContext)
	 */
	public static TokenEntry update(
		TokenEntry tokenEntry, ServiceContext serviceContext) {

		return getPersistence().update(tokenEntry, serviceContext);
	}

	/**
	 * Returns an ordered range of all the token entries where userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.object.storage.sharepoint.model.impl.TokenEntryModelImpl</code>.
	 * </p>
	 *
	 * @param userId the user ID
	 * @param start the lower bound of the range of token entries
	 * @param end the upper bound of the range of token entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching token entries
	 */
	public static List<TokenEntry> findByUserId(
		long userId, int start, int end,
		OrderByComparator<TokenEntry> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByUserId(
			userId, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first token entry in the ordered set where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching token entry
	 * @throws NoSuchTokenEntryException if a matching token entry could not be found
	 */
	public static TokenEntry findByUserId_First(
			long userId, OrderByComparator<TokenEntry> orderByComparator)
		throws com.liferay.object.storage.sharepoint.exception.
			NoSuchTokenEntryException {

		return getPersistence().findByUserId_First(userId, orderByComparator);
	}

	/**
	 * Returns the first token entry in the ordered set where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching token entry, or <code>null</code> if a matching token entry could not be found
	 */
	public static TokenEntry fetchByUserId_First(
		long userId, OrderByComparator<TokenEntry> orderByComparator) {

		return getPersistence().fetchByUserId_First(userId, orderByComparator);
	}

	/**
	 * Removes all the token entries where userId = &#63; from the database.
	 *
	 * @param userId the user ID
	 */
	public static void removeByUserId(long userId) {
		getPersistence().removeByUserId(userId);
	}

	/**
	 * Returns the number of token entries where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @return the number of matching token entries
	 */
	public static int countByUserId(long userId) {
		return getPersistence().countByUserId(userId);
	}

	/**
	 * Returns the token entry where groupId = &#63; and userId = &#63; or throws a <code>NoSuchTokenEntryException</code> if it could not be found.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the matching token entry
	 * @throws NoSuchTokenEntryException if a matching token entry could not be found
	 */
	public static TokenEntry findByG_U(long groupId, long userId)
		throws com.liferay.object.storage.sharepoint.exception.
			NoSuchTokenEntryException {

		return getPersistence().findByG_U(groupId, userId);
	}

	/**
	 * Returns the token entry where groupId = &#63; and userId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching token entry, or <code>null</code> if a matching token entry could not be found
	 */
	public static TokenEntry fetchByG_U(
		long groupId, long userId, boolean useFinderCache) {

		return getPersistence().fetchByG_U(groupId, userId, useFinderCache);
	}

	/**
	 * Removes the token entry where groupId = &#63; and userId = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the token entry that was removed
	 */
	public static TokenEntry removeByG_U(long groupId, long userId)
		throws com.liferay.object.storage.sharepoint.exception.
			NoSuchTokenEntryException {

		return getPersistence().removeByG_U(groupId, userId);
	}

	/**
	 * Returns the number of token entries where groupId = &#63; and userId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the number of matching token entries
	 */
	public static int countByG_U(long groupId, long userId) {
		return getPersistence().countByG_U(groupId, userId);
	}

	/**
	 * Creates a new token entry with the primary key. Does not add the token entry to the database.
	 *
	 * @param tokenEntryId the primary key for the new token entry
	 * @return the new token entry
	 */
	public static TokenEntry create(long tokenEntryId) {
		return getPersistence().create(tokenEntryId);
	}

	/**
	 * Removes the token entry with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry that was removed
	 * @throws NoSuchTokenEntryException if a token entry with the primary key could not be found
	 */
	public static TokenEntry remove(long tokenEntryId)
		throws com.liferay.object.storage.sharepoint.exception.
			NoSuchTokenEntryException {

		return getPersistence().remove(tokenEntryId);
	}

	public static TokenEntry updateImpl(TokenEntry tokenEntry) {
		return getPersistence().updateImpl(tokenEntry);
	}

	/**
	 * Returns the token entry with the primary key or throws a <code>NoSuchTokenEntryException</code> if it could not be found.
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry
	 * @throws NoSuchTokenEntryException if a token entry with the primary key could not be found
	 */
	public static TokenEntry findByPrimaryKey(long tokenEntryId)
		throws com.liferay.object.storage.sharepoint.exception.
			NoSuchTokenEntryException {

		return getPersistence().findByPrimaryKey(tokenEntryId);
	}

	/**
	 * Returns the token entry with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry, or <code>null</code> if a token entry with the primary key could not be found
	 */
	public static TokenEntry fetchByPrimaryKey(long tokenEntryId) {
		return getPersistence().fetchByPrimaryKey(tokenEntryId);
	}

	/**
	 * Returns the token entry where groupId = &#63; and userId = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the matching token entry, or <code>null</code> if a matching token entry could not be found
	 */
	public static TokenEntry fetchByG_U(long groupId, long userId) {
		return getPersistence().fetchByG_U(groupId, userId);
	}

	/**
	 * Returns all the token entries where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @return the matching token entries
	 */
	public static List<TokenEntry> findByUserId(long userId) {
		return getPersistence().findByUserId(userId);
	}

	/**
	 * Returns a range of all the token entries where userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.object.storage.sharepoint.model.impl.TokenEntryModelImpl</code>.
	 * </p>
	 *
	 * @param userId the user ID
	 * @param start the lower bound of the range of token entries
	 * @param end the upper bound of the range of token entries (not inclusive)
	 * @return the range of matching token entries
	 */
	public static List<TokenEntry> findByUserId(
		long userId, int start, int end) {

		return getPersistence().findByUserId(userId, start, end);
	}

	/**
	 * Returns an ordered range of all the token entries where userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.object.storage.sharepoint.model.impl.TokenEntryModelImpl</code>.
	 * </p>
	 *
	 * @param userId the user ID
	 * @param start the lower bound of the range of token entries
	 * @param end the upper bound of the range of token entries (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching token entries
	 */
	public static List<TokenEntry> findByUserId(
		long userId, int start, int end,
		OrderByComparator<TokenEntry> orderByComparator) {

		return getPersistence().findByUserId(
			userId, start, end, orderByComparator);
	}

	public static TokenEntryPersistence getPersistence() {
		return _persistence;
	}

	public static void setPersistence(TokenEntryPersistence persistence) {
		_persistence = persistence;
	}

	private static volatile TokenEntryPersistence _persistence;

}
// LIFERAY-SERVICE-BUILDER-HASH:103377647