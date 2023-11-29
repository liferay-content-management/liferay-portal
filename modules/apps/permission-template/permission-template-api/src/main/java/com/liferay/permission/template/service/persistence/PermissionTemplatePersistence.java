/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.permission.template.service.persistence;

import com.liferay.permission.template.exception.NoSuchPermissionTemplateException;
import com.liferay.permission.template.model.PermissionTemplate;
import com.liferay.portal.kernel.service.persistence.BasePersistence;
import com.liferay.portal.kernel.service.persistence.change.tracking.CTPersistence;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the permission template service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see PermissionTemplateUtil
 * @generated
 */
@ProviderType
public interface PermissionTemplatePersistence
	extends BasePersistence<PermissionTemplate>,
			CTPersistence<PermissionTemplate> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link PermissionTemplateUtil} to access the permission template persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

	/**
	 * Returns the permission template where classNameId = &#63; and classPK = &#63; or throws a <code>NoSuchPermissionTemplateException</code> if it could not be found.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching permission template
	 * @throws NoSuchPermissionTemplateException if a matching permission template could not be found
	 */
	public PermissionTemplate findByC_C(long classNameId, long classPK)
		throws NoSuchPermissionTemplateException;

	/**
	 * Returns the permission template where classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching permission template, or <code>null</code> if a matching permission template could not be found
	 */
	public PermissionTemplate fetchByC_C(long classNameId, long classPK);

	/**
	 * Returns the permission template where classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching permission template, or <code>null</code> if a matching permission template could not be found
	 */
	public PermissionTemplate fetchByC_C(
		long classNameId, long classPK, boolean useFinderCache);

	/**
	 * Removes the permission template where classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the permission template that was removed
	 */
	public PermissionTemplate removeByC_C(long classNameId, long classPK)
		throws NoSuchPermissionTemplateException;

	/**
	 * Returns the number of permission templates where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching permission templates
	 */
	public int countByC_C(long classNameId, long classPK);

	/**
	 * Caches the permission template in the entity cache if it is enabled.
	 *
	 * @param permissionTemplate the permission template
	 */
	public void cacheResult(PermissionTemplate permissionTemplate);

	/**
	 * Caches the permission templates in the entity cache if it is enabled.
	 *
	 * @param permissionTemplates the permission templates
	 */
	public void cacheResult(
		java.util.List<PermissionTemplate> permissionTemplates);

	/**
	 * Creates a new permission template with the primary key. Does not add the permission template to the database.
	 *
	 * @param permissionTemplateId the primary key for the new permission template
	 * @return the new permission template
	 */
	public PermissionTemplate create(long permissionTemplateId);

	/**
	 * Removes the permission template with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template that was removed
	 * @throws NoSuchPermissionTemplateException if a permission template with the primary key could not be found
	 */
	public PermissionTemplate remove(long permissionTemplateId)
		throws NoSuchPermissionTemplateException;

	public PermissionTemplate updateImpl(PermissionTemplate permissionTemplate);

	/**
	 * Returns the permission template with the primary key or throws a <code>NoSuchPermissionTemplateException</code> if it could not be found.
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template
	 * @throws NoSuchPermissionTemplateException if a permission template with the primary key could not be found
	 */
	public PermissionTemplate findByPrimaryKey(long permissionTemplateId)
		throws NoSuchPermissionTemplateException;

	/**
	 * Returns the permission template with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template, or <code>null</code> if a permission template with the primary key could not be found
	 */
	public PermissionTemplate fetchByPrimaryKey(long permissionTemplateId);

	/**
	 * Returns all the permission templates.
	 *
	 * @return the permission templates
	 */
	public java.util.List<PermissionTemplate> findAll();

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
	public java.util.List<PermissionTemplate> findAll(int start, int end);

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
	public java.util.List<PermissionTemplate> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PermissionTemplate>
			orderByComparator);

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
	public java.util.List<PermissionTemplate> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PermissionTemplate>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Removes all the permission templates from the database.
	 */
	public void removeAll();

	/**
	 * Returns the number of permission templates.
	 *
	 * @return the number of permission templates
	 */
	public int countAll();

}