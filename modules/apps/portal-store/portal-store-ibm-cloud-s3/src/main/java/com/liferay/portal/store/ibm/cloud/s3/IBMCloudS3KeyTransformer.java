package com.liferay.portal.store.ibm.cloud.s3;

public interface IBMCloudS3KeyTransformer {

	public String getDirectoryKey(
			long companyId, long repositoryId, String folderName);

	public String getFileKey(
		long companyId, long repositoryId, String fileName);

	public String getFileName(String key);

	public String getFileVersionKey(
		long companyId, long repositoryId, String fileName,
		String versionLabel);

	public String getNormalizedFileName(String fileName);

	public String getRepositoryKey(long companyId, long repositoryId);

	public String moveKey(String key, String oldPrefix, String newPrefix);
		
}
