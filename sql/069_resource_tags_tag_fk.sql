alter table resource_tags add foreign key (resource_id) REFERENCES resource(id);