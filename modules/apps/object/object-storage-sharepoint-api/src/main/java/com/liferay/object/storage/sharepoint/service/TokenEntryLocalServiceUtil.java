/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.storage.sharepoint.service;

import com.liferay.object.storage.sharepoint.model.TokenEntry;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.Serializable;

import java.util.List;

/**
 * Provides the local service utility for TokenEntry. This utility wraps
 * <code>com.liferay.object.storage.sharepoint.service.impl.TokenEntryLocalServiceImpl</code> and
 * is an access point for service operations in application layer code running
 * on the local server. Methods of this service will not have security checks
 * based on the propagated JAAS credentials because this service can only be
 * accessed from within the same VM.
 *
 * @author Jürgen Kappler
 * @see TokenEntryLocalService
 * @generated
 */
public class TokenEntryLocalServiceUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to <code>com.liferay.object.storage.sharepoint.service.impl.TokenEntryLocalServiceImpl</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static TokenEntry addTokenEntry(
			String accessToken, java.util.Date expirationDate, long groupId,
			String refreshToken, long userId)
		throws PortalException {

		return getService().addTokenEntry(
			accessToken, expirationDate, groupId, refreshToken, userId);
	}

	/**
	 * Adds the token entry to the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect TokenEntryLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param tokenEntry the token entry
	 * @return the token entry that was added
	 */
	public static TokenEntry addTokenEntry(TokenEntry tokenEntry) {
		return getService().addTokenEntry(tokenEntry);
	}

	/**
	 * @throws PortalException
	 */
	public static PersistedModel createPersistedModel(
			Serializable primaryKeyObj)
		throws PortalException {

		return getService().createPersistedModel(primaryKeyObj);
	}

	/**
	 * Creates a new token entry with the primary key. Does not add the token entry to the database.
	 *
	 * @param tokenEntryId the primary key for the new token entry
	 * @return the new token entry
	 */
	public static TokenEntry createTokenEntry(long tokenEntryId) {
		return getService().createTokenEntry(tokenEntryId);
	}

	/**
	 * @throws PortalException
	 */
	public static PersistedModel deletePersistedModel(
			PersistedModel persistedModel)
		throws PortalException {

		return getService().deletePersistedModel(persistedModel);
	}

	/**
	 * Deletes the token entry with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect TokenEntryLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry that was removed
	 * @throws PortalException if a token entry with the primary key could not be found
	 */
	public static TokenEntry deleteTokenEntry(long tokenEntryId)
		throws PortalException {

		return getService().deleteTokenEntry(tokenEntryId);
	}

	/**
	 * Deletes the token entry from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect TokenEntryLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param tokenEntry the token entry
	 * @return the token entry that was removed
	 */
	public static TokenEntry deleteTokenEntry(TokenEntry tokenEntry) {
		return getService().deleteTokenEntry(tokenEntry);
	}

	public static <T> T dslQuery(DSLQuery dslQuery) {
		return getService().dslQuery(dslQuery);
	}

	public static int dslQueryCount(DSLQuery dslQuery) {
		return getService().dslQueryCount(dslQuery);
	}

	public static DynamicQuery dynamicQuery() {
		return getService().dynamicQuery();
	}

	/**
	 * Performs a dynamic query on the database and returns the matching rows.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the matching rows
	 */
	public static <T> List<T> dynamicQuery(DynamicQuery dynamicQuery) {
		return getService().dynamicQuery(dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.object.storage.sharepoint.model.impl.TokenEntryModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @return the range of matching rows
	 */
	public static <T> List<T> dynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getService().dynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.object.storage.sharepoint.model.impl.TokenEntryModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching rows
	 */
	public static <T> List<T> dynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<T> orderByComparator) {

		return getService().dynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the number of rows matching the dynamic query
	 */
	public static long dynamicQueryCount(DynamicQuery dynamicQuery) {
		return getService().dynamicQueryCount(dynamicQuery);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @param projection the projection to apply to the query
	 * @return the number of rows matching the dynamic query
	 */
	public static long dynamicQueryCount(
		DynamicQuery dynamicQuery,
		com.liferay.portal.kernel.dao.orm.Projection projection) {

		return getService().dynamicQueryCount(dynamicQuery, projection);
	}

	public static TokenEntry fetchTokenEntry(long tokenEntryId) {
		return getService().fetchTokenEntry(tokenEntryId);
	}

	public static TokenEntry fetchTokenEntry(long groupId, long userId) {
		return getService().fetchTokenEntry(groupId, userId);
	}

	public static com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return getService().getActionableDynamicQuery();
	}

	public static
		com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
			getIndexableActionableDynamicQuery() {

		return getService().getIndexableActionableDynamicQuery();
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	public static String getOSGiServiceIdentifier() {
		return getService().getOSGiServiceIdentifier();
	}

	/**
	 * @throws PortalException
	 */
	public static PersistedModel getPersistedModel(Serializable primaryKeyObj)
		throws PortalException {

		return getService().getPersistedModel(primaryKeyObj);
	}

	/**
	 * Returns a range of all the token entries.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.object.storage.sharepoint.model.impl.TokenEntryModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of token entries
	 * @param end the upper bound of the range of token entries (not inclusive)
	 * @return the range of token entries
	 */
	public static List<TokenEntry> getTokenEntries(int start, int end) {
		return getService().getTokenEntries(start, end);
	}

	/**
	 * Returns the number of token entries.
	 *
	 * @return the number of token entries
	 */
	public static int getTokenEntriesCount() {
		return getService().getTokenEntriesCount();
	}

	/**
	 * Returns the token entry with the primary key.
	 *
	 * @param tokenEntryId the primary key of the token entry
	 * @return the token entry
	 * @throws PortalException if a token entry with the primary key could not be found
	 */
	public static TokenEntry getTokenEntry(long tokenEntryId)
		throws PortalException {

		return getService().getTokenEntry(tokenEntryId);
	}

	/**
	 * Updates the token entry in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect TokenEntryLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param tokenEntry the token entry
	 * @return the token entry that was updated
	 */
	public static TokenEntry updateTokenEntry(TokenEntry tokenEntry) {
		return getService().updateTokenEntry(tokenEntry);
	}

	public static TokenEntryLocalService getService() {
		return _serviceSnapshot.get();
	}

	private static final Snapshot<TokenEntryLocalService> _serviceSnapshot =
		new Snapshot<>(
			TokenEntryLocalServiceUtil.class, TokenEntryLocalService.class);

}
// LIFERAY-SERVICE-BUILDER-HASH:2062949037