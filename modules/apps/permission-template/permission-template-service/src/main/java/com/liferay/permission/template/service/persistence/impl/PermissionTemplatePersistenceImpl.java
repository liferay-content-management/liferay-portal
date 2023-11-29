/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.permission.template.service.persistence.impl;

import com.liferay.permission.template.exception.NoSuchPermissionTemplateException;
import com.liferay.permission.template.model.PermissionTemplate;
import com.liferay.permission.template.model.PermissionTemplateTable;
import com.liferay.permission.template.model.impl.PermissionTemplateImpl;
import com.liferay.permission.template.model.impl.PermissionTemplateModelImpl;
import com.liferay.permission.template.service.persistence.PermissionTemplatePersistence;
import com.liferay.permission.template.service.persistence.PermissionTemplateUtil;
import com.liferay.permission.template.service.persistence.impl.constants.PermissionTemplatePersistenceConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.change.tracking.CTColumnResolutionType;
import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.Query;
import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.dao.orm.SessionFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.persistence.change.tracking.helper.CTPersistenceHelper;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.ProxyUtil;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The persistence implementation for the permission template service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
@Component(service = PermissionTemplatePersistence.class)
public class PermissionTemplatePersistenceImpl
	extends BasePersistenceImpl<PermissionTemplate>
	implements PermissionTemplatePersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>PermissionTemplateUtil</code> to access the permission template persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		PermissionTemplateImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private FinderPath _finderPathWithPaginationFindAll;
	private FinderPath _finderPathWithoutPaginationFindAll;
	private FinderPath _finderPathCountAll;
	private FinderPath _finderPathFetchByC_C;
	private FinderPath _finderPathCountByC_C;

	/**
	 * Returns the permission template where classNameId = &#63; and classPK = &#63; or throws a <code>NoSuchPermissionTemplateException</code> if it could not be found.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching permission template
	 * @throws NoSuchPermissionTemplateException if a matching permission template could not be found
	 */
	@Override
	public PermissionTemplate findByC_C(long classNameId, long classPK)
		throws NoSuchPermissionTemplateException {

		PermissionTemplate permissionTemplate = fetchByC_C(
			classNameId, classPK);

		if (permissionTemplate == null) {
			StringBundler sb = new StringBundler(6);

			sb.append(_NO_SUCH_ENTITY_WITH_KEY);

			sb.append("classNameId=");
			sb.append(classNameId);

			sb.append(", classPK=");
			sb.append(classPK);

			sb.append("}");

			if (_log.isDebugEnabled()) {
				_log.debug(sb.toString());
			}

			throw new NoSuchPermissionTemplateException(sb.toString());
		}

		return permissionTemplate;
	}

	/**
	 * Returns the permission template where classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching permission template, or <code>null</code> if a matching permission template could not be found
	 */
	@Override
	public PermissionTemplate fetchByC_C(long classNameId, long classPK) {
		return fetchByC_C(classNameId, classPK, true);
	}

	/**
	 * Returns the permission template where classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching permission template, or <code>null</code> if a matching permission template could not be found
	 */
	@Override
	public PermissionTemplate fetchByC_C(
		long classNameId, long classPK, boolean useFinderCache) {

		Object[] finderArgs = null;

		if (useFinderCache) {
			finderArgs = new Object[] {classNameId, classPK};
		}

		Object result = null;

		if (useFinderCache) {
			result = finderCache.getResult(
				_finderPathFetchByC_C, finderArgs, this);
		}

		boolean productionMode = ctPersistenceHelper.isProductionMode(
			PermissionTemplate.class);

		if (result instanceof PermissionTemplate) {
			PermissionTemplate permissionTemplate = (PermissionTemplate)result;

			if ((classNameId != permissionTemplate.getClassNameId()) ||
				(classPK != permissionTemplate.getClassPK())) {

				result = null;
			}
			else if (!ctPersistenceHelper.isProductionMode(
						PermissionTemplate.class,
						permissionTemplate.getPrimaryKey())) {

				result = null;
			}
		}
		else if (!productionMode && (result instanceof List<?>)) {
			result = null;
		}

		if (result == null) {
			StringBundler sb = new StringBundler(4);

			sb.append(_SQL_SELECT_PERMISSIONTEMPLATE_WHERE);

			sb.append(_FINDER_COLUMN_C_C_CLASSNAMEID_2);

			sb.append(_FINDER_COLUMN_C_C_CLASSPK_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(classNameId);

				queryPos.add(classPK);

				List<PermissionTemplate> list = query.list();

				if (list.isEmpty()) {
					if (useFinderCache && productionMode) {
						finderCache.putResult(
							_finderPathFetchByC_C, finderArgs, list);
					}
				}
				else {
					PermissionTemplate permissionTemplate = list.get(0);

					result = permissionTemplate;

					cacheResult(permissionTemplate);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		if (result instanceof List<?>) {
			return null;
		}
		else {
			return (PermissionTemplate)result;
		}
	}

	/**
	 * Removes the permission template where classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the permission template that was removed
	 */
	@Override
	public PermissionTemplate removeByC_C(long classNameId, long classPK)
		throws NoSuchPermissionTemplateException {

		PermissionTemplate permissionTemplate = findByC_C(classNameId, classPK);

		return remove(permissionTemplate);
	}

	/**
	 * Returns the number of permission templates where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching permission templates
	 */
	@Override
	public int countByC_C(long classNameId, long classPK) {
		boolean productionMode = ctPersistenceHelper.isProductionMode(
			PermissionTemplate.class);

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		Long count = null;

		if (productionMode) {
			finderPath = _finderPathCountByC_C;

			finderArgs = new Object[] {classNameId, classPK};

			count = (Long)finderCache.getResult(finderPath, finderArgs, this);
		}

		if (count == null) {
			StringBundler sb = new StringBundler(3);

			sb.append(_SQL_COUNT_PERMISSIONTEMPLATE_WHERE);

			sb.append(_FINDER_COLUMN_C_C_CLASSNAMEID_2);

			sb.append(_FINDER_COLUMN_C_C_CLASSPK_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(classNameId);

				queryPos.add(classPK);

				count = (Long)query.uniqueResult();

				if (productionMode) {
					finderCache.putResult(finderPath, finderArgs, count);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_C_C_CLASSNAMEID_2 =
		"permissionTemplate.classNameId = ? AND ";

	private static final String _FINDER_COLUMN_C_C_CLASSPK_2 =
		"permissionTemplate.classPK = ?";

	public PermissionTemplatePersistenceImpl() {
		setModelClass(PermissionTemplate.class);

		setModelImplClass(PermissionTemplateImpl.class);
		setModelPKClass(long.class);

		setTable(PermissionTemplateTable.INSTANCE);
	}

	/**
	 * Caches the permission template in the entity cache if it is enabled.
	 *
	 * @param permissionTemplate the permission template
	 */
	@Override
	public void cacheResult(PermissionTemplate permissionTemplate) {
		if (permissionTemplate.getCtCollectionId() != 0) {
			return;
		}

		entityCache.putResult(
			PermissionTemplateImpl.class, permissionTemplate.getPrimaryKey(),
			permissionTemplate);

		finderCache.putResult(
			_finderPathFetchByC_C,
			new Object[] {
				permissionTemplate.getClassNameId(),
				permissionTemplate.getClassPK()
			},
			permissionTemplate);
	}

	private int _valueObjectFinderCacheListThreshold;

	/**
	 * Caches the permission templates in the entity cache if it is enabled.
	 *
	 * @param permissionTemplates the permission templates
	 */
	@Override
	public void cacheResult(List<PermissionTemplate> permissionTemplates) {
		if ((_valueObjectFinderCacheListThreshold == 0) ||
			((_valueObjectFinderCacheListThreshold > 0) &&
			 (permissionTemplates.size() >
				 _valueObjectFinderCacheListThreshold))) {

			return;
		}

		for (PermissionTemplate permissionTemplate : permissionTemplates) {
			if (permissionTemplate.getCtCollectionId() != 0) {
				continue;
			}

			if (entityCache.getResult(
					PermissionTemplateImpl.class,
					permissionTemplate.getPrimaryKey()) == null) {

				cacheResult(permissionTemplate);
			}
		}
	}

	/**
	 * Clears the cache for all permission templates.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache() {
		entityCache.clearCache(PermissionTemplateImpl.class);

		finderCache.clearCache(PermissionTemplateImpl.class);
	}

	/**
	 * Clears the cache for the permission template.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache(PermissionTemplate permissionTemplate) {
		entityCache.removeResult(
			PermissionTemplateImpl.class, permissionTemplate);
	}

	@Override
	public void clearCache(List<PermissionTemplate> permissionTemplates) {
		for (PermissionTemplate permissionTemplate : permissionTemplates) {
			entityCache.removeResult(
				PermissionTemplateImpl.class, permissionTemplate);
		}
	}

	@Override
	public void clearCache(Set<Serializable> primaryKeys) {
		finderCache.clearCache(PermissionTemplateImpl.class);

		for (Serializable primaryKey : primaryKeys) {
			entityCache.removeResult(PermissionTemplateImpl.class, primaryKey);
		}
	}

	protected void cacheUniqueFindersCache(
		PermissionTemplateModelImpl permissionTemplateModelImpl) {

		Object[] args = new Object[] {
			permissionTemplateModelImpl.getClassNameId(),
			permissionTemplateModelImpl.getClassPK()
		};

		finderCache.putResult(_finderPathCountByC_C, args, Long.valueOf(1));
		finderCache.putResult(
			_finderPathFetchByC_C, args, permissionTemplateModelImpl);
	}

	/**
	 * Creates a new permission template with the primary key. Does not add the permission template to the database.
	 *
	 * @param permissionTemplateId the primary key for the new permission template
	 * @return the new permission template
	 */
	@Override
	public PermissionTemplate create(long permissionTemplateId) {
		PermissionTemplate permissionTemplate = new PermissionTemplateImpl();

		permissionTemplate.setNew(true);
		permissionTemplate.setPrimaryKey(permissionTemplateId);

		permissionTemplate.setCompanyId(CompanyThreadLocal.getCompanyId());

		return permissionTemplate;
	}

	/**
	 * Removes the permission template with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template that was removed
	 * @throws NoSuchPermissionTemplateException if a permission template with the primary key could not be found
	 */
	@Override
	public PermissionTemplate remove(long permissionTemplateId)
		throws NoSuchPermissionTemplateException {

		return remove((Serializable)permissionTemplateId);
	}

	/**
	 * Removes the permission template with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param primaryKey the primary key of the permission template
	 * @return the permission template that was removed
	 * @throws NoSuchPermissionTemplateException if a permission template with the primary key could not be found
	 */
	@Override
	public PermissionTemplate remove(Serializable primaryKey)
		throws NoSuchPermissionTemplateException {

		Session session = null;

		try {
			session = openSession();

			PermissionTemplate permissionTemplate =
				(PermissionTemplate)session.get(
					PermissionTemplateImpl.class, primaryKey);

			if (permissionTemplate == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
				}

				throw new NoSuchPermissionTemplateException(
					_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			return remove(permissionTemplate);
		}
		catch (NoSuchPermissionTemplateException noSuchEntityException) {
			throw noSuchEntityException;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	protected PermissionTemplate removeImpl(
		PermissionTemplate permissionTemplate) {

		Session session = null;

		try {
			session = openSession();

			if (!session.contains(permissionTemplate)) {
				permissionTemplate = (PermissionTemplate)session.get(
					PermissionTemplateImpl.class,
					permissionTemplate.getPrimaryKeyObj());
			}

			if ((permissionTemplate != null) &&
				ctPersistenceHelper.isRemove(permissionTemplate)) {

				session.delete(permissionTemplate);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (permissionTemplate != null) {
			clearCache(permissionTemplate);
		}

		return permissionTemplate;
	}

	@Override
	public PermissionTemplate updateImpl(
		PermissionTemplate permissionTemplate) {

		boolean isNew = permissionTemplate.isNew();

		if (!(permissionTemplate instanceof PermissionTemplateModelImpl)) {
			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(permissionTemplate.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					permissionTemplate);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in permissionTemplate proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom PermissionTemplate implementation " +
					permissionTemplate.getClass());
		}

		PermissionTemplateModelImpl permissionTemplateModelImpl =
			(PermissionTemplateModelImpl)permissionTemplate;

		Session session = null;

		try {
			session = openSession();

			if (ctPersistenceHelper.isInsert(permissionTemplate)) {
				if (!isNew) {
					session.evict(
						PermissionTemplateImpl.class,
						permissionTemplate.getPrimaryKeyObj());
				}

				session.save(permissionTemplate);
			}
			else {
				permissionTemplate = (PermissionTemplate)session.merge(
					permissionTemplate);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (permissionTemplate.getCtCollectionId() != 0) {
			if (isNew) {
				permissionTemplate.setNew(false);
			}

			permissionTemplate.resetOriginalValues();

			return permissionTemplate;
		}

		entityCache.putResult(
			PermissionTemplateImpl.class, permissionTemplateModelImpl, false,
			true);

		cacheUniqueFindersCache(permissionTemplateModelImpl);

		if (isNew) {
			permissionTemplate.setNew(false);
		}

		permissionTemplate.resetOriginalValues();

		return permissionTemplate;
	}

	/**
	 * Returns the permission template with the primary key or throws a <code>com.liferay.portal.kernel.exception.NoSuchModelException</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the permission template
	 * @return the permission template
	 * @throws NoSuchPermissionTemplateException if a permission template with the primary key could not be found
	 */
	@Override
	public PermissionTemplate findByPrimaryKey(Serializable primaryKey)
		throws NoSuchPermissionTemplateException {

		PermissionTemplate permissionTemplate = fetchByPrimaryKey(primaryKey);

		if (permissionTemplate == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			throw new NoSuchPermissionTemplateException(
				_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
		}

		return permissionTemplate;
	}

	/**
	 * Returns the permission template with the primary key or throws a <code>NoSuchPermissionTemplateException</code> if it could not be found.
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template
	 * @throws NoSuchPermissionTemplateException if a permission template with the primary key could not be found
	 */
	@Override
	public PermissionTemplate findByPrimaryKey(long permissionTemplateId)
		throws NoSuchPermissionTemplateException {

		return findByPrimaryKey((Serializable)permissionTemplateId);
	}

	/**
	 * Returns the permission template with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the permission template
	 * @return the permission template, or <code>null</code> if a permission template with the primary key could not be found
	 */
	@Override
	public PermissionTemplate fetchByPrimaryKey(Serializable primaryKey) {
		if (ctPersistenceHelper.isProductionMode(
				PermissionTemplate.class, primaryKey)) {

			return super.fetchByPrimaryKey(primaryKey);
		}

		PermissionTemplate permissionTemplate = null;

		Session session = null;

		try {
			session = openSession();

			permissionTemplate = (PermissionTemplate)session.get(
				PermissionTemplateImpl.class, primaryKey);

			if (permissionTemplate != null) {
				cacheResult(permissionTemplate);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		return permissionTemplate;
	}

	/**
	 * Returns the permission template with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param permissionTemplateId the primary key of the permission template
	 * @return the permission template, or <code>null</code> if a permission template with the primary key could not be found
	 */
	@Override
	public PermissionTemplate fetchByPrimaryKey(long permissionTemplateId) {
		return fetchByPrimaryKey((Serializable)permissionTemplateId);
	}

	@Override
	public Map<Serializable, PermissionTemplate> fetchByPrimaryKeys(
		Set<Serializable> primaryKeys) {

		if (ctPersistenceHelper.isProductionMode(PermissionTemplate.class)) {
			return super.fetchByPrimaryKeys(primaryKeys);
		}

		if (primaryKeys.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<Serializable, PermissionTemplate> map =
			new HashMap<Serializable, PermissionTemplate>();

		if (primaryKeys.size() == 1) {
			Iterator<Serializable> iterator = primaryKeys.iterator();

			Serializable primaryKey = iterator.next();

			PermissionTemplate permissionTemplate = fetchByPrimaryKey(
				primaryKey);

			if (permissionTemplate != null) {
				map.put(primaryKey, permissionTemplate);
			}

			return map;
		}

		if ((databaseInMaxParameters > 0) &&
			(primaryKeys.size() > databaseInMaxParameters)) {

			Iterator<Serializable> iterator = primaryKeys.iterator();

			while (iterator.hasNext()) {
				Set<Serializable> page = new HashSet<>();

				for (int i = 0;
					 (i < databaseInMaxParameters) && iterator.hasNext(); i++) {

					page.add(iterator.next());
				}

				map.putAll(fetchByPrimaryKeys(page));
			}

			return map;
		}

		StringBundler sb = new StringBundler((primaryKeys.size() * 2) + 1);

		sb.append(getSelectSQL());
		sb.append(" WHERE ");
		sb.append(getPKDBName());
		sb.append(" IN (");

		for (Serializable primaryKey : primaryKeys) {
			sb.append((long)primaryKey);

			sb.append(",");
		}

		sb.setIndex(sb.index() - 1);

		sb.append(")");

		String sql = sb.toString();

		Session session = null;

		try {
			session = openSession();

			Query query = session.createQuery(sql);

			for (PermissionTemplate permissionTemplate :
					(List<PermissionTemplate>)query.list()) {

				map.put(
					permissionTemplate.getPrimaryKeyObj(), permissionTemplate);

				cacheResult(permissionTemplate);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		return map;
	}

	/**
	 * Returns all the permission templates.
	 *
	 * @return the permission templates
	 */
	@Override
	public List<PermissionTemplate> findAll() {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
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
	@Override
	public List<PermissionTemplate> findAll(int start, int end) {
		return findAll(start, end, null);
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
	@Override
	public List<PermissionTemplate> findAll(
		int start, int end,
		OrderByComparator<PermissionTemplate> orderByComparator) {

		return findAll(start, end, orderByComparator, true);
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
	@Override
	public List<PermissionTemplate> findAll(
		int start, int end,
		OrderByComparator<PermissionTemplate> orderByComparator,
		boolean useFinderCache) {

		boolean productionMode = ctPersistenceHelper.isProductionMode(
			PermissionTemplate.class);

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache && productionMode) {
				finderPath = _finderPathWithoutPaginationFindAll;
				finderArgs = FINDER_ARGS_EMPTY;
			}
		}
		else if (useFinderCache && productionMode) {
			finderPath = _finderPathWithPaginationFindAll;
			finderArgs = new Object[] {start, end, orderByComparator};
		}

		List<PermissionTemplate> list = null;

		if (useFinderCache && productionMode) {
			list = (List<PermissionTemplate>)finderCache.getResult(
				finderPath, finderArgs, this);
		}

		if (list == null) {
			StringBundler sb = null;
			String sql = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					2 + (orderByComparator.getOrderByFields().length * 2));

				sb.append(_SQL_SELECT_PERMISSIONTEMPLATE);

				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);

				sql = sb.toString();
			}
			else {
				sql = _SQL_SELECT_PERMISSIONTEMPLATE;

				sql = sql.concat(PermissionTemplateModelImpl.ORDER_BY_JPQL);
			}

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				list = (List<PermissionTemplate>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache && productionMode) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Removes all the permission templates from the database.
	 *
	 */
	@Override
	public void removeAll() {
		for (PermissionTemplate permissionTemplate : findAll()) {
			remove(permissionTemplate);
		}
	}

	/**
	 * Returns the number of permission templates.
	 *
	 * @return the number of permission templates
	 */
	@Override
	public int countAll() {
		boolean productionMode = ctPersistenceHelper.isProductionMode(
			PermissionTemplate.class);

		Long count = null;

		if (productionMode) {
			count = (Long)finderCache.getResult(
				_finderPathCountAll, FINDER_ARGS_EMPTY, this);
		}

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(
					_SQL_COUNT_PERMISSIONTEMPLATE);

				count = (Long)query.uniqueResult();

				if (productionMode) {
					finderCache.putResult(
						_finderPathCountAll, FINDER_ARGS_EMPTY, count);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	@Override
	protected EntityCache getEntityCache() {
		return entityCache;
	}

	@Override
	protected String getPKDBName() {
		return "permissionTemplateId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_PERMISSIONTEMPLATE;
	}

	@Override
	public Set<String> getCTColumnNames(
		CTColumnResolutionType ctColumnResolutionType) {

		return _ctColumnNamesMap.getOrDefault(
			ctColumnResolutionType, Collections.emptySet());
	}

	@Override
	public List<String> getMappingTableNames() {
		return _mappingTableNames;
	}

	@Override
	public Map<String, Integer> getTableColumnsMap() {
		return PermissionTemplateModelImpl.TABLE_COLUMNS_MAP;
	}

	@Override
	public String getTableName() {
		return "PermissionTemplate";
	}

	@Override
	public List<String[]> getUniqueIndexColumnNames() {
		return _uniqueIndexColumnNames;
	}

	private static final Map<CTColumnResolutionType, Set<String>>
		_ctColumnNamesMap = new EnumMap<CTColumnResolutionType, Set<String>>(
			CTColumnResolutionType.class);
	private static final List<String> _mappingTableNames =
		new ArrayList<String>();
	private static final List<String[]> _uniqueIndexColumnNames =
		new ArrayList<String[]>();

	static {
		Set<String> ctControlColumnNames = new HashSet<String>();
		Set<String> ctStrictColumnNames = new HashSet<String>();

		ctControlColumnNames.add("mvccVersion");
		ctControlColumnNames.add("ctCollectionId");
		ctStrictColumnNames.add("groupId");
		ctStrictColumnNames.add("companyId");
		ctStrictColumnNames.add("classNameId");
		ctStrictColumnNames.add("classPK");
		ctStrictColumnNames.add("permissionTemplateEnabled");

		_ctColumnNamesMap.put(
			CTColumnResolutionType.CONTROL, ctControlColumnNames);
		_ctColumnNamesMap.put(
			CTColumnResolutionType.PK,
			Collections.singleton("permissionTemplateId"));
		_ctColumnNamesMap.put(
			CTColumnResolutionType.STRICT, ctStrictColumnNames);

		_uniqueIndexColumnNames.add(new String[] {"classNameId", "classPK"});
	}

	/**
	 * Initializes the permission template persistence.
	 */
	@Activate
	public void activate() {
		_valueObjectFinderCacheListThreshold = GetterUtil.getInteger(
			PropsUtil.get(PropsKeys.VALUE_OBJECT_FINDER_CACHE_LIST_THRESHOLD));

		_finderPathWithPaginationFindAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findAll", new String[0],
			new String[0], true);

		_finderPathWithoutPaginationFindAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findAll", new String[0],
			new String[0], true);

		_finderPathCountAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countAll",
			new String[0], new String[0], false);

		_finderPathFetchByC_C = new FinderPath(
			FINDER_CLASS_NAME_ENTITY, "fetchByC_C",
			new String[] {Long.class.getName(), Long.class.getName()},
			new String[] {"classNameId", "classPK"}, true);

		_finderPathCountByC_C = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByC_C",
			new String[] {Long.class.getName(), Long.class.getName()},
			new String[] {"classNameId", "classPK"}, false);

		PermissionTemplateUtil.setPersistence(this);
	}

	@Deactivate
	public void deactivate() {
		PermissionTemplateUtil.setPersistence(null);

		entityCache.removeCache(PermissionTemplateImpl.class.getName());
	}

	@Override
	@Reference(
		target = PermissionTemplatePersistenceConstants.SERVICE_CONFIGURATION_FILTER,
		unbind = "-"
	)
	public void setConfiguration(Configuration configuration) {
	}

	@Override
	@Reference(
		target = PermissionTemplatePersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Override
	@Reference(
		target = PermissionTemplatePersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@Reference
	protected CTPersistenceHelper ctPersistenceHelper;

	@Reference
	protected EntityCache entityCache;

	@Reference
	protected FinderCache finderCache;

	private static final String _SQL_SELECT_PERMISSIONTEMPLATE =
		"SELECT permissionTemplate FROM PermissionTemplate permissionTemplate";

	private static final String _SQL_SELECT_PERMISSIONTEMPLATE_WHERE =
		"SELECT permissionTemplate FROM PermissionTemplate permissionTemplate WHERE ";

	private static final String _SQL_COUNT_PERMISSIONTEMPLATE =
		"SELECT COUNT(permissionTemplate) FROM PermissionTemplate permissionTemplate";

	private static final String _SQL_COUNT_PERMISSIONTEMPLATE_WHERE =
		"SELECT COUNT(permissionTemplate) FROM PermissionTemplate permissionTemplate WHERE ";

	private static final String _ORDER_BY_ENTITY_ALIAS = "permissionTemplate.";

	private static final String _NO_SUCH_ENTITY_WITH_PRIMARY_KEY =
		"No PermissionTemplate exists with the primary key ";

	private static final String _NO_SUCH_ENTITY_WITH_KEY =
		"No PermissionTemplate exists with the key {";

	private static final Log _log = LogFactoryUtil.getLog(
		PermissionTemplatePersistenceImpl.class);

	@Override
	protected FinderCache getFinderCache() {
		return finderCache;
	}

}