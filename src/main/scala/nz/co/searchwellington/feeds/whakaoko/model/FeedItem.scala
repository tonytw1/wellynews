package nz.co.searchwellington.feeds.whakaoko.model

import org.joda.time.DateTime

import java.util.Date

case class FeedItem(id: String,
                    title: Option[String] = None,
                    body: Option[String] = None,
                    subscriptionId: String,
                    url: String,
                    imageUrl: Option[String] = None,
                    date: Option[Date] = None,
                    place: Option[Place] = None,
                    categories: Option[Seq[Category]] = None,
                    accepted: Option[DateTime] = None
                   )
