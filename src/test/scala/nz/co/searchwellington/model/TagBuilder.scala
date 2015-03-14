package nz.co.searchwellington.model

class TagBuilder() {

  var name, autotagHints: String = null;

  def name(name: String): TagBuilder = {
    this.name = name
    this
  }

  def autotagHints(autotagHints: String): TagBuilder = {
    this.autotagHints = autotagHints
    this
  }

  def build(): Tag = {
    val tag: Tag = new Tag
    tag.setName(name)
    tag.setAutotagHints(autotagHints)
    tag
  }

}
