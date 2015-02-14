package nz.co.searchwellington.model.frontend

class FrontendTag(var id: String, var name: String) extends java.io.Serializable {

  def this() {
    this("", "")
  }

  def getId = id
  def setId (newId: String) = {id = newId}

  def getName = name
  def setName(newName: String) = {name = newName}

}