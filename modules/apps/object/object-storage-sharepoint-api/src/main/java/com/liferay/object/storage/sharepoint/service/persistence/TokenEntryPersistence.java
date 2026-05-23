/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.service.persistence;

import com.liferay.object.storage.sharepoint.exception.NoSuchTokenEntryException;
import com.liferay.object.storage.sharepoint.model.TokenEntry;
import com.liferay.portal.kernel.service.persistence.BasePersistence;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the token entry service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Jürgen Kappler
 * @see TokenEntryUtil
 * @generated
 */
@ProviderType
public interface TokenEntryPersistence extends BasePersistence<TokenEntry> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link TokenEntryUtil} to access the token entry persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

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
	public java.util.List<TokenEntry> findByUserId(
		long userId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<TokenEntry>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first token entry in the ordered set where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching token entry
	 * @throws NoSuchTokenEntryException if a matching token entry could not be found
	 */
	public TokenEntry findByUserId_First(
			long userId,
			com.liferay.portal.kernel.util.OrderByComparator<TokenEntry>
				orderByComparator)
		throws NoSuchTokenEntryException;

	/**
	 * Returns the first token entry in the ordered set where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching token entry, or <code>null</code> if a matching token entry could not be found
	 */
	public TokenEntry fetchByUserId_First(
		long userId,
		com.liferay.portal.kernel.util.OrderByComparator<TokenEntry>
			orderByComparator);

	/**
	 * Removes all the token entries where userId = &#63; from the database.
	 *
	 * @param userId the user ID
	 */
	public void removeByUserId(long userId);

	/**
	 * Returns the number of token entries where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @return the number of matching token entries
	 */
	public int countByUserId(long userId);

	/**
	 * Returns the token entry where groupId = &#63; and userId = &#63; or throws a <code>NoSuchTokenEntryException</code> if it could not be found.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the matching token entry
	 * @throws NoSuchTokenEntryException if a matching token entry could not be found
	 */
	public TokenEntry findByG_U(long groupId, long userId)
		throws NoSuchTokenEntryException;

	/**
	 * Returns the token entry where groupId = &#63; and userId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching token entry, or <code>null</code> if a matching token entry could not be found
	 */
	public TokenEntry fetchByG_U(
		long groupId, long userId, boolean useFinderCache);

	/**
	 * Removes the token entry where groupId = &#63; and userId = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the token entry that was removed
	 */
	public TokenEntry removeByG_U(long groupId, long userId)
		throws NoSuchTokenEntryException;

	/**
	 * Returns the number of token entries where groupId = &#63; and userId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the number of matching token entries
	 */
	public int countByG_U(long groupId, long userId);

	/**
	 * Creates a new token entry with the primary key. Does not add the token entry to the database.
	 *
	 * @param tokenEntryId the primary key for the new token entry
	 * @return the new token entry
	 */
	public TokenEntry create(long tokenEntryId);

	/**
	 * Removes the token entry with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry that was removed
	 * @throws NoSuchTokenEntryException if a token entry with the primary key could not be found
	 */
	public TokenEntry remove(long tokenEntryId)
		throws NoSuchTokenEntryException;

	public TokenEntry updateImpl(TokenEntry tokenEntry);

	/**
	 * Returns the token entry with the primary key or throws a <code>NoSuchTokenEntryException</code> if it could not be found.
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry
	 * @throws NoSuchTokenEntryException if a token entry with the primary key could not be found
	 */
	public TokenEntry findByPrimaryKey(long tokenEntryId)
		throws NoSuchTokenEntryException;

	/**
	 * Returns the token entry with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry, or <code>null</code> if a token entry with the primary key could not be found
	 */
	public TokenEntry fetchByPrimaryKey(long tokenEntryId);

	/**
	 * Returns the token entry where groupId = &#63; and userId = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @return the matching token entry, or <code>null</code> if a matching token entry could not be found
	 */
	public default TokenEntry fetchByG_U(long groupId, long userId) {
		return fetchByG_U(groupId, userId, true);
	}

	/**
	 * Returns all the token entries where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @return the matching token entries
	 */
	public default java.util.List<TokenEntry> findByUserId(long userId) {
		return findByUserId(
			userId, com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS,
			com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS, null, true);
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
	public default java.util.List<TokenEntry> findByUserId(
		long userId, int start, int end) {

		return findByUserId(userId, start, end, null, true);
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
	public default java.util.List<TokenEntry> findByUserId(
		long userId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<TokenEntry>
			orderByComparator) {

		return findByUserId(userId, start, end, orderByComparator, true);
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:1260887903