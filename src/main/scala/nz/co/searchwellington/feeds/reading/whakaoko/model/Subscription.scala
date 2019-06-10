package nz.co.searchwellington.feeds.reading.whakaoko.model

import org.joda.time.DateTime

case class Subscription(id: String,
                        name: String,
                        channelId: String,
                        url: String,
                        lastRead: Option[DateTime],
                        latestItemDate: Option[DateTime]
                       )
