alter table resource add column submitting_twit BIGINT;


update resource set submitting_twit = (select id from twits where twitterid = 3254615642) where id = 20178;
update resource set submitting_twit = (select id from twits where twitterid = 1111484152) where id = 16998;
update resource set submitting_twit = (select id from twits where twitterid = 813826594) where id = 14635;
update resource set submitting_twit = (select id from twits where twitterid = 803781845) where id = 14512;
update resource set submitting_twit = (select id from twits where twitterid = 803972774) where id = 14514;
update resource set submitting_twit = (select id from twits where twitterid = 834601916) where id = 14972;
update resource set submitting_twit = (select id from twits where twitterid = 1111484152) where id = 16998;




delete from newsitem_retwits;

update resource set submitting_twit =  where id = ;
update resource set submitting_twit =  where id = ;
update resource set submitting_twit =  where id = ;

