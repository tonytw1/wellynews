curl -XPUT 'http://localhost:9200/searchwellington/resources/_mapping' -H 'Content-Type: application/json' -d '{
         "resources" : {
            "properties" : {
               "tags.id" : {
                  "analyzer": "standard",
                  "type" : "text"
               },
               "held" : {
                  "type" : "boolean"
               },
               "date" : {
                  "format" : "dateOptionalTime",
                  "type" : "date"
               },
               "acceptedByProfilename" : {
                  "type" : "text"
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
                        "type" : "text"
                     },
                     "osmId" : {
                        "properties" : {
                           "type" : {
                              "type" : "text"
                           },
                           "id" : {
                              "type" : "long"
                           }
                        }
                     }
                  }
               },
               "author" : {
                  "type" : "text"
               },
               "latestItemDate" : {
                  "type" : "long"
               },
               "handTags" : {
                  "properties" : {
                     "name" : {
                        "type" : "text"
                     },
                     "id" : {
                        "type" : "text"
                     }
                  }
               },
               "urlWords" : {
                  "analyzer": "standard",
                  "type" : "text"
               },
               "url" : {
                  "type" : "text"
               },
               "id" : {
                  "analyzer": "standard",
                  "type" : "text",
                  "store": true

               },
               "publisherName" : {
                  "analyzer": "standard",
                  "type" : "text"
               },
               "owner" : {
                  "type" : "text"
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
                  "analyzer": "standard",
                  "type" : "text"
               },
               "frontendImage" : {
                  "properties" : {
                     "url" : {
                        "type" : "text"
                     }
                  }
               },
               "description" : {
                  "type" : "text"
               },
               "tags" : {
                  "properties" : {
                     "name" : {
                        "type" : "text"
                     },
                     "id" : {
                        "type" : "text"
                     }
                  }
               },
               "imageUrl" : {
                  "type" : "text"
               },
               "acceptedFromFeedName" : {
                  "type" : "text"
               },
               "accepted" : {
                  "type" : "long"
               },
               "type" : {
                  "analyzer": "standard",
                  "type" : "text"
               },
               "httpStatus" : {
                  "type" : "long"
               },
               "place.osmId.id" : {
                  "type" : "long"
               },
               "webUrl" : {
                  "type" : "text"
               },
               "place.osmId.type" : {
                  "analyzer": "standard",
                  "type" : "text"
               },
               "headline" : {
                  "type" : "text"
               }
            }
         }
}
'
