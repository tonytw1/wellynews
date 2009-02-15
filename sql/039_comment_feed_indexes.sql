alter table comment add index comment_comment_feed_index(comment_feed);
alter table resource add index resource_comment_feed_index(comment_feed);

// Makes this query go from .88s to 0.05s
// select resource.id from resource, comment_feed, comment where comment_feed.id = resource.comment_feed and comment.comment_feed = comment_feed.id and type = 'N' and comment_order = 0;

// Makes this query go from 5.58 to 0.08
// select this_.id as id5_1_, this_.date as date5_1_, this_.title as title5_1_, this_.page as page5_1_, this_.snapshot as snapshot5_1_, this_.http_status as http7_5_1_, this_.description as descript8_5_1_, this_.last_scanned as last9_5_1_, this_.last_changed as last10_5_1_, this_.publisher as publisher5_1_, this_.comment_feed as comment12_5_1_, commentfee1_.id as id2_0_, commentfee1_.url as url2_0_, commentfee1_.last_read as last3_2_0_ from resource this_ inner join comment_feed commentfee1_ on this_.comment_feed=commentfee1_.id where this_.type='N' and this_.comment_feed is not null and exists (select 1 from comment where commentfee1_.id=comment_feed) order by this_.date desc limit ?