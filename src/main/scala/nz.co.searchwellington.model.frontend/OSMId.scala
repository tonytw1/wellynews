package nz.co.searchwellington.model.frontend

case class OSMId(id: Long, `type`: String) {

  def getId: Long = id
  def getType: String = `type`

}