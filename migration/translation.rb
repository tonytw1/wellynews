table "config" do
	column "id", :key, :as => :integer
	column "feedburner_widget", :string
	column "stats_tracking", :string
	column "flickr_pool_group_id", :string, :references => "flickr_pool_groups"
	column "use_clickthrough_tracking", :string
	column "twitter_listener_enabled", :integer
	column "feed_reading_enabled", :integer
end

table "discovered_feeds" do
	column "id", :key, :as => :integer
	column "url", :string
end

table "discovered_urls_references" do
	column "resource_id", :integer, :references => "resources"
	column "discovered_url_id", :integer, :references => "discovered_urls"
end

table "geocode" do
   before_save do |row|
            id = row.delete('pre_mongified_id')
            row.id = id
    end
	column "id", :key, :as => :integer
	column "address", :string
	column "latitude", :float
	column "longitude", :float
	column "type", :string
	column "osm_id", :text, :references => "osms"
	column "osm_type", :string
end

table "image" do
   before_save do |row|
            id = row.delete('pre_mongified_id')
            row.id = id
    end
	column "id", :key, :as => :integer
	column "url", :string
end

table "resource" do
    before_save do |row|
            id = row.delete('pre_mongified_id')
            row.id = id
    end
	column "id", :key, :as => :integer
	column "page", :string
	column "title", :string
	column "description", :text
	column "date", :datetime
	column "category", :integer
	column "type", :string
	column "publisher", :integer
	column "checksum", :string
	column "feed_publisher", :integer
	column "acceptance", :string
	column "watchlist_publisher", :integer
	column "http_status", :integer
	column "last_scanned", :datetime
	column "last_changed", :datetime
	column "latest_item", :datetime
	column "comment_feed", :integer
	column "last_read", :datetime
	column "live_time", :datetime
	column "twitter_submitter", :string
	column "twitter_message", :string
	column "calendar_publisher", :integer
	column "technorati_count", :integer
	column "geocode", :string
	column "geocode_id", :integer, :references => "geocodes"
	column "url_words", :string
	column "owner", :integer
	column "twitter_id", :integer, :references => "twitters"
	column "submitting_twit", :integer
	column "feed", :integer
	column "embargoed_until", :datetime
	column "held", :integer
	column "image_id", :integer, :references => "images"
	column "accepted", :datetime
	column "accepted_by", :integer
	column "whakaoko_id", :string
end

table "resource_tags" do
	column "id", :key, :as => :integer
	column "resource_id", :integer, :references => "resources"
	column "tag_id", :integer, :references => "tags"
	column "user_id", :integer, :references => "users"
end

table "suggestion" do
   before_save do |row|
            id = row.delete('pre_mongified_id')
            row.id = id
    end
	column "id", :key, :as => :integer
	column "url", :string
	column "feed", :integer
	column "first_seen", :datetime
end

table "supression" do
   before_save do |row|
            id = row.delete('pre_mongified_id')
            row.id = id
    end
	column "id", :key, :as => :integer
	column "url", :string
end

table "tag" do
    before_save do |row|
            id = row.delete('pre_mongified_id')
            row.id = id
    end
	column "id", :key, :as => :integer
	column "name", :string
	column "parent", :integer
	column "display_name", :string
	column "main_image", :string
	column "secondary_image", :string
	column "related_feed", :integer
	column "related_twitter", :string
	column "autotag_hints", :string
	column "hidden", :boolean
	column "geocode_id", :integer, :references => "geocodes"
	column "description", :string
	column "featured", :boolean
end

table "user" do
   before_save do |row|
            id = row.delete('pre_mongified_id')
            row.id = id
    end
	column "id", :key, :as => :integer
	column "openid", :string
	column "admin", :boolean
	column "url", :string
	column "profilename", :string
	column "name", :string
	column "bio", :text
	column "apikey", :string
	column "twitterid", :integer
end

