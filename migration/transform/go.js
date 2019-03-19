function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

function booleanFromInt(booleanInt) {
    return booleanInt == 1;
}

db.resource.find({}).forEach(
    function(doc) {
        var geocode_id = null;
        var geocode_id = doc.geocode_id;
        if (geocode_id != null) {
            print(geocode_id);
            var geocode = db.geocode.find(geocode_id).next();
            doc.geocode = geocode;
        }

        var resource_tags = null;
        var resource_tags = doc.resource_tags;
        if (resource_tags == null) {
            doc.resource_tags = [];
        }

        doc.id = uuidv4();
        doc.held = booleanFromInt(doc.held);
        db.resource.save(doc);
    }
);

db.tag.find({}).forEach(
    function(doc) {
        var geocode_id = null
        var geocode_id = doc.geocode_id;
        if (geocode_id != null) {
            var geocode = db.geocode.find(geocode_id);
            doc.geocode = geocode.next();
        }

        doc.id = uuidv4();
        db.tag.save(doc);
    }
);

db.user.find({}).forEach(
    function(doc) {
	uuid = uuidv4();
	doc.id = uuid;
	db.user.save(doc);
    }
);
