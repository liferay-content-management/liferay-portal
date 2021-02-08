package com.liferay.portal.store.ibm.cloud.s3;

import com.ibm.cloud.objectstorage.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;

public interface IBMCloudS3FileCache {

	public void cleanUpCacheFiles();

	public File getCacheFile(S3Object s3Object, String fileName)
		throws IOException;
	
}
