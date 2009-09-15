create table twits (
	id int primary key AUTO_INCREMENT,
	twitterid BIGINT unique,
	author varchar(255),
	profileImage varchar(255),
	text varchar(140),
	date timestamp
	);
	