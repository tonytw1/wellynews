alter table resource_tags add index resource_tag_index (resource_id);
alter table resource add index publisher_index (publisher);