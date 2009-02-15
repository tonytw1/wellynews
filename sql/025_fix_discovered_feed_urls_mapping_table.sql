DROP TABLE IF EXISTS `discovered_urls_references`;

CREATE TABLE `discovered_urls_references` (
  `resource_id` int(11) NOT NULL,
  `discovered_url_id` int(11) NOT NULL,
  UNIQUE KEY `mapping` (`discovered_url_id`, `resource_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
