/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.search.spi.model.index.contributor;

import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Registers {@link CMSContentTextSimilarityModelDocumentContributor} under each
 * object definition's own class name.
 *
 * <p>
 * Object entries are indexed at create/update time by a per-object-definition
 * indexer keyed on {@code objectDefinition.getClassName()} (a generated
 * {@code com.liferay.object.model.ObjectDefinition#...} class name), not by the
 * static {@code com.liferay.object.model.ObjectEntry} indexer. A
 * {@code ModelDocumentContributor} registered only under
 * {@code com.liferay.object.model.ObjectEntry} therefore runs on full reindex
 * but never at write time. This registrar tracks the per-definition indexers and
 * mirror-registers the text similarity contributor under each definition's class
 * name so the band signatures are also written when content is created or
 * updated. The contributor itself only contributes for CMS content entries.
 * </p>
 *
 * @author Mikel Lorza
 */
@Component(service = {})
public class CMSContentTextSimilarityContributorRegistrar {

	@Activate
	protected void activate(BundleContext bundleContext)
		throws InvalidSyntaxException {

		_bundleContext = bundleContext;

		_serviceTracker = new ServiceTracker<>(
			bundleContext,
			bundleContext.createFilter(
				StringBundler.concat(
					"(&(objectClass=", Indexer.class.getName(),
					")(indexer.class.name=",
					ObjectDefinitionConstants.
						CLASS_NAME_PREFIX_CUSTOM_OBJECT_DEFINITION,
					"*))")),
			new IndexerServiceTrackerCustomizer());

		_serviceTracker.open();
	}

	@Deactivate
	protected void deactivate() {
		if (_serviceTracker != null) {
			_serviceTracker.close();
		}
	}

	private BundleContext _bundleContext;
	private final CMSContentTextSimilarityModelDocumentContributor
		_modelDocumentContributor =
			new CMSContentTextSimilarityModelDocumentContributor();
	private ServiceTracker
		<Indexer<?>, ServiceRegistration<ModelDocumentContributor<?>>>
			_serviceTracker;

	private class IndexerServiceTrackerCustomizer
		implements ServiceTrackerCustomizer
			<Indexer<?>, ServiceRegistration<ModelDocumentContributor<?>>> {

		@Override
		public ServiceRegistration<ModelDocumentContributor<?>> addingService(
			ServiceReference<Indexer<?>> serviceReference) {

			Object className = serviceReference.getProperty(
				"indexer.class.name");

			if (className == null) {
				return null;
			}

			return _bundleContext.registerService(
				(Class<ModelDocumentContributor<?>>)
					(Class<?>)ModelDocumentContributor.class,
				_modelDocumentContributor,
				HashMapDictionaryBuilder.<String, Object>put(
					"indexer.class.name", className
				).build());
		}

		@Override
		public void modifiedService(
			ServiceReference<Indexer<?>> serviceReference,
			ServiceRegistration<ModelDocumentContributor<?>>
				serviceRegistration) {
		}

		@Override
		public void removedService(
			ServiceReference<Indexer<?>> serviceReference,
			ServiceRegistration<ModelDocumentContributor<?>>
				serviceRegistration) {

			if (serviceRegistration != null) {
				serviceRegistration.unregister();
			}

			_bundleContext.ungetService(serviceReference);
		}

	}

}