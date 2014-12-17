package nz.co.searchwellington.model.frontend

class FrontendImage(var url: String) {

  def this() {
    this("")
  }

  def getUrl = url
  def setUrl (newUrl: String) = {url = newUrl}

}
