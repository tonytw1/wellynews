package nz.co.searchwellington.model

import reactivemongo.api.bson.BSONObjectID

case class Tagging(tag_id: BSONObjectID, user_id: BSONObjectID, reason: Option[String] = None)
