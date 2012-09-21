alter table geocode add column osm_type varchar(20);
update geocode set osm_id = NULL;

