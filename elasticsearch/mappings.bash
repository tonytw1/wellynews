curl -XPUT 'http://localhost:9200/searchwellington/resources/_mapping' -d '{
         "resources" : {
            "properties" : {
               "tags.id" : {
                  "index" : "not_analyzed",
                  "type" : "string"
               },
               "held" : {
                  "type" : "boolean"
               },
               "date" : {
                  "format" : "dateOptionalTime",
                  "type" : "date"
               },
               "acceptedByProfilename" : {
                  "type" : "string"
               },
               "place" : {
                  "properties" : {
                     "latLong" : {
                        "properties" : {
                           "longitude" : {
                              "type" : "double"
                           },
                           "latitude" : {
                              "type" : "double"
                           }
                        }
                     },
                     "address" : {
                        "type" : "string"
                     },
                     "osmId" : {
                        "properties" : {
                           "type" : {
                              "type" : "string"
                           },
                           "id" : {
                              "type" : "long"
                           }
                        }
                     }
                  }
               },
               "author" : {
                  "type" : "string"
               },
               "latestItemDate" : {
                  "type" : "long"
               },
               "handTags" : {
                  "properties" : {
                     "name" : {
                        "type" : "string"
                     },
                     "id" : {
                        "type" : "string"
                     }
                  }
               },
               "urlWords" : {
                  "index" : "not_analyzed",
                  "type" : "string"
               },
               "url" : {
                  "type" : "string"
               },
               "id" : {
                  "index" : "not_analyzed",
                  "type" : "string"
               },
               "publisherName" : {
                  "index" : "not_analyzed",
                  "type" : "string"
               },
               "owner" : {
                  "type" : "string"
               },
               "latLong" : {
                  "properties" : {
                     "longitude" : {
                        "type" : "double"
                     },
                     "latitude" : {
                        "type" : "double"
                     }
                  }
               },
               "location" : {
                  "type" : "geo_point"
               },
               "name" : {
                  "index" : "not_analyzed",
                  "type" : "string"
               },
               "frontendImage" : {
                  "properties" : {
                     "url" : {
                        "type" : "string"
                     }
                  }
               },
               "description" : {
                  "type" : "string"
               },
               "tags" : {
                  "properties" : {
                     "name" : {
                        "type" : "string"
                     },
                     "id" : {
                        "type" : "string"
                     }
                  }
               },
               "imageUrl" : {
                  "type" : "string"
               },
               "acceptedFromFeedName" : {
                  "type" : "string"
               },
               "accepted" : {
                  "type" : "long"
               },
               "type" : {
                  "index" : "not_analyzed",
                  "type" : "string"
               },
               "httpStatus" : {
                  "type" : "long"
               },
               "place.osmId.id" : {
                  "index" : "not_analyzed",
                  "type" : "string"
               },
               "webUrl" : {
                  "type" : "string"
               },
               "place.osmId.type" : {
                  "index" : "not_analyzed",
                  "type" : "string"
               },
               "headline" : {
                  "type" : "string"
               }
            }
         }
}
'
