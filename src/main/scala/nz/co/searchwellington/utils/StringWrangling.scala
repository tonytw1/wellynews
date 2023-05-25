package nz.co.searchwellington.utils

import com.google.common.base.{Splitter, Strings}
import org.apache.commons.lang3.StringUtils

import scala.jdk.CollectionConverters.IterableHasAsScala

trait StringWrangling {

  def lowerCappedSentence(input: String): String = {
    if (isCapitalised(input)) {
      input.toLowerCase.capitalize
    } else {
      input
    }
  }

  def trimToCharacterCount(description: String, maxLength: Int): String = {
    if (description.length > maxLength) {
      val trimmed = description.substring(0, maxLength)
      if (trimmed.contains(".")) return StringUtils.substringBefore(trimmed, ".") + "."
    }
    description
  }

  def splitCommaDelimited(commaSeperated: String): Seq[String] = {
    val commaSplitter = Splitter.on(",")
    commaSplitter.split(commaSeperated).asScala.map(_.trim).filter(!Strings.isNullOrEmpty(_)).toSeq
  }

  private def isCapitalised(input: String) = input == input.toUpperCase

}
