create table PermissionTemplate (
	mvccVersion LONG default 0 not null,
	ctCollectionId LONG default 0 not null,
	permissionTemplateId LONG not null,
	groupId LONG,
	companyId LONG,
	classNameId LONG,
	classPK LONG,
	permissionTemplateEnabled BOOLEAN,
	primary key (permissionTemplateId, ctCollectionId)
);