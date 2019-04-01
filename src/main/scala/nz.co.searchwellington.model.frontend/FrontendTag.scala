package nz.co.searchwellington.model.frontend

case class FrontendTag(id: String, name: String, displayName: String, description: String, children: Seq[FrontendTag] = Seq.empty) {

  def getId: String = id

  def getDisplayName: String = displayName

  def getName: String = name

  def getDescription: String = description

  def getChildren: java.util.List[FrontendTag] = {
    import scala.collection.JavaConverters._
    children.asJava
  }

}