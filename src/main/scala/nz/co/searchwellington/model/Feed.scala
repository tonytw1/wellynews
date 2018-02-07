package nz.co.searchwellington.model

import java.util.Date

trait Feed extends PublishedResource { // TODO persisted classses shouldn't need to implement frontend interfaces
  def getAcceptancePolicy: String
  def setAcceptancePolicy(acceptancePolicy: String): Unit

  def setLatestItemDate(latestPublicationDate: Date): Unit
  def getLatestItemDate: Date

  def getLastRead: Date
  def setLastRead(lastRead: Date): Unit

  def getUrlWords: String

  def getWhakaokoId: String

  def setWhakaokoId(whakaokoId: String): Unit
}
