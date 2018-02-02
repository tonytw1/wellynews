package nz.co.searchwellington.model

class TagBuilder() {

  var name, autotagHints, displayName: String = null
  var parent: Tag = null

  def autotagHints(autotagHints: String): TagBuilder = {
    this.autotagHints = autotagHints
    this
  }

  def displayName(displayName: String): TagBuilder = {
    this.displayName = displayName
    this
  }

  def name(name: String): TagBuilder = {
    this.name = name
    this
  }

  def parent(parent: Tag): TagBuilder = {
    this.parent = parent
    this
  }

  def build(): Tag = {
    val tag: Tag = new Tag
    tag.setAutotagHints(autotagHints)
    tag.setDisplayName(displayName)
    tag.setName(name)
    //tag.setParent(parent)
    tag
  }

}
