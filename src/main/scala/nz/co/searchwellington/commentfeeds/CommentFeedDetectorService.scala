package nz.co.searchwellington.commentfeeds

import nz.co.searchwellington.commentfeeds.detectors.CommentFeedDetector
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class CommentFeedDetectorService @Autowired()(detectors: Array[CommentFeedDetector]) {

  private val log = LogFactory.getLog(classOf[CommentFeedDetectorService])

  log.info("Autowired " + detectors.length + " comment detectors: " + detectors.toSeq.map(_.getClass.getSimpleName).mkString(", "))

  def isCommentFeedUrl(url: String): Boolean = {
    val tuples = detectors.map { d =>
      (d.getClass.getName, d.isValid(url))
    }

    tuples.foreach( t =>
      log.info(t._1 + ": " + t._2)
    )

    tuples.map(_._2).exists(_)

    detectors.find(d => d.isValid(url)).exists { d =>
      log.info(d.getClass.getName + " detected comment feed url: " + url)
      true
    }
  }

}