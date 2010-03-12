 alter table resource_tags add column user_id int(11);
 
 update resource_tags set user_id = (select id from user where profilename = 'tonytw1');
 