package nz.co.searchwellington.views

class EscapeTools {
  def javascript(input: String): String = org.apache.commons.text.StringEscapeUtils.escapeEcmaScript(input)
}