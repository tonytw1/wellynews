package nz.co.searchwellington.model

import java.util.UUID

import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter
import reactivemongo.bson.BSONObjectID

case class User(_id: BSONObjectID = BSONObjectID.generate,
                id: String = UUID.randomUUID.toString,
                name: Option[String] = None,
                profilename: Option[String] = None,
                bio: Option[String] = None,
                openid: Option[String] = None,
                twitterid: Option[Int] = None,
                url: Option[String] = None,
                apikey: Option[String] = None,
                admin: Boolean = false) extends TaggingVoter {

  def getId: String = id

  def getOpenId: String = openid.orNull

  def getTwitterId: Int = twitterid.getOrElse(0)

  def isUnlinkedAccount: Boolean = openid == null && twitterid == null

  def getProfilename: String = profilename.orNull

  def getVoterName: String = this.getProfilename

  def isAdmin: Boolean = admin

  def getUrl: String = url.orNull

  def getName: String = name.orNull

  def getBio: String = bio.orNull

  def getApikey: String = apikey.getOrElse("")

  def getDisplayName: String = profilename.getOrElse(id)

}