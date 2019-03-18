package nz.co.searchwellington.model.frontend

case class FrontendTag(id: String, name: String, displayName: String, description: String) {

  def getId(): String = {
    id
  }

  def getDisplayName(): String = {
    displayName
  }

  def getName(): String = {
    name
  }

  def getDescription(): String = {
    description
  }

}


