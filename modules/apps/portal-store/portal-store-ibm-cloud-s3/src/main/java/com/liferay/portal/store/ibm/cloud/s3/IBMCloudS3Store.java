/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.store.ibm.cloud.s3;

import com.ibm.cloud.objectstorage.AmazonClientException;
import com.ibm.cloud.objectstorage.AmazonServiceException;
import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.Protocol;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSCredentialsProvider;
import com.ibm.cloud.objectstorage.auth.BasicAWSCredentials;
import com.ibm.cloud.objectstorage.auth.DefaultAWSCredentialsProviderChain;
import com.ibm.cloud.objectstorage.internal.StaticCredentialsProvider;
import com.ibm.cloud.objectstorage.regions.Region;
import com.ibm.cloud.objectstorage.regions.Regions;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3Client;
import com.ibm.cloud.objectstorage.services.s3.S3ClientOptions;
import com.ibm.cloud.objectstorage.services.s3.model.DeleteObjectRequest;
import com.ibm.cloud.objectstorage.services.s3.model.DeleteObjectsRequest;
import com.ibm.cloud.objectstorage.services.s3.model.GetObjectMetadataRequest;
import com.ibm.cloud.objectstorage.services.s3.model.GetObjectRequest;
import com.ibm.cloud.objectstorage.services.s3.model.ListObjectsRequest;
import com.ibm.cloud.objectstorage.services.s3.model.ObjectListing;
import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata;
import com.ibm.cloud.objectstorage.services.s3.model.PutObjectRequest;
import com.ibm.cloud.objectstorage.services.s3.model.S3Object;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectSummary;
import com.ibm.cloud.objectstorage.services.s3.model.StorageClass;
import com.ibm.cloud.objectstorage.services.s3.transfer.TransferManager;
import com.ibm.cloud.objectstorage.services.s3.transfer.TransferManagerConfiguration;
import com.ibm.cloud.objectstorage.services.s3.transfer.Upload;

import com.liferay.document.library.kernel.exception.AccessDeniedException;
import com.liferay.document.library.kernel.exception.DuplicateFileException;
import com.liferay.document.library.kernel.exception.NoSuchFileException;
import com.liferay.document.library.kernel.store.Store;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.concurrent.ThreadPoolExecutor;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.store.ibm.cloud.s3.configuration.IBMCloudS3StoreConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Wing Shun Chan
 * @author Sten Martinez
 * @author Edward C. Han
 * @author Vilmos Papp
 * @author Máté Thurzó
 * @author Manuel de la Peña
 * @author Daniel Sanz
 */
@Component(
	configurationPid = "com.liferay.portal.store.ibm.cloud.s3.configuration.IBMCloudS3StoreConfiguration",
	configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true,
	property = {
		"service.ranking:Integer=0",
		"store.type=com.liferay.portal.store.s3.S3Store"
	},
	service = Store.class
)
public class IBMCloudS3Store implements Store {

	@Override
	public void addFile(
			long companyId, long repositoryId, String fileName,
			String versionLabel, InputStream inputStream)
		throws PortalException {

		if (hasFile(companyId, repositoryId, fileName, versionLabel)) {
			throw new DuplicateFileException(
				companyId, repositoryId, fileName, versionLabel);
		}

		File file = null;

		try {
			file = FileUtil.createTempFile(inputStream);

			_putObject(companyId, repositoryId, fileName, versionLabel, file);
		}
		catch (IOException ioException) {
			throw new SystemException(ioException);
		}
		finally {
			FileUtil.delete(file);
		}
	}

	@Override
	public void deleteDirectory(
		long companyId, long repositoryId, String dirName) {

		_deleteObjects(
			_ibmCloudS3KeyTransformer.getDirectoryKey(
				companyId, repositoryId, dirName));
	}

	@Override
	public void deleteFile(
		long companyId, long repositoryId, String fileName,
		String versionLabel) {

		try {
			String key = _ibmCloudS3KeyTransformer.getFileVersionKey(
				companyId, repositoryId, fileName, versionLabel);

			DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(
				_bucketName, key);

			_amazonS3.deleteObject(deleteObjectRequest);
		}
		catch (AmazonClientException amazonClientException) {
			throw _transform(amazonClientException);
		}
	}

	public String getBucketName() {
		return _bucketName;
	}

