alter table resource add column calendar_publisher int;

update resource set calendar_publisher = publisher where type ='C';

update resource set publisher = NULL where type='C';

