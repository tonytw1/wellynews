alter table user engine InnoDB;

alter table resource_tags change column user_id user_id int(11) NOT NULL;
alter table resource_tags change column resource_id resource_id int(11) NOT NULL;
alter table resource_tags change column tag_id tag_id int(11) NOT NULL;

alter table resource_tags add foreign key (user_id) REFERENCES user(id);
alter table resource_tags add foreign key (tag_id) REFERENCES tag(id);
alter table resource_tags add foreign key (resource_id) REFERENCES resource(id);
