package nz.co.searchwellington.feeds.reading.whakaoko.model

import org.joda.time.DateTime

case class FeedItem(id: String,
                    title: Option[String] = None,
                    body: Option[String] = None,
                    subscriptionId: String,
                    url: String,
                    imageUrl: Option[String] = None,
                    date: Option[DateTime] = None,
                    geoTagged: Boolean) // TODO Place
