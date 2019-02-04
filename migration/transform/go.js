function booleanFromInt(booleanInt) {
    return booleanInt == 1;
}

db.resource.find({}).forEach(
    function(doc) {
        var resourceId = doc.id;
        doc.held2 = booleanFromInt(doc.held);
        if (doc.geocode_id) {
                var geocode = db.geocode.findOne({id: doc.geocode_id});
                doc.geocode = geocode;
        }
        db.resource.save(doc);
    }
);

db.tag.find({}).forEach(
    function(doc) {
        var tagId = doc.id;
        if (doc.geocode_id) {
                var geocode = db.geocode.findOne({id: doc.geocode_id});
                print(tagId);
                print(geocode);
                doc.geocode = geocode;
                db.tag.save(doc);

        }
    }
);
