package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.requests.common.DistanceUnit
import com.sksamuel.elastic4s.requests.indexes.IndexRequest
import com.sksamuel.elastic4s.requests.searches.aggs.responses.bucket.{DateHistogram, Terms}
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.requests.searches.{DateHistogramInterval, SearchRequest}
import com.sksamuel.elastic4s.requests.{bulk => _, delete => _, searches => _}
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, Response}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model._
import nz.co.searchwellington.tagging.IndexTagsService
import org.apache.commons.logging.LogFactory
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, Interval}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

@Component
class ElasticSearchIndexer @Autowired()(val showBrokenDecisionService: ShowBrokenDecisionService,
                                        @Value("${elasticsearch.url}") elasticsearchUrl: String,
                                        @Value("${elasticsearch.index}") elasticsearchIndex: String,
                                        indexTagsService: IndexTagsService) // TODO this should have been dealt with higher up
  extends ElasticFields with ModeratedQueries with ReasonableWaits {

  def byDateDescending(request: SearchRequest): SearchRequest = request sortByFieldDesc Date

  def byTitleAscending(request: SearchRequest): SearchRequest = request sortByFieldAsc TitleSort

  def byLastChangedDescending(request: SearchRequest): SearchRequest = request sortByFieldDesc LastChanged

  def byFeedLatestFeedItemDate(request: SearchRequest): SearchRequest = request sortByFieldDesc FeedLatestItemDate

  def byAcceptedDate(request: SearchRequest): SearchRequest = request sortByFieldDesc AcceptedDate

  private val log = LogFactory.getLog(classOf[ElasticSearchIndexer])

  private val Index = elasticsearchIndex

  private val client = {
    import scala.concurrent.ExecutionContext.Implicits.global
    def ensureIndexes(client: ElasticClient): Unit = {
      val exists = Await.result((client execute indexExists(Index)).map { r =>
        if (r.isSuccess) {
          log.info("Elastic index exists")
          r.result.exists
        } else {
          throw new RuntimeException("Could not determine if index exists")
        }
      }, OneMinute)

      if (!exists) {
        log.info("Index does not exist; creating")
        createIndexes(client)
      }
    }

    def createIndexes(client: ElasticClient): Unit = {
      try {
        val eventualCreateIndexResult = client.execute {
          createIndex(Index).mapping(properties(
            textField(Title),
            keywordField(TitleSort),
            keywordField(Type),
            dateField(Date),
            textField(Description),
            keywordField(Tags),
            keywordField(HandTags),
            keywordField(TaggingUsers),
            keywordField(Publisher),
            booleanField(Held),
            keywordField(Owner),
            geopointField(LatLong),
            keywordField(FeedAcceptancePolicy),
            dateField(FeedLatestItemDate),
            dateField(LastChanged),
            keywordField(Hostname),
            dateField(AcceptedDate),
          ))
        }

        val result = Await.result(eventualCreateIndexResult, OneMinute)
        log.info("Create index result: " + result)
        if (!result.isSuccess) {
          log.info("Create index failed; throwing exception")
          throw new RuntimeException("Could not create elastic index: " + result.error.reason)
        }

      } catch {
        case e: Exception =>
          log.error("Failed to create elastic index", e)
          throw new RuntimeException(e)
      }
    }

    log.info("Connecting to Elasticsearch url: " + elasticsearchUrl)
    val props = ElasticProperties(elasticsearchUrl)
    val client = ElasticClient(JavaClient(props))
    ensureIndexes(client)
    client
  }

  def updateMultipleContentItems(resources: Seq[(Resource, Seq[String], Seq[String])])(implicit ec: ExecutionContext): Future[Response[BulkResponse]] = {
    log.debug("Index batch of size: " + resources.size)

    val eventualIndexDefinitions: Seq[Future[IndexRequest]] = resources.map { r =>
      for {
        geocode <- indexTagsService.getIndexGeocodeForResource(r._1)

      } yield {
        val publisher = r._1 match {
          case p: PublishedResource => p.getPublisher
          case _ => None
        }

        val feedAcceptancePolicy = r._1 match {
          case f: Feed => Some(f.acceptance)
          case _ => None
        }
        val feedLatestItemDate = r._1 match {
          case f: Feed => f.latestItemDate
          case _ => None
        }

        val hostname = Try {
          new java.net.URL(r._1.page)
        }.toOption.map { url =>
          url.getHost
        }

        val accepted = r._1 match {
          case n: Newsitem => n.accepted
          case _ => None
        }

        val latLong = geocode.flatMap(_.latLong)

        val fields = Seq(
          Some(Type -> r._1.`type`),
          Some(Title -> r._1.title),
          Some(TitleSort -> r._1.title),
          Some(HttpStatus -> r._1.http_status.toString),
          r._1.description.map(d => Description -> d),
          r._1.date.map(d => Date -> new DateTime(d)),
          Some(Tags, r._2),
          Some(HandTags, r._3),
          publisher.map(p => Publisher -> p.stringify),
          Some(Held -> r._1.held),
          r._1.owner.map(o => Owner -> o.stringify),
          latLong.map(ll => LatLong -> Map("lat" -> ll.getLatitude, "lon" -> ll.getLongitude)),
          Some(TaggingUsers, r._1.resource_tags.map(_.user_id.stringify)),
          feedAcceptancePolicy.map(ap => FeedAcceptancePolicy -> ap.toString),
          feedLatestItemDate.map(fid => FeedLatestItemDate -> new DateTime(fid)),
          r._1.last_changed.map(lc => LastChanged -> new DateTime(lc)),
          hostname.map(u => Hostname -> u),
          accepted.map(a => AcceptedDate -> new DateTime(a))
        )

        indexInto(Index).fields(fields.flatten) id r._1._id.stringify
      }
    }

    Future.sequence(eventualIndexDefinitions).flatMap { indexDefinitions =>
      client.execute(bulk(indexDefinitions))
    }
  }

  def deleteResource(id: BSONObjectID)(implicit ec: ExecutionContext): Future[Boolean] = {
    (client execute deleteById(Index, id.stringify)).map { response =>
      response.isSuccess
    }
  }

  def getResources(query: ResourceQuery, order: SearchRequest => SearchRequest = byDateDescending, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[(Seq[BSONObjectID], Long)] = {
    executeResourceQuery(query, order, loggedInUser)
  }

  def getPublisherAggregationFor(query: ResourceQuery, loggedInUser: Option[User], size: Option[Int] = None)(implicit ec: ExecutionContext): Future[Seq[(String, Long)]] = {
    getAggregationFor(query, Publisher, loggedInUser, size)
  }

  def getAggregationFor(query: ResourceQuery, aggName: String, loggedInUser: Option[User], size: Option[Int] = None)(implicit ec: ExecutionContext): Future[Seq[(String, Long)]] = {
    val aggs = Seq(termsAgg(aggName, aggName) size size.getOrElse(Integer.MAX_VALUE))
    val request = (search(Index) query composeQueryFor(query, loggedInUser)) limit 0 aggregations aggs
    client.execute(request).map { r =>
      r.result.aggs.result[Terms](aggName).buckets.map(b => (b.key, b.docCount))
    }
  }

  def getTypeCounts(loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[Map[String, Long]] = {
    val everyThing = matchAllQuery
    val aggs = Seq(termsAgg("type", "type"))
    val request = search(Index) query withModeration(everyThing, loggedInUser) limit 0 aggregations aggs

    client.execute(request).map { r =>
      val typeAgg = r.result.aggs.result[Terms]("type")
      typeAgg.buckets.map { b =>
        (b.key, b.docCount)
      }.toMap
    }
  }

  private def executeResourceQuery(query: ResourceQuery, order: SearchRequest => SearchRequest, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[(Seq[BSONObjectID], Long)] = {
    val request = order(search(Index) query composeQueryFor(query, loggedInUser)) start query.startIndex limit query.maxItems

    val start = DateTime.now()
    val eventualTuple: Future[(Seq[BSONObjectID], Long)] = client.execute(request).map { r =>
      val hits = r.result.hits.hits
      val ids = hits.map(h => BSONObjectID.parse(h.id).get)
      val total = r.result.totalHits
      (ids, total)
    }

    eventualTuple.map { r =>
      val duration = new org.joda.time.Duration(start, DateTime.now)
      log.debug("Elastic query " + query + " took: " + duration.getMillis + " ms")
      r
    }
  }

  private def composeQueryFor(query: ResourceQuery, loggedInUser: Option[User]): Query = {
    val conditions = Seq(
      query.`type`.map { `type` =>
        should {
          `type`.map { t =>
            matchQuery(Type, t)
          }
        }
      },
      query.tags.map { tags =>
        must {
          tags.map { t =>
            matchQuery(Tags, t._id.stringify)
          }
        }
      },
      query.publisher.map { p =>
        matchQuery(Publisher, p._id.stringify)
      },
      query.interval.map { i =>
        rangeQuery("date") gte i.getStartMillis lt i.getEndMillis
      },
      query.before.map { d =>
        rangeQuery("date") lt d.getMillis
      },
      query.q.map { qt =>
        val titleMatches = matchQuery(Title, qt).boost(5)
        val descriptionMatches = matchQuery(Description, qt)
        should(titleMatches, descriptionMatches)
      },
      query.owner.map { o =>
        termQuery(Owner, o.stringify)
      },
      query.geocoded.map { g =>
        if (g) {
          existsQuery(LatLong)
        } else {
          not(existsQuery(LatLong))
        }
      },
      query.circle.map { c =>
        geoDistanceQuery(LatLong, c.centre.getLatitude, c.centre.getLongitude).distance(c.radius, DistanceUnit.KILOMETERS)
      },
      query.taggingUser.map { tu =>
        matchQuery(TaggingUsers, tu.stringify)
      },
      query.feedAcceptancePolicy.map { ap =>
        matchQuery(FeedAcceptancePolicy, ap.toString)
      },
      query.held.map { h =>
        matchQuery(Held, h)
      },
      query.hostname.map { h =>
        matchQuery(Hostname, h)
      },
      query.notPublishedBy.map { p =>
        not(matchQuery(Publisher, p._id.stringify))
      }
    ).flatten

    withModeration(must(conditions), loggedInUser)
  }

  def createdAcceptedDateAggregationFor(query: ResourceQuery, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[Seq[(String, Long)]] = {
    val aggs = Seq(dateHistogramAgg("accepted", "accepted").calendarInterval(DateHistogramInterval.Day).format("YYYY-MM-dd"))
    val request = search(Index) query composeQueryFor(query, loggedInUser) limit 0 aggregations aggs

    client.execute(request).map { r =>
      val dateAgg = r.result.aggs.result[DateHistogram]("accepted")
      val acceptedDays = dateAgg.buckets.map { b =>
        val day = b.date
        (day, b.docCount)
      }
      acceptedDays.filter(_._2 > 0).reverse // TODO compared to month aggregation; who should do the date parsing?
    }
  }

  def createdMonthAggregationFor(query: ResourceQuery, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[Seq[(Interval, Long)]] = {
    val aggs = Seq(dateHistogramAgg("date", "date").calendarInterval(DateHistogramInterval.Month))
    val request = search(Index) query composeQueryFor(query, loggedInUser) limit 0 aggregations aggs

    client.execute(request).map { r =>
      val dateAgg = r.result.aggs.result[DateHistogram]("date")
      val archiveLinks = dateAgg.buckets.map { b =>
        val startOfMonth = ISODateTimeFormat.dateTimeParser().parseDateTime(b.date)
        val month = new Interval(startOfMonth, startOfMonth.plusMonths(1))
        (month, b.docCount)
      }
      archiveLinks.filter(_._2 > 0).reverse
    }
  }

}
