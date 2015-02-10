package nz.co.searchwellington.model.frontend

@SerialVersionUID(1L)
class FrontendImage(var url: String) {

  def this() {
    this("")
  }

  def getUrl = url
  def setUrl (newUrl: String) = {url = newUrl}

}
