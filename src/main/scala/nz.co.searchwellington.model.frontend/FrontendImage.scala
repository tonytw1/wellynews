package nz.co.searchwellington.model.frontend

class FrontendImage(var url: String) extends java.io.Serializable {

  def this() {
    this("")
  }

  def getUrl = url

}
