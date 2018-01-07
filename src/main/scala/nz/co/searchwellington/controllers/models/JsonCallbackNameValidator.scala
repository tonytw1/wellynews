package nz.co.searchwellington.controllers.models

import org.springframework.stereotype.Component

@Component class JsonCallbackNameValidator {

  private val VALID_CALLBACK_NAME_REGEX: String = "[a-z|A-Z|0-9|_]+"

  def isValidCallbackName(callback: String): Boolean = {
    return callback.matches(VALID_CALLBACK_NAME_REGEX)
  }

}