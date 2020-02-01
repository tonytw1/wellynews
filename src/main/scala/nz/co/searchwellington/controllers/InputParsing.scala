package nz.co.searchwellington.controllers

trait InputParsing {

  def optionalInputString(i: String): Option[String] = {
    Option(i).flatMap { t =>
      val trimmed = t.trim
      if (trimmed.nonEmpty) {
        Some(trimmed)
      } else {
        None
      }
    }
  }

}
