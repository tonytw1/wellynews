package nz.co.searchwellington.model

import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter

case class User(id: String, openid: Option[String] = None, twitterId: Option[Long] = None, profilename: Option[String] = None, url: Option[String] = None,
                name: Option[String] = None, bio: Option[String] = None, apikey: Option[String]= None, admin: Boolean = false) extends TaggingVoter {

  def getId: String = id

  def getOpenId: String = openid.getOrElse(null)

  def getTwitterId: Long = twitterId.getOrElse(0)

  def isUnlinkedAccount: Boolean = openid == null && twitterId == null

  def getProfilename: String = profilename.getOrElse(null)

  def getVoterName: String = this.getProfilename

  def isAdmin: Boolean = admin

  def getUrl: String = url.getOrElse(null)

  def getName: String = name.getOrElse(null)

  def getBio: String = bio.getOrElse(null)

  def getApikey: String = apikey.getOrElse("")

}