	@Override
	public InputStream getFileAsStream(
			long companyId, long repositoryId, String fileName,
			String versionLabel)
		throws PortalException {

		try {
			return new FileInputStream(
				_getFile(companyId, repositoryId, fileName, versionLabel));
		}
		catch (FileNotFoundException fileNotFoundException) {
			throw new SystemException(fileNotFoundException);
		}
	}

	@Override
	public String[] getFileNames(
		long companyId, long repositoryId, String dirName) {

		List<S3ObjectSummary> s3ObjectSummaries = _getS3ObjectSummaries(
			_getPrefixKey(companyId, repositoryId, dirName));

		Iterator<S3ObjectSummary> iterator = s3ObjectSummaries.iterator();

		String[] fileNames = new String[s3ObjectSummaries.size()];

		for (int i = 0; i < fileNames.length; i++) {
			S3ObjectSummary s3ObjectSummary = iterator.next();

			fileNames[i] = _ibmCloudS3KeyTransformer.getFileName(
				s3ObjectSummary.getKey());
		}

		return fileNames;
	}

	private String _getPrefixKey(
		long companyId, long repositoryId, String dirName) {

		if (Validator.isNull(dirName)) {
			return _ibmCloudS3KeyTransformer.getRepositoryKey(
				companyId, repositoryId);
		}

		return _ibmCloudS3KeyTransformer.getDirectoryKey(
			companyId, repositoryId, dirName);
	}

	@Override
	public long getFileSize(
			long companyId, long repositoryId, String fileName,
			String versionLabel)
		throws PortalException {

		String key = _ibmCloudS3KeyTransformer.getFileVersionKey(
			companyId, repositoryId, fileName, versionLabel);

		GetObjectMetadataRequest getObjectMetadataRequest =
			new GetObjectMetadataRequest(_bucketName, key);

		ObjectMetadata objectMetadata = _amazonS3.getObjectMetadata(
			getObjectMetadataRequest);

		if (objectMetadata == null) {
			throw new NoSuchFileException(companyId, repositoryId, fileName);
		}

		return objectMetadata.getContentLength();
	}

	@Override
	public String[] getFileVersions(
		long companyId, long repositoryId, String fileName) {

		throw new UnsupportedOperationException();
	}

	public TransferManager getTransferManager() {
		return _transferManager;
	}

	@Override
	public boolean hasFile(
		long companyId, long repositoryId, String fileName,
		String versionLabel) {

		try {
			if (Validator.isNull(versionLabel)) {
				versionLabel = _getHeadVersionLabel(
					companyId, repositoryId, fileName);
			}

			String key = _ibmCloudS3KeyTransformer.getFileVersionKey(
				companyId, repositoryId, fileName, versionLabel);

			return _amazonS3.doesObjectExist(_bucketName, key);
		}
		catch (AmazonClientException amazonClientException) {
			if (_isFileNotFound(amazonClientException)) {
				return false;
			}

			throw _transform(amazonClientException);
		}
		catch (NoSuchFileException noSuchFileException) {

			// LPS-52675

			if (_log.isDebugEnabled()) {
				_log.debug(noSuchFileException, noSuchFileException);
			}

			return false;
		}
	}

	@Activate
	protected void activate(Map<String, Object> properties) {
		_ibmCloudS3StoreConfiguration = ConfigurableUtil.createConfigurable(
			IBMCloudS3StoreConfiguration.class, properties);

		_awsCredentialsProvider = _getAWSCredentialsProvider();

		_amazonS3 = _getAmazonS3(_awsCredentialsProvider);

		_bucketName = _ibmCloudS3StoreConfiguration.bucketName();
		_transferManager = _getTransferManager(_amazonS3);

		try {
			_storageClass = StorageClass.fromValue(
				_ibmCloudS3StoreConfiguration.ibmCloudS3StorageClass());
		}
		catch (IllegalArgumentException illegalArgumentException) {
			_storageClass = StorageClass.Standard;

			if (_log.isWarnEnabled()) {
				_log.warn(
					_ibmCloudS3StoreConfiguration.ibmCloudS3StorageClass() +
						" is not a valid value for the storage class",
					illegalArgumentException);
			}
		}
	}

