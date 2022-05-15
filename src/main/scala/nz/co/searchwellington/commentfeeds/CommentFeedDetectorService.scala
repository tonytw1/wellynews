package nz.co.searchwellington.commentfeeds

import nz.co.searchwellington.commentfeeds.detectors.CommentFeedDetector
import nz.co.searchwellington.model.Resource
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.net.URL

@Component class CommentFeedDetectorService @Autowired()(detectors: Array[CommentFeedDetector]) {

  private val log = LogFactory.getLog(classOf[CommentFeedDetectorService])

  log.info("Autowired " + detectors.length + " comment detectors: " + detectors.toSeq.map(_.getClass.getSimpleName).mkString(", "))

  def isCommentFeedUrl(url: URL, source: Resource): Boolean = {
    detectors.find(_.isValid(url, source)).exists { d =>
      log.info(d.getClass.getName + " detected comment feed url: " + url)
      true
    }
  }

}