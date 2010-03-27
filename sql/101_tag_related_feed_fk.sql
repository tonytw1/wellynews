alter table tag add foreign key (related_feed) REFERENCES resource(id);