	private void _configureConnectionProtocol(
		ClientConfiguration clientConfiguration) {

		String connectionProtocol =
			_ibmCloudS3StoreConfiguration.connectionProtocol();

		if (Validator.isNull(connectionProtocol) ||
			connectionProtocol.equals("DEFAULT")) {

			return;
		}

		if (connectionProtocol.equals("HTTP")) {
			clientConfiguration.setProtocol(Protocol.HTTP);
		}
		else {
			clientConfiguration.setProtocol(Protocol.HTTPS);
		}
	}

	private void _configureProxySettings(
		ClientConfiguration clientConfiguration) {

		String proxyHost = _ibmCloudS3StoreConfiguration.proxyHost();

		if (Validator.isNull(proxyHost)) {
			return;
		}

		clientConfiguration.setProxyHost(proxyHost);
		clientConfiguration.setProxyPort(
			_ibmCloudS3StoreConfiguration.proxyPort());

		String proxyAuthType = _ibmCloudS3StoreConfiguration.proxyAuthType();

		if (proxyAuthType.equals("ntlm") ||
			proxyAuthType.equals("username-password")) {

			clientConfiguration.setProxyPassword(
				_ibmCloudS3StoreConfiguration.proxyPassword());
			clientConfiguration.setProxyUsername(
				_ibmCloudS3StoreConfiguration.proxyUsername());

			if (proxyAuthType.equals("ntlm")) {
				clientConfiguration.setProxyDomain(
					_ibmCloudS3StoreConfiguration.ntlmProxyDomain());
				clientConfiguration.setProxyWorkstation(
					_ibmCloudS3StoreConfiguration.ntlmProxyWorkstation());
			}
		}
	}

	private void _configureS3Endpoint(AmazonS3 amazonS3) {
		String s3Endpoint = _ibmCloudS3StoreConfiguration.ibmCloudS3Endpoint();

		if (Validator.isNull(s3Endpoint)) {
			return;
		}

		amazonS3.setEndpoint(s3Endpoint);
	}

	private void _configureS3PathStyle(AmazonS3 amazonS3) {
		boolean s3PathStyle =
			_ibmCloudS3StoreConfiguration.ibmCloudS3PathStyle();

		if (!s3PathStyle) {
			return;
		}

		S3ClientOptions s3ClientOptions = new S3ClientOptions();

		s3ClientOptions.setPathStyleAccess(true);

		amazonS3.setS3ClientOptions(s3ClientOptions);
	}

	private void _configureSignerOverride(
		ClientConfiguration clientConfiguration) {

		String signerOverride = _ibmCloudS3StoreConfiguration.signerOverride();

		if (Validator.isNull(signerOverride)) {
			return;
		}

		clientConfiguration.setSignerOverride(signerOverride);
	}

	private void _deleteObjects(String prefix) {
		try {
			String[] keys = new String[_DELETE_MAX];

			List<S3ObjectSummary> s3ObjectSummaries = _getS3ObjectSummaries(
				prefix);

			Iterator<S3ObjectSummary> iterator = s3ObjectSummaries.iterator();

			while (iterator.hasNext()) {
				DeleteObjectsRequest deleteObjectsRequest =
					new DeleteObjectsRequest(_bucketName);

				for (int i = 0; i < keys.length; i++) {
					if (iterator.hasNext()) {
						S3ObjectSummary s3ObjectSummary = iterator.next();

						keys[i] = s3ObjectSummary.getKey();
					}
					else {
						keys = Arrays.copyOfRange(keys, 0, i);

						break;
					}
				}

				deleteObjectsRequest.withKeys(keys);

				_amazonS3.deleteObjects(deleteObjectsRequest);
			}
		}
		catch (AmazonClientException amazonClientException) {
			throw _transform(amazonClientException);
		}
	}

	private AmazonS3 _getAmazonS3(
		AWSCredentialsProvider awsCredentialsProvider) {

		AmazonS3 amazonS3 = new AmazonS3Client(
			awsCredentialsProvider, _getClientConfiguration());

		Region region = Region.getRegion(
			Regions.fromName(_ibmCloudS3StoreConfiguration.ibmCloudS3Region()));

		amazonS3.setRegion(region);

		_configureS3Endpoint(amazonS3);
		_configureS3PathStyle(amazonS3);

		return amazonS3;
	}

