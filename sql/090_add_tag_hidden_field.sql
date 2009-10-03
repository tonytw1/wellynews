alter table tag add column hidden tinyint(1) default 0;

update tag set hidden = 1 where name in ('wcnhosted', 'featured');

