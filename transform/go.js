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
	    print(doc);
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

	    doc.held2 = booleanFromInt(doc.held);

        db.resource.save(doc);
    }
);
