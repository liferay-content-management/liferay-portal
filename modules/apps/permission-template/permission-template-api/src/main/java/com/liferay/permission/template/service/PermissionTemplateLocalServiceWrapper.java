/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.permission.template.service;

import com.liferay.permission.template.model.PermissionTemplate;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.persistence.BasePersistence;
import com.liferay.portal.kernel.service.persistence.change.tracking.CTPersistence;

/**
 * Provides a wrapper for {@link PermissionTemplateLocalService}.
 *
 * @author Brian Wing Shun Chan
 * @see PermissionTemplateLocalService
 * @generated
 */
public class PermissionTemplateLocalServiceWrapper
	implements PermissionTemplateLocalService,
			   ServiceWrapper<PermissionTemplateLocalService> {

	public PermissionTemplateLocalServiceWrapper() {
		this(null);
	}

	public PermissionTemplateLocalServiceWrapper(
		PermissionTemplateLocalService permissionTemplateLocalService) {

		_permissionTemplateLocalService = permissionTemplateLocalService;
	}

	@Override
	public PermissionTemplate addPermissionTemplate(
		long companyId, long groupId, String className, long classPK,
		boolean permissionTemplateEnabled) {

		return _permissionTemplateLocalService.addPermissionTemplate(
			companyId, groupId, className, classPK, permissionTemplateEnabled);
	}

	/**
	 * Adds the permission template to the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PermissionTemplateLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param permissionTemplate the permission template
	 * @return the permission template that was added
	 */
	@Override
	public PermissionTemplate addPermissionTemplate(
		PermissionTemplate permissionTemplate) {

		return _permissionTemplateLocalService.addPermissionTemplate(
			permissionTemplate);
	}

	/**
	 * Creates a new permission template with the primary key. Does not add the permission template to the database.
	 *
	 * @param permissionTemplateId the primary key for the new permission template
	 * @return the new permission template
	 */
	@Override
	public PermissionTemplate createPermissionTemplate(
		long permissionTemplateId) {

		return _permissionTemplateLocalService.createPermissionTemplate(
			permissionTemplateId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel createPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _permissionTemplateLocalService.createPersistedModel(
			primaryKeyObj);
	}

	/**
	 * Deletes the permission template with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PermissionTemplateLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template that was removed
	 * @throws PortalException if a permission template with the primary key could not be found
	 */
	@Override
	public PermissionTemplate deletePermissionTemplate(
			long permissionTemplateId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _permissionTemplateLocalService.deletePermissionTemplate(
			permissionTemplateId);
	}

	/**
	 * Deletes the permission template from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PermissionTemplateLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param permissionTemplate the permission template
	 * @return the permission template that was removed
	 */
	@Override
	public PermissionTemplate deletePermissionTemplate(
		PermissionTemplate permissionTemplate) {

		return _permissionTemplateLocalService.deletePermissionTemplate(
			permissionTemplate);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel deletePersistedModel(
			com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _permissionTemplateLocalService.deletePersistedModel(
			persistedModel);
	}

	@Override
	public <T> T dslQuery(com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {
		return _permissionTemplateLocalService.dslQuery(dslQuery);
	}

	@Override
	public int dslQueryCount(
		com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {

		return _permissionTemplateLocalService.dslQueryCount(dslQuery);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery() {
		return _permissionTemplateLocalService.dynamicQuery();
	}

	/**
	 * Performs a dynamic query on the database and returns the matching rows.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the matching rows
	 */
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return _permissionTemplateLocalService.dynamicQuery(dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.permission.template.model.impl.PermissionTemplateModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @return the range of matching rows
	 */
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) {

		return _permissionTemplateLocalService.dynamicQuery(
			dynamicQuery, start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.permission.template.model.impl.PermissionTemplateModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching rows
	 */
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end,
		com.liferay.portal.kernel.util.OrderByComparator<T> orderByComparator) {

		return _permissionTemplateLocalService.dynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the number of rows matching the dynamic query
	 */
	@Override
	public long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return _permissionTemplateLocalService.dynamicQueryCount(dynamicQuery);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @param projection the projection to apply to the query
	 * @return the number of rows matching the dynamic query
	 */
	@Override
	public long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery,
		com.liferay.portal.kernel.dao.orm.Projection projection) {

		return _permissionTemplateLocalService.dynamicQueryCount(
			dynamicQuery, projection);
	}

	@Override
	public PermissionTemplate fetchPermissionTemplate(
		long permissionTemplateId) {

		return _permissionTemplateLocalService.fetchPermissionTemplate(
			permissionTemplateId);
	}

	@Override
	public PermissionTemplate fetchPermissionTemplate(
		String className, long classPK) {

		return _permissionTemplateLocalService.fetchPermissionTemplate(
			className, classPK);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return _permissionTemplateLocalService.getActionableDynamicQuery();
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
		getIndexableActionableDynamicQuery() {

		return _permissionTemplateLocalService.
			getIndexableActionableDynamicQuery();
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _permissionTemplateLocalService.getOSGiServiceIdentifier();
	}

	/**
	 * Returns the permission template with the primary key.
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template
	 * @throws PortalException if a permission template with the primary key could not be found
	 */
	@Override
	public PermissionTemplate getPermissionTemplate(long permissionTemplateId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _permissionTemplateLocalService.getPermissionTemplate(
			permissionTemplateId);
	}

	/**
	 * Returns a range of all the permission templates.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.permission.template.model.impl.PermissionTemplateModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of permission templates
	 * @param end the upper bound of the range of permission templates (not inclusive)
	 * @return the range of permission templates
	 */
	@Override
	public java.util.List<PermissionTemplate> getPermissionTemplates(
		int start, int end) {

		return _permissionTemplateLocalService.getPermissionTemplates(
			start, end);
	}

	/**
	 * Returns the number of permission templates.
	 *
	 * @return the number of permission templates
	 */
	@Override
	public int getPermissionTemplatesCount() {
		return _permissionTemplateLocalService.getPermissionTemplatesCount();
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel getPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _permissionTemplateLocalService.getPersistedModel(primaryKeyObj);
	}

	@Override
	public PermissionTemplate updatePermissionTemplate(
		long companyId, long groupId, String className, long classPK,
		boolean permissionTemplateEnabled) {

		return _permissionTemplateLocalService.updatePermissionTemplate(
			companyId, groupId, className, classPK, permissionTemplateEnabled);
	}

	/**
	 * Updates the permission template in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PermissionTemplateLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param permissionTemplate the permission template
	 * @return the permission template that was updated
	 */
	@Override
	public PermissionTemplate updatePermissionTemplate(
		PermissionTemplate permissionTemplate) {

		return _permissionTemplateLocalService.updatePermissionTemplate(
			permissionTemplate);
	}

	@Override
	public BasePersistence<?> getBasePersistence() {
		return _permissionTemplateLocalService.getBasePersistence();
	}

	@Override
	public CTPersistence<PermissionTemplate> getCTPersistence() {
		return _permissionTemplateLocalService.getCTPersistence();
	}

	@Override
	public Class<PermissionTemplate> getModelClass() {
		return _permissionTemplateLocalService.getModelClass();
	}

	@Override
	public <R, E extends Throwable> R updateWithUnsafeFunction(
			UnsafeFunction<CTPersistence<PermissionTemplate>, R, E>
				updateUnsafeFunction)
		throws E {

		return _permissionTemplateLocalService.updateWithUnsafeFunction(
			updateUnsafeFunction);
	}

	@Override
	public PermissionTemplateLocalService getWrappedService() {
		return _permissionTemplateLocalService;
	}

	@Override
	public void setWrappedService(
		PermissionTemplateLocalService permissionTemplateLocalService) {

		_permissionTemplateLocalService = permissionTemplateLocalService;
	}

	private PermissionTemplateLocalService _permissionTemplateLocalService;

}