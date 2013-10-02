curl -XPUT 'http://localhost:9200/searchwellington/resources/_mapping' -d '
{ "content" : { "properties" : { 
	"id" : {"type" : "string", "index" : "not_analyzed" },
   	"type" : {"type" : "string", "index" : "not_analyzed" },
   	"name" : {"type" : "string", "index" : "not_analyzed" },
   	"tags.id" : {"type" : "string", "index" : "not_analyzed" },
   	"publisherName" : {"type" : "string", "index" : "not_analyzed" },
   	"place.osmId.id" : {"type" : "string", "index" : "not_analyzed" },
   	"place.osmId.type" : {"type" : "string", "index" : "not_analyzed" },
   	"location" : {"type" : "geo_point"},
   	"urlWords" : {"type" : "string", "index" : "not_analyzed" }   	
	} } }
'