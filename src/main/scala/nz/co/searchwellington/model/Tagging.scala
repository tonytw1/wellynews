package nz.co.searchwellington.model

import reactivemongo.bson.BSONObjectID

case class Tagging(tag_id: BSONObjectID, user_id: BSONObjectID)
