package nz.co.searchwellington.commentfeeds

import nz.co.searchwellington.commentfeeds.detectors.CommentFeedDetector
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class CommentFeedDetectorService @Autowired()(detectors: Array[CommentFeedDetector]) {

  private val log = Logger.getLogger(classOf[CommentFeedDetectorService])

  log.info("Autowired " + detectors.length + " comment detectors: " + detectors.toSeq.map(_.getClass.getSimpleName).mkString(", "))

  def isCommentFeedUrl(url: String): Boolean = {
    detectors.find(d => d.isValid(url)).exists { d =>
      log.info(d.getClass.getName + " detected comment feed url: " + url)
      true
    }
  }

}