package nz.co.searchwellington.model

import reactivemongo.api.bson.BSONObjectID

case class Supression(_id: BSONObjectID = BSONObjectID.generate, url: String) {

  def getId: String = _id.stringify
  def getUrl: String = url

}