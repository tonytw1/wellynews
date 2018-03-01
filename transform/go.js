function dateFromString(dateString) {
    var d = dateString.substr(0,10) + 'T' + dateString.substr(11,5) + 'Z';
    var date = new Date(d);
    return date;
}

function booleanFromInt(booleanInt) {
    var b = booleanInt == 1;
    return b;
}

db.tag.find({}).forEach(
    function(doc) {
	    doc.hidden2 = booleanFromInt(doc.hidden);
	    doc.featured2 = booleanFromInt(doc.featured);
        db.tag.save(doc);
    }
);

db.resource.find({}).forEach(
    function(doc) {

        if(doc.date) doc.date2 = dateFromString(doc.date);
        if(doc.last_scanned) doc.last_scanned2 = dateFromString(doc.last_scanned);
        if(doc.last_changed) doc.last_changed2 = dateFromString(doc.last_changed);
        if(doc.live_time) doc.live_time2 = dateFromString(doc.live_time);
        if(doc.embargoed_until) doc.embargoed_until2 = dateFromString(doc.embargoed_until);
        if(doc.accepted) doc.accepted2 = dateFromString(doc.accepted);
        if(doc.latestItemDate) doc.latestItemDate2 = dateFromString(doc.latestItemDate);
        if(doc.lastRead) doc.lastRead2 = dateFromString(doc.lastRead);
        if(doc.publisher) doc.publisher = NumberLong(doc.publisher);
        if(doc.feed_publisher) doc.publisher = NumberLong(doc.feed_publisher);
        doc.held2 = booleanFromInt(doc.held);

        db.resource.save(doc);
    }
);

db.user.find({}).forEach(
    function(doc) {
	    doc.admin2 = booleanFromInt(doc.admin);
        db.user.save(doc);
    }
);

db.resource.find({}).forEach(
    function(doc) {
        var resourceId = doc.id;
        if (doc.geocode_id) {
                var geocode = db.geocode.findOne({id: doc.geocode_id});
                print(resourceId);
                print(geocode);
                doc.geocode = geocode;
                db.resource.save(doc);

        }
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
