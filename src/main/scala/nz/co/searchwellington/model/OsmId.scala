package nz.co.searchwellington.model

case class OsmId(id: Long, `type`: String) {

  def getId: Long = id

  def getType: String = `type`

}