create table DLFileVersionPreview(
	dlFileVersionPreviewId LONG not null primary key,
	groupId                LONG,
	fileEntryId            LONG,
	fileVersionId          LONG,
	previewStatus          INTEGER
);

create unique index IX_4ACE7FBB on DLStorageQuota (companyId, ctCollectionId);

COMMIT_TRANSACTION;