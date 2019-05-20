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
                twitterid: Option[Long] = None,
                url: Option[String] = None,
                apikey: Option[String] = None,
                admin: Boolean = false) extends TaggingVoter {

  def getId: String = id

  def getOpenId: String = openid.getOrElse(null)

  def getTwitterId: Long = twitterid.getOrElse(0)

  def isUnlinkedAccount: Boolean = openid == null && twitterid == null

  def getProfilename: String = profilename.getOrElse(null)

  def getVoterName: String = this.getProfilename

  def isAdmin: Boolean = admin

  def getUrl: String = url.getOrElse(null)

  def getName: String = name.getOrElse(null)

  def getBio: String = bio.getOrElse(null)

  def getApikey: String = apikey.getOrElse("")

}