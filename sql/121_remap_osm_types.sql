update geocode set osm_type = 'NODE' where osm_type = 'node';
update geocode set osm_type = 'WAY' where osm_type = 'way';
update geocode set osm_type = 'RELATION' where osm_type = 'relation';
