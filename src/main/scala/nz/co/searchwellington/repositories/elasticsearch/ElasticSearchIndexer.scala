package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.bulk.BulkResponse
import com.sksamuel.elastic4s.requests.common.DistanceUnit
import com.sksamuel.elastic4s.requests.searches.aggs.HistogramOrder
import com.sksamuel.elastic4s.requests.searches.aggs.responses.bucket.{DateHistogram, Terms}
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.requests.searches.{DateHistogramInterval, SearchRequest}
import com.sksamuel.elastic4s.requests.{bulk => _, delete => _, searches => _}
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, Response}
import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.geocoding.osm.OsmIdParser
import nz.co.searchwellington.instrumentation.SpanFactory
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.geo.{Geocode, OsmId}
import org.apache.commons.logging.LogFactory
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, Interval}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{Await, ExecutionContext, Future}

@Component
class ElasticSearchIndexer @Autowired()(val showBrokenDecisionService: ShowBrokenDecisionService,
                                        val osmIdParser: OsmIdParser,
                                        @Value("${elasticsearch.url}") elasticsearchUrl: String,
                                        @Value("${elasticsearch.index}") elasticsearchIndex: String) extends ElasticFields with ModeratedQueries with ReasonableWaits {

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
            keywordField(FeedAcceptancePolicy),
            dateField(FeedLatestItemDate),
            dateField(LastChanged),
            keywordField(Hostname),
            dateField(AcceptedDate),
            objectField(GeotagVote).copy(properties = Seq(
              textField("address").index(false),
              geopointField(LatLong),
              keywordField("osmId")
            ))
          ))
        }

        val result = Await.result(eventualCreateIndexResult, OneMinute)
        log.info("Create index " + Index + " result: " + result)
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

  def updateMultipleContentItems(resources: Seq[IndexResource])(implicit ec: ExecutionContext): Future[Response[BulkResponse]] = {
    log.debug("Index batch of size: " + resources.size)
    val indexDefinitions = resources.map { indexResource =>
      val resource = indexResource.resource
      val publisher = resource match {
        case p: PublishedResource => p.getPublisher
        case _ => None
      }

      val feedAcceptancePolicy = resource match {
        case f: Feed => Some(f.acceptance)
        case _ => None
      }
      val feedLatestItemDate = resource match {
        case f: Feed => f.latestItemDate
        case _ => None
      }

      val accepted = resource match {
        case n: Newsitem => n.accepted
        case _ => None
      }

      val latLong = indexResource.geocode.flatMap(_.latLong)

      val fields = Seq(
        Some(Type -> resource.`type`),
        Some(Title -> resource.title),
        Some(TitleSort -> resource.title),
        Some(HttpStatus -> resource.http_status.toString),
        resource.description.map(d => Description -> d),
        resource.date.map(d => Date -> new DateTime(d)),
        Some(Tags, indexResource.indexTagIds),
        Some(HandTags, indexResource.handTagIds),
        publisher.map(p => Publisher -> p.stringify),
        Some(Held -> resource.held),
        resource.owner.map(o => Owner -> o.stringify),
        Some(TaggingUsers, resource.resource_tags.map(_.user_id.stringify)),
        feedAcceptancePolicy.map(ap => FeedAcceptancePolicy -> ap.toString),
        feedLatestItemDate.map(fid => FeedLatestItemDate -> new DateTime(fid)),
        resource.last_changed.map(lc => LastChanged -> new DateTime(lc)),
        indexResource.hostname.map(u => Hostname -> u),
        accepted.map(a => AcceptedDate -> new DateTime(a)),
        indexResource.geocode.map ( g => {
          val geotagVoteFields = Seq(
            g.address.map("address" -> _),
            latLong.map(ll => LatLong -> Map("lat" -> ll.getLatitude, "lon" -> ll.getLongitude)),
            g.osmId.map { osmId =>
              "osmId" -> (osmId.id.toString + "/" + osmId.`type`)
            }
          )
          GeotagVote -> geotagVoteFields.flatten.toMap
        })
      )
      indexInto(Index).fields(fields.flatten) id resource._id.stringify
    }

    client.execute(bulk(indexDefinitions))
  }

  def deleteResource(id: BSONObjectID)(implicit ec: ExecutionContext): Future[Boolean] = {
    (client execute deleteById(Index, id.stringify)).map { response =>
      response.isSuccess
    }
  }

  def getResources(query: ResourceQuery, order: SearchRequest => SearchRequest = byDateDescending, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[(Seq[ElasticResource], Long)] = {
    val request = order(search(Index) query composeQueryFor(query, loggedInUser)) start query.startIndex limit query.maxItems

    val span = SpanFactory.childOf(currentSpan, "executeResourceQuery").
      setAttribute("database", "elasticsearch").
      setAttribute("query", query.toString). // TODO human readable
      startSpan()

    val start = DateTime.now()
    val eventualTuple = client.execute(request).map { r =>
      val elasticResources = r.result.hits.hits.toSeq.flatMap { h =>
        BSONObjectID.parse(h.id).toOption.map { bid =>
          val handTags = h.sourceAsMap.get(HandTags).asInstanceOf[Option[List[String]]].map(_.flatMap(tid => BSONObjectID.parse(tid).toOption)).getOrElse(Seq.empty)
          val indexTags = h.sourceAsMap.get(Tags).asInstanceOf[Option[List[String]]].map(_.flatMap(tid => BSONObjectID.parse(tid).toOption)).getOrElse(Seq.empty)

          // Hydrate geotag from nested object fields; there must be a more direct way of doing this?
          val geotagVoteFields = h.sourceAsMap.get(GeotagVote).asInstanceOf[Option[Map[String, Object]]]
          val geocode = geotagVoteFields.map { fields =>
            val maybeAddress = fields.get("address")
            val maybeLatLong = fields.get(LatLong).flatMap { ll: Object =>
              val llMap = ll.asInstanceOf[Map[String, Double]]
              llMap.get("lat").flatMap { lat =>
                llMap.get("lon").map { lon =>
                  geo.LatLong(lat, lon)
                }
              }
            }

            val maybeOsmId = fields.get("osmId").map { asString =>
              val parsed = osmIdParser.parseOsmId(asString.asInstanceOf[String]) // TODO use one Osm id class; even if it means working out enums in Mongo
              OsmId(parsed.getId, parsed.getType.toString)
            }
            val address = maybeAddress.map(_.asInstanceOf[String])

            Geocode(address = address, latLong = maybeLatLong, osmId = maybeOsmId)
          }

          ElasticResource(bid, handTags, indexTags, geocode)
        }
      }
      (elasticResources, r.result.totalHits)
    }
    eventualTuple.map { r =>
      val duration = new org.joda.time.Duration(start, DateTime.now)
      span.setAttribute("fetched", r._1.size)
      span.end()
      log.debug("Elastic query " + query + " took: " + duration.getMillis + " ms")
      r
    }
  }

  def getPublisherAggregationFor(query: ResourceQuery, loggedInUser: Option[User], size: Option[Int] = None)(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[(String, Long)]] = {
    getAggregationFor(query, Publisher, loggedInUser, size)
  }

  def getTagAggregationFor(query: ResourceQuery, loggedInUser: Option[User], size: Option[Int] = None)(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[(String, Long)]] = {
    getAggregationFor(query, Tags, loggedInUser, size)
  }

  def getTypeCounts(loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[(String, Long)]] = {
    val allResources = ResourceQuery()
    getAggregationFor(allResources, "type", loggedInUser)
  }

  def createdAcceptedDateAggregationFor(query: ResourceQuery, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[(String, Long)]] = {
    val span = SpanFactory.childOf(currentSpan, "createdAcceptedDateAggregationFor").
      setAttribute("database", "elasticsearch").
      setAttribute("query", query.toString).
      setAttribute("aggregationName", AcceptedDate).
      startSpan()

    val dateDay = dateHistogramAgg(AcceptedDate, AcceptedDate).
      calendarInterval(DateHistogramInterval.Day).format("YYYY-MM-dd").
      order(HistogramOrder.KEY_DESC).
      minDocCount(1)

    val request = search(Index) query composeQueryFor(query, loggedInUser) limit 0 aggregations Seq(dateDay)

    client.execute(request).map { r =>
      val dateAgg = r.result.aggs.result[DateHistogram](AcceptedDate)
      span.setAttribute("buckets", dateAgg.buckets.size)
      span.end()
      dateAgg.buckets.map { b =>
        val day = b.date
        (day, b.docCount)
      } // TODO compared to month aggregation; who should do the date parsing?
    }
  }

  def createdMonthAggregationFor(query: ResourceQuery, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[(Interval, Long)]] = {
    val span = SpanFactory.childOf(currentSpan, "createdMonthAggregationFor").
      setAttribute("database", "elasticsearch").
      setAttribute("query", query.toString).
      setAttribute("aggregationName", Date).
      startSpan()

    val dateMonth = dateHistogramAgg(Date, Date).calendarInterval(DateHistogramInterval.Month).order(HistogramOrder.KEY_DESC).minDocCount(1)

    val request = search(Index) query composeQueryFor(query, loggedInUser) limit 0 aggregations Seq(dateMonth)

    client.execute(request).map { r =>
      val dateAgg = r.result.aggs.result[DateHistogram](Date)
      span.setAttribute("buckets", dateAgg.buckets.size)
      span.end()

      dateAgg.buckets.map { b =>
        val startOfMonth = ISODateTimeFormat.dateTimeParser().parseDateTime(b.date)
        val month = new Interval(startOfMonth, startOfMonth.plusMonths(1))
        (month, b.docCount)
      }
    }
  }

  private def getAggregationFor(query: ResourceQuery, aggName: String, loggedInUser: Option[User], size: Option[Int] = None)(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[(String, Long)]] = {
    val span = SpanFactory.childOf(currentSpan, "getAggregationFor").
      setAttribute("database", "elasticsearch").
      setAttribute("query", query.toString).
      setAttribute("aggregationName", aggName).
      startSpan()

    val aggs = Seq(termsAgg(aggName, aggName) size size.getOrElse(Integer.MAX_VALUE))
    val request = (search(Index) query composeQueryFor(query, loggedInUser)) limit 0 aggregations aggs
    client.execute(request).map { r =>
      val terms = r.result.aggs.result[Terms](aggName)
      span.setAttribute("buckets", terms.buckets.size)
      span.end()
      terms.buckets.map(b => (b.key, b.docCount))
    }
  }

  private def composeQueryFor(query: ResourceQuery, loggedInUser: Option[User]): Query = {
    val latLongField = GeotagVote + "." + LatLong

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
        rangeQuery(Date) gte i.getStartMillis lt i.getEndMillis
      },
      query.before.map { d =>
        rangeQuery(Date) lt d.getMillis
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
          existsQuery(latLongField)
        } else {
          not(existsQuery(latLongField))
        }
      },
      query.circle.map { c =>
        geoDistanceQuery(latLongField, c.centre.getLatitude, c.centre.getLongitude).distance(c.radius, DistanceUnit.KILOMETERS)
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

}

case class ElasticResource(_id: BSONObjectID, handTags: Seq[BSONObjectID], indexTags: Seq[BSONObjectID], geocode: Option[Geocode])