	private AWSCredentialsProvider _getAWSCredentialsProvider() {
		if (Validator.isNotNull(_ibmCloudS3StoreConfiguration.accessKey()) &&
			Validator.isNotNull(_ibmCloudS3StoreConfiguration.secretKey())) {

			AWSCredentials awsCredentials = new BasicAWSCredentials(
				_ibmCloudS3StoreConfiguration.accessKey(),
				_ibmCloudS3StoreConfiguration.secretKey());

			return new StaticCredentialsProvider(awsCredentials);
		}

		return new DefaultAWSCredentialsProviderChain();
	}

	private ClientConfiguration _getClientConfiguration() {
		ClientConfiguration clientConfiguration = new ClientConfiguration();

		clientConfiguration.setConnectionTimeout(
			_ibmCloudS3StoreConfiguration.connectionTimeout());

		clientConfiguration.setMaxErrorRetry(
			_ibmCloudS3StoreConfiguration.httpClientMaxErrorRetry());
		clientConfiguration.setMaxConnections(
			_ibmCloudS3StoreConfiguration.httpClientMaxConnections());

		_configureConnectionProtocol(clientConfiguration);
		_configureProxySettings(clientConfiguration);
		_configureSignerOverride(clientConfiguration);

		return clientConfiguration;
	}

	private String _getHeadVersionLabel(
			long companyId, long repositoryId, String fileName)
		throws NoSuchFileException {

		String key = _ibmCloudS3KeyTransformer.getFileKey(
			companyId, repositoryId, fileName);

		List<S3ObjectSummary> s3ObjectSummaries = _getS3ObjectSummaries(key);

		Iterator<S3ObjectSummary> iterator = s3ObjectSummaries.iterator();

		String[] keys = new String[s3ObjectSummaries.size()];

		for (int i = 0; i < keys.length; i++) {
			S3ObjectSummary s3ObjectSummary = iterator.next();

			keys[i] = s3ObjectSummary.getKey();
		}

		if (keys.length > 0) {
			Arrays.sort(keys);

			String headVersionKey = keys[keys.length - 1];

			int x = headVersionKey.lastIndexOf(CharPool.SLASH);

			return headVersionKey.substring(x + 1);
		}

		throw new NoSuchFileException(companyId, repositoryId, fileName);
	}

	private S3Object _getS3Object(
			long companyId, long repositoryId, String fileName,
			String versionLabel)
		throws NoSuchFileException {

		try {
			if (Validator.isNull(versionLabel)) {
				versionLabel = _getHeadVersionLabel(
					companyId, repositoryId, fileName);
			}

			String key = _ibmCloudS3KeyTransformer.getFileVersionKey(
				companyId, repositoryId, fileName, versionLabel);

			GetObjectRequest getObjectRequest = new GetObjectRequest(
				_bucketName, key);

			S3Object s3Object = _amazonS3.getObject(getObjectRequest);

			if (s3Object == null) {
				throw new NoSuchFileException(
					companyId, repositoryId, fileName, versionLabel);
			}

			return s3Object;
		}
		catch (AmazonClientException amazonClientException) {
			if (_isFileNotFound(amazonClientException)) {
				throw new NoSuchFileException(
					companyId, repositoryId, fileName, versionLabel);
			}

			throw _transform(amazonClientException);
		}
	}

	private List<S3ObjectSummary> _getS3ObjectSummaries(String prefix) {
		try {
			ListObjectsRequest listObjectsRequest = new ListObjectsRequest();

			listObjectsRequest.withBucketName(_bucketName);
			listObjectsRequest.withPrefix(prefix);

			ObjectListing objectListing = _amazonS3.listObjects(
				listObjectsRequest);

			List<S3ObjectSummary> s3ObjectSummaries = new ArrayList<>(
				objectListing.getMaxKeys());

			while (true) {
				s3ObjectSummaries.addAll(objectListing.getObjectSummaries());

				if (objectListing.isTruncated()) {
					objectListing = _amazonS3.listNextBatchOfObjects(
						objectListing);
				}
				else {
					break;
				}
			}

			return s3ObjectSummaries;
		}
		catch (AmazonClientException amazonClientException) {
			throw _transform(amazonClientException);
		}
	}

