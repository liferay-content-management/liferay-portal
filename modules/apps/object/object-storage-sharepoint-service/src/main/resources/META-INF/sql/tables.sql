create table OSSharepoint_TokenEntry (
	mvccVersion LONG default 0 not null,
	tokenEntryId LONG not null primary key,
	groupId LONG,
	companyId LONG,
	userId LONG,
	userName VARCHAR(75) null,
	createDate DATE null,
	accessToken VARCHAR(75) null,
	expirationDate DATE null,
	refreshToken VARCHAR(75) null
);