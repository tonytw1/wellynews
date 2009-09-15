CREATE TABLE newsitem_retwits ( id int primary key auto_increment,
                        resource_id int,
                       	twit_id int );
                       	
                       	
create index newsitem_twits_resource_id_index on newsitem_retwits (resource_id) ;