package nz.co.searchwellington.model

import reactivemongo.bson.BSONObjectID

import scala.collection.mutable

case class DiscoveredFeed(_id: BSONObjectID = BSONObjectID.generate, url: String, references: mutable.Set[Resource] = mutable.Set.empty)