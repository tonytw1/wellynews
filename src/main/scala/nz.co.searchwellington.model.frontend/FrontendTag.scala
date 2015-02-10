package nz.co.searchwellington.model.frontend

@SerialVersionUID(1L)
class FrontendTag(var id: String, var name: String) {

  def this() {
    this("", "")
  }

  def getId = id
  def setId (newId: String) = {id = newId}

  def getName = name
  def setName(newName: String) = {name = newName}

}