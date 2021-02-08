package com.liferay.portal.store.ibm.cloud.s3;

import com.ibm.cloud.objectstorage.services.s3.model.ObjectMetadata;
import com.ibm.cloud.objectstorage.services.s3.model.S3Object;
import com.liferay.portal.store.ibm.cloud.s3.configuration.IBMCloudS3StoreConfiguration;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.SystemProperties;
import com.liferay.portal.kernel.util.Time;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
		configurationPid = "com.liferay.portal.store.ibm.cloud.s3.configuration.IBMCloudS3StoreConfiguration",
		configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true,
		service = IBMCloudS3FileCache.class
	)
public class IBMCloudS3FileCacheImpl implements IBMCloudS3FileCache {

	@Override
	public void cleanUpCacheFiles() {
		_calledCleanUpCacheFilesCount++;

		if (_calledCleanUpCacheFilesCount <
				_cacheDirCleanUpFrequency.intValue()) {

			return;
		}

		synchronized (this) {
			if (_calledCleanUpCacheFilesCount == 0) {
				return;
			}

			_calledCleanUpCacheFilesCount = 0;

			Path cacheDirPath = Paths.get(getCacheDirName());

			long lastModified = System.currentTimeMillis();

			lastModified -= _cacheDirCleanUpExpunge.intValue() * Time.DAY;

			cleanUpCacheFiles(cacheDirPath, lastModified);
		}
	}

	@Override
	public File getCacheFile(S3Object s3Object, String fileName)
		throws IOException {

		StringBundler sb = new StringBundler(4);

		sb.append(getCacheDirName());
		sb.append(
			DateUtil.getCurrentDate(
				_CACHE_DIR_PATTERN, LocaleUtil.getDefault()));
		sb.append(_ibmCloudS3KeyTransformer.getNormalizedFileName(fileName));

		ObjectMetadata objectMetadata = s3Object.getObjectMetadata();

		Date lastModifiedDate = objectMetadata.getLastModified();

		sb.append(lastModifiedDate.getTime());

		String cacheFileName = sb.toString();

		File cacheFile = new File(cacheFileName);

		try (InputStream inputStream = s3Object.getObjectContent()) {
			if (cacheFile.exists() &&
				(cacheFile.lastModified() >= lastModifiedDate.getTime())) {

				return cacheFile;
			}

			if (inputStream == null) {
				throw new IOException("S3 object input stream is null");
			}

			FileUtil.mkdirs(cacheFile.getParentFile());

			try (OutputStream outputStream = new FileOutputStream(cacheFile)) {
				StreamUtil.transfer(inputStream, outputStream);
			}
		}

		return cacheFile;
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_ibmCloudS3StoreConfiguration = ConfigurableUtil.createConfigurable(
			IBMCloudS3StoreConfiguration.class, properties);

		_cacheDirCleanUpExpunge = new AtomicInteger(
			_ibmCloudS3StoreConfiguration.cacheDirCleanUpExpunge());
		_cacheDirCleanUpFrequency = new AtomicInteger(
			_ibmCloudS3StoreConfiguration.cacheDirCleanUpFrequency());
	}

	protected void cleanUpCacheFiles(Path cacheDirPath, long lastModified) {
		if (Files.notExists(cacheDirPath)) {
			return;
		}

		try {
			Files.walkFileTree(
				cacheDirPath,
				new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult postVisitDirectory(
							Path dirPath, IOException ioException)
						throws IOException {

						try (DirectoryStream<Path> directoryStream =
								Files.newDirectoryStream(dirPath)) {

							Iterator<Path> iterator =
								directoryStream.iterator();

							if (!iterator.hasNext()) {
								Files.delete(dirPath);
							}
						}

						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(
							Path filePath,
							BasicFileAttributes basicFileAttributes)
						throws IOException {

						FileTime fileTime = Files.getLastModifiedTime(filePath);

						if (fileTime.toMillis() < lastModified) {
							Files.delete(filePath);
						}

						return FileVisitResult.CONTINUE;
					}

				});
		}
		catch (IOException ioException) {
			_log.error(
				"Unable to clean up cache files for " + cacheDirPath,
				ioException);
		}
	}

	@Deactivate
	protected void deactivate() {
		File cacheDir = new File(getCacheDirName());

		boolean deleted = cacheDir.delete();

		if (!deleted) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to delete " + getCacheDirName());
			}
		}
	}

	protected String getCacheDirName() {
		return SystemProperties.get(SystemProperties.TMP_DIR) + _CACHE_DIR_NAME;
	}

	@Reference(unbind = "-")
	protected void setIBMCloudS3KeyTransformer(IBMCloudS3KeyTransformer ibmCloudS3KeyTransformer) {
		_ibmCloudS3KeyTransformer = ibmCloudS3KeyTransformer;
	}

	private static final String _CACHE_DIR_NAME = "/liferay/s3";

	private static final String _CACHE_DIR_PATTERN = "/yyyy/MM/dd/HH/";

	private static final Log _log = LogFactoryUtil.getLog(
		IBMCloudS3FileCacheImpl.class);

	private AtomicInteger _cacheDirCleanUpExpunge;
	private AtomicInteger _cacheDirCleanUpFrequency;
	private int _calledCleanUpCacheFilesCount;
	private IBMCloudS3KeyTransformer _ibmCloudS3KeyTransformer;
	private volatile IBMCloudS3StoreConfiguration _ibmCloudS3StoreConfiguration;
	
}
