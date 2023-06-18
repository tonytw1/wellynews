package nz.co.searchwellington.forms

import com.google.common.collect.Lists
import jakarta.validation.constraints.NotBlank

import java.util

class NewWebsite {
  @NotBlank
  private var title: String = _
  @NotBlank
  private var url: String = _

  private var tags: util.List[String] = Lists.newArrayList

  def getTitle: String = title

  def setTitle(title: String): Unit = {
    this.title = title
  }

  def getUrl: String = url

  def setUrl(url: String): Unit = this.url = url

  def getTags: util.List[String] = tags

  def setTags(tags: util.List[String]): Unit = {
    this.tags = tags
  }

  override def toString: String = "NewWebsite{" + "title='" + title + '\'' + ", url='" + url + '\'' + '}'
}