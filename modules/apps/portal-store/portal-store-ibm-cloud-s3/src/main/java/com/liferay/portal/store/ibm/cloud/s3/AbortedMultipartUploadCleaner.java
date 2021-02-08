package com.liferay.portal.store.ibm.cloud.s3;

import com.ibm.cloud.objectstorage.services.s3.transfer.TransferManager;
import com.liferay.document.library.kernel.store.Store;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.DestinationNames;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.scheduler.SchedulerEngineHelper;
import com.liferay.portal.kernel.scheduler.SchedulerEntry;
import com.liferay.portal.kernel.scheduler.SchedulerEntryImpl;
import com.liferay.portal.kernel.scheduler.TimeUnit;
import com.liferay.portal.kernel.scheduler.Trigger;
import com.liferay.portal.kernel.scheduler.TriggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@Component(service = {})
public class AbortedMultipartUploadCleaner {

	@Activate
	protected void activate(BundleContext bundleContext)
		throws InvalidSyntaxException {

		_serviceTracker = new ServiceTracker<>(
			bundleContext,
			bundleContext.createFilter(
				StringBundler.concat(
					"(&(", Constants.OBJECTCLASS, "=", Store.class.getName(),
					")(store.type=", IBMCloudS3Store.class.getName(), "))")),
			new IBMCloudS3StoreServiceTrackerCustomizer(
				bundleContext, _schedulerEngineHelper, _triggerFactory));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTracker.close();
	}

	@Reference(unbind = "-")
	private SchedulerEngineHelper _schedulerEngineHelper;

	private ServiceTracker<IBMCloudS3Store, MessageListener> _serviceTracker;

	@Reference(unbind = "-")
	private TriggerFactory _triggerFactory;

	private static class AbortedMultipartUploadMessageListener
		extends BaseMessageListener {

		@Override
		protected void doReceive(Message message) throws Exception {
			TransferManager transferManager = _ibmCloudS3Store.getTransferManager();

			transferManager.abortMultipartUploads(
				_ibmCloudS3Store.getBucketName(), _computeStartDate());
		}

		private AbortedMultipartUploadMessageListener(IBMCloudS3Store ibmCloudS3Store) {
			_ibmCloudS3Store = ibmCloudS3Store;
		}

		private Date _computeStartDate() {
			Date date = new Date();

			LocalDateTime localDateTime = LocalDateTime.ofInstant(
				date.toInstant(), ZoneId.systemDefault());

			LocalDateTime previousDayLocalDateTime = localDateTime.minus(
				1, ChronoUnit.DAYS);

			ZonedDateTime zonedDateTime = previousDayLocalDateTime.atZone(
				ZoneId.systemDefault());

			return Date.from(zonedDateTime.toInstant());
		}

		private final IBMCloudS3Store _ibmCloudS3Store;

	}

	private static class IBMCloudS3StoreServiceTrackerCustomizer
		implements ServiceTrackerCustomizer<IBMCloudS3Store, MessageListener> {

		@Override
		public MessageListener addingService(
			ServiceReference<IBMCloudS3Store> serviceReference) {

			MessageListener messageListener =
				new AbortedMultipartUploadMessageListener(
					_bundleContext.getService(serviceReference));

			Class<?> clazz = getClass();

			String className = clazz.getName();

			Trigger trigger = _triggerFactory.createTrigger(
				className, className, null, null, 1, TimeUnit.DAY);

			SchedulerEntry schedulerEntry = new SchedulerEntryImpl(
				className, trigger);

			_schedulerEngineHelper.register(
				messageListener, schedulerEntry,
				DestinationNames.SCHEDULER_DISPATCH);

			return messageListener;
		}

		@Override
		public void modifiedService(
			ServiceReference<IBMCloudS3Store> serviceReference,
			MessageListener messageListener) {
		}

		@Override
		public void removedService(
			ServiceReference<IBMCloudS3Store> serviceReference,
			MessageListener messageListener) {

			_schedulerEngineHelper.unregister(messageListener);
		}

		private IBMCloudS3StoreServiceTrackerCustomizer(
			BundleContext bundleContext,
			SchedulerEngineHelper schedulerEngineHelper,
			TriggerFactory triggerFactory) {

			_bundleContext = bundleContext;
			_schedulerEngineHelper = schedulerEngineHelper;
			_triggerFactory = triggerFactory;
		}

		private final BundleContext _bundleContext;
		private final SchedulerEngineHelper _schedulerEngineHelper;
		private final TriggerFactory _triggerFactory;

	}

}
