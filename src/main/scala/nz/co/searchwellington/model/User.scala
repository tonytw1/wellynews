package nz.co.searchwellington.model

import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter
import reactivemongo.api.bson.BSONObjectID

import java.util.{Date, UUID}

case class User(_id: BSONObjectID = BSONObjectID.generate,
                id: String = UUID.randomUUID.toString,
                name: Option[String] = None,
                profilename: Option[String] = None,
                bio: Option[String] = None,
                openid: Option[String] = None,
                twitterid: Option[Int] = None,
                url: Option[String] = None,
                apikey: Option[String] = None,
                admin: Boolean = false,
                created: Option[Date] = None
               ) extends TaggingVoter {

  def getId: String = id

  def getOpenId: String = openid.orNull

  def getTwitterId: Int = twitterid.getOrElse(0)

  def isUnlinkedAccount: Boolean = openid.isEmpty && twitterid.isEmpty

  def getProfilename: String = profilename.orNull

  def getVoterName: String = this.getProfilename

  def isAdmin: Boolean = admin

  def getUrl: String = url.orNull

  def getName: String = name.orNull

  def getBio: String = bio.orNull

  def getApikey: String = apikey.getOrElse("")

  def getDisplayName: String = profilename.getOrElse(id)

  def getCreated: Date = created.orNull

}