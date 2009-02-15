create table tag (id int primary key auto_increment, name varchar(255) NOT NULL) CHARSET=UTF8;


alter table tag change column name name varchar(255) unique;


alter table tag add column parent int;
