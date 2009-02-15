create table discovered_urls_references ( 
	resource_id int not null primary key,
	discovered_url_id int not null unique ); 
	