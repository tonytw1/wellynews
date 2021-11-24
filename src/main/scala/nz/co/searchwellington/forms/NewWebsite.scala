package nz.co.searchwellington.forms

import org.hibernate.validator.constraints.NotBlank

class NewWebsite {
  @NotBlank
  private var title: String = _
  @NotBlank
  private var url: String = _

  def getTitle: String = title

  def setTitle(title: String): Unit = {
    this.title = title
  }

  def getUrl: String = url

  def setUrl(url: String): Unit = this.url = url

  override def toString: String = "NewWebsite{" + "title='" + title + '\'' + ", url='" + url + '\'' + '}'
}