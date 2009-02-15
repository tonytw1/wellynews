CREATE TABLE `comment_feed_references` (
  `comment_feed_id` int(11) NOT NULL,
  `resource_id` int(11) NOT NULL,
  UNIQUE KEY `mapping` (`comment_feed_id`,`resource_id`)
) 