CREATE TABLE resource_tags ( id int primary key auto_increment,
                        resource_id int,
                       	tag_id int );
                       	
                       	
create index tag_id_index on resource_tags (tag_id) ;