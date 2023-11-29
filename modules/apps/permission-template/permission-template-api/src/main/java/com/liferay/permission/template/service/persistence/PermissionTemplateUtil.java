/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.permission.template.service.persistence;

import com.liferay.permission.template.model.PermissionTemplate;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The persistence utility for the permission template service. This utility wraps <code>com.liferay.permission.template.service.persistence.impl.PermissionTemplatePersistenceImpl</code> and provides direct access to the database for CRUD operations. This utility should only be used by the service layer, as it must operate within a transaction. Never access this utility in a JSP, controller, model, or other front-end class.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see PermissionTemplatePersistence
 * @generated
 */
public class PermissionTemplateUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache()
	 */
	public static void clearCache() {
		getPersistence().clearCache();
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static void clearCache(PermissionTemplate permissionTemplate) {
		getPersistence().clearCache(permissionTemplate);
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
	public static Map<Serializable, PermissionTemplate> fetchByPrimaryKeys(
		Set<Serializable> primaryKeys) {

		return getPersistence().fetchByPrimaryKeys(primaryKeys);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery)
	 */
	public static List<PermissionTemplate> findWithDynamicQuery(
		DynamicQuery dynamicQuery) {

		return getPersistence().findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int)
	 */
	public static List<PermissionTemplate> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getPersistence().findWithDynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int, OrderByComparator)
	 */
	public static List<PermissionTemplate> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<PermissionTemplate> orderByComparator) {

		return getPersistence().findWithDynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static PermissionTemplate update(
		PermissionTemplate permissionTemplate) {

		return getPersistence().update(permissionTemplate);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel, ServiceContext)
	 */
	public static PermissionTemplate update(
		PermissionTemplate permissionTemplate, ServiceContext serviceContext) {

		return getPersistence().update(permissionTemplate, serviceContext);
	}

	/**
	 * Caches the permission template in the entity cache if it is enabled.
	 *
	 * @param permissionTemplate the permission template
	 */
	public static void cacheResult(PermissionTemplate permissionTemplate) {
		getPersistence().cacheResult(permissionTemplate);
	}

	/**
	 * Caches the permission templates in the entity cache if it is enabled.
	 *
	 * @param permissionTemplates the permission templates
	 */
	public static void cacheResult(
		List<PermissionTemplate> permissionTemplates) {

		getPersistence().cacheResult(permissionTemplates);
	}

	/**
	 * Creates a new permission template with the primary key. Does not add the permission template to the database.
	 *
	 * @param permissionTemplateId the primary key for the new permission template
	 * @return the new permission template
	 */
	public static PermissionTemplate create(long permissionTemplateId) {
		return getPersistence().create(permissionTemplateId);
	}

	/**
	 * Removes the permission template with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template that was removed
	 * @throws NoSuchPermissionTemplateException if a permission template with the primary key could not be found
	 */
	public static PermissionTemplate remove(long permissionTemplateId)
		throws com.liferay.permission.template.exception.
			NoSuchPermissionTemplateException {

		return getPersistence().remove(permissionTemplateId);
	}

	public static PermissionTemplate updateImpl(
		PermissionTemplate permissionTemplate) {

		return getPersistence().updateImpl(permissionTemplate);
	}

	/**
	 * Returns the permission template with the primary key or throws a <code>NoSuchPermissionTemplateException</code> if it could not be found.
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template
	 * @throws NoSuchPermissionTemplateException if a permission template with the primary key could not be found
	 */
	public static PermissionTemplate findByPrimaryKey(long permissionTemplateId)
		throws com.liferay.permission.template.exception.
			NoSuchPermissionTemplateException {

		return getPersistence().findByPrimaryKey(permissionTemplateId);
	}

	/**
	 * Returns the permission template with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template, or <code>null</code> if a permission template with the primary key could not be found
	 */
	public static PermissionTemplate fetchByPrimaryKey(
		long permissionTemplateId) {

		return getPersistence().fetchByPrimaryKey(permissionTemplateId);
	}

	/**
	 * Returns all the permission templates.
	 *
	 * @return the permission templates
	 */
	public static List<PermissionTemplate> findAll() {
		return getPersistence().findAll();
	}

	/**
	 * Returns a range of all the permission templates.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PermissionTemplateModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of permission templates
	 * @param end the upper bound of the range of permission templates (not inclusive)
	 * @return the range of permission templates
	 */
	public static List<PermissionTemplate> findAll(int start, int end) {
		return getPersistence().findAll(start, end);
	}

	/**
	 * Returns an ordered range of all the permission templates.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PermissionTemplateModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of permission templates
	 * @param end the upper bound of the range of permission templates (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of permission templates
	 */
	public static List<PermissionTemplate> findAll(
		int start, int end,
		OrderByComparator<PermissionTemplate> orderByComparator) {

		return getPersistence().findAll(start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the permission templates.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PermissionTemplateModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of permission templates
	 * @param end the upper bound of the range of permission templates (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of permission templates
	 */
	public static List<PermissionTemplate> findAll(
		int start, int end,
		OrderByComparator<PermissionTemplate> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findAll(
			start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Removes all the permission templates from the database.
	 */
	public static void removeAll() {
		getPersistence().removeAll();
	}

	/**
	 * Returns the number of permission templates.
	 *
	 * @return the number of permission templates
	 */
	public static int countAll() {
		return getPersistence().countAll();
	}

	public static PermissionTemplatePersistence getPersistence() {
		return _persistence;
	}

	public static void setPersistence(
		PermissionTemplatePersistence persistence) {

		_persistence = persistence;
	}

	private static volatile PermissionTemplatePersistence _persistence;

}