	private TransferManager _getTransferManager(AmazonS3 amazonS3) {
		ExecutorService executorService = new ThreadPoolExecutor(
			_ibmCloudS3StoreConfiguration.corePoolSize(),
			_ibmCloudS3StoreConfiguration.maxPoolSize());

		TransferManager transferManager = new TransferManager(
			amazonS3, executorService, false);

		TransferManagerConfiguration transferManagerConfiguration =
			new TransferManagerConfiguration();

		transferManagerConfiguration.setMinimumUploadPartSize(
			_ibmCloudS3StoreConfiguration.minimumUploadPartSize());
		transferManagerConfiguration.setMultipartUploadThreshold(
			_ibmCloudS3StoreConfiguration.multipartUploadThreshold());

		transferManager.setConfiguration(transferManagerConfiguration);

		return transferManager;
	}

	private boolean _isFileNotFound(
		AmazonClientException amazonClientException) {

		if (amazonClientException instanceof AmazonServiceException) {
			AmazonServiceException amazonServiceException =
				(AmazonServiceException)amazonClientException;

			String errorCode = amazonServiceException.getErrorCode();

			if (errorCode.equals(_ERROR_CODE_FILE_NOT_FOUND) &&
				(amazonServiceException.getStatusCode() ==
					_STATUS_CODE_FILE_NOT_FOUND)) {

				return true;
			}
		}

		return false;
	}

	@Modified
	protected void modified(Map<String, Object> properties) {
		activate(properties);
	}

	private void _putObject(
		long companyId, long repositoryId, String fileName, String versionLabel,
		File file) {

		Upload upload = null;

		try {
			String key = _ibmCloudS3KeyTransformer.getFileVersionKey(
				companyId, repositoryId, fileName, versionLabel);

			PutObjectRequest putObjectRequest = new PutObjectRequest(
				_bucketName, key, file);

			putObjectRequest.withStorageClass(_storageClass);

			upload = _transferManager.upload(putObjectRequest);

			upload.waitForCompletion();
		}
		catch (AmazonClientException amazonClientException) {
			throw _transform(amazonClientException);
		}
		catch (InterruptedException interruptedException) {
			upload.abort();

			Thread thread = Thread.currentThread();

			thread.interrupt();
		}
	}

	private SystemException _transform(
		AmazonClientException amazonClientException) {

		if (amazonClientException instanceof AmazonServiceException) {
			AmazonServiceException amazonServiceException =
				(AmazonServiceException)amazonClientException;

			StringBundler sb = new StringBundler(11);

			sb.append("{errorCode=");

			String errorCode = amazonServiceException.getErrorCode();

			sb.append(errorCode);

			sb.append(", errorType=");
			sb.append(amazonServiceException.getErrorType());
			sb.append(", message=");
			sb.append(amazonServiceException.getMessage());
			sb.append(", requestId=");
			sb.append(amazonServiceException.getRequestId());
			sb.append(", statusCode=");
			sb.append(amazonServiceException.getStatusCode());
			sb.append("}");

			if (errorCode.equals("AccessDenied")) {
				return new AccessDeniedException(sb.toString());
			}

			return new SystemException(sb.toString());
		}

		return new SystemException(
			amazonClientException.getMessage(), amazonClientException);
	}

	private File _getFile(
			long companyId, long repositoryId, String fileName,
			String versionLabel)
		throws PortalException {

		try {
			S3Object s3Object = _getS3Object(
				companyId, repositoryId, fileName, versionLabel);

			File file = _ibmCloudS3FileCache.getCacheFile(s3Object, fileName);

			_ibmCloudS3FileCache.cleanUpCacheFiles();

			return file;
		}
		catch (IOException ioException) {
			throw new SystemException(ioException);
		}
	}

	private static final int _DELETE_MAX = 1000;

	private static final String _ERROR_CODE_FILE_NOT_FOUND = "NoSuchKey";

	private static final int _STATUS_CODE_FILE_NOT_FOUND = 404;

	private static final Log _log = LogFactoryUtil.getLog(
		IBMCloudS3Store.class);

	private static volatile IBMCloudS3StoreConfiguration
		_ibmCloudS3StoreConfiguration;

	private AmazonS3 _amazonS3;
	private AWSCredentialsProvider _awsCredentialsProvider;
	private String _bucketName;

	@Reference
	private IBMCloudS3FileCache _ibmCloudS3FileCache;

	@Reference
	private IBMCloudS3KeyTransformer _ibmCloudS3KeyTransformer;

	private StorageClass _storageClass;
	private TransferManager _transferManager;

}