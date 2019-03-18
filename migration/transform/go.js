function booleanFromInt(booleanInt) {
    return booleanInt == 1;
}

db.resource.find({}).forEach(
    function(doc) {
        var resourceId = doc.id;
        doc.held = booleanFromInt(doc.held);
        db.resource.save(doc);
    }
);
