package nz.co.searchwellington.model.frontend

case class FrontendTag(id: Int, name: String, displayName: String, description: String) {

  def getId(): Int = {
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


