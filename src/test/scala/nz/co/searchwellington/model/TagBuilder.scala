package nz.co.searchwellington.model

import java.util.UUID

class TagBuilder() {

  var name = "Test " + UUID.randomUUID().toString
  var displayName: String = name
  var autotagHints: Option[String] = None

  def name(name: String): TagBuilder = {
    this.name = name
    this
  }

  def autotagHints(autotagHints: String): TagBuilder = {
    this.autotagHints = Some(autotagHints)
    this
  }

  def displayName(displayName: String): TagBuilder = {
    this.displayName = displayName
    this
  }

  def build(): Tag = {
    Tag(name = name, autotag_hints = autotagHints, display_name = displayName)
  }

}
