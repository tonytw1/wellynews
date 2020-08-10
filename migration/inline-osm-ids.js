// Foreach geocode with an optional OSM id migrate the osm id fields into a nested object
// Used in the resource and tag collections

db.tag.find({}).forEach(
    function(doc) {
        if (doc.geocode) {
            if (doc.geocode.osm_id && doc.geocode.osm_type) {
                print(doc.name);
                print(doc.geocode.osm_id);
                print(doc.geocode.osm_type);
                var osm = {
                    id: doc.geocode.osm_id,
                    type: doc.geocode.osm_type
                };
                doc.geocode.osmId = osm;
                db.tag.save(doc);
            }
         }
    }
);

db.resource.find({}).forEach(
    function(doc) {
        if (doc.geocode) {
            if (doc.geocode.osm_id && doc.geocode.osm_type) {
                print(doc.name);
                print(doc.geocode.osm_id);
                print(doc.geocode.osm_type);
                var osm = {
                    id: doc.geocode.osm_id,
                    type: doc.geocode.osm_type
                };
                doc.geocode.osmId = osm;
                db.resource.save(doc);
            }
         }
    }
);
