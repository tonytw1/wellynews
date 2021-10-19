# wellynews

Source code for https://wellington.gen.nz and [@wellynews](https://twitter.com/wellynews).

Aggregates community news from my hometown Wellington, New Zealand into a newslog.
Content is curated from community group websites and RSS feeds.

The content is automatically tagged and can be output as customised RSS feeds.

For example:
- Transport related newsitems (a tag feed)
- Consultation newsitems relating to the Central Library (a tag combiner feed)
- Newsitems Wellington City Council has published about the airport runway extension (a publisher tag combiner feed).

This a long running project (> 10 years of continuous operation) and the code base has changed alot over the years.

Currently implemented as Scala controllers served from Spring Boot and built with Maven :grimace:
MongoDB is used for persistence and Elasticsearch for indexing.


## Related services

Specific details of RSS and Twitter have been pushed into these potentially reusable services:

- [Whakaoko](https://github.com/tonytw1/whakaoko) for RSS feed polling and aggregation.
- [RSS to Twitter](https://github.com/tonytw1/rsstotwitter) for automatic publishing to the @wellynews Twitter account.
- [Brownbag](https://github.com/tonytw1/rsstotwitter) for screen scraping of interesting content into RSS feeds for easier ingression


## Model

### Website

The website of a content publisher such as Wellington City Council.  
Newsitems and feeds found on this website will be attributed to this publisher.

### Newsitems

A news item published on a publisher's website. A page with a unique URL containing a press release or a match report.

### Feeds

An RSS feed published by a given website. Newsitems accepted from this feed will be attributed to the feed's publisher.

### Watchlist

A page on a publishers website which is known to contain links to new newsitems.
For publishers with interesting content who do not provide a feed watchlist page.
This might be a homepage or a news page.
Polled regularly to detect changes which might indicate new newsitems.
See detecting page changes below.


## Tagging

Been able to automatically arrange newsitems into meaningful categories like consultation and transport is something we really want.
We use tags todo this and infer an items tags from what we know about where it came from.

### Index tags

Index tags determine which tags give resources appear under.
For example an item tagged trains will also appear under transport.
Some slightly interesting things happen to automatically calculate the index tags

#### Tagging votes

The different contributors to an items tags are represented by tagging votes.
These votes are counted to determine an items actual tagging.

There are different types of tagging votes.

##### Hand tagging

Represents a tag applied directly by a user.

##### Publisher tags

News items inherit tags from their publisher.
For well categorised publishers such as transport operators or sports clubs this approach can tag their newsitems with a high level of confidence.

##### Feed tags

News items accepted from a feed inherit the hand taggings applied to the feed.

Some publishers has multiple feeds which each cover a very specific topic (such as the city council's planning applications).
In this case a feed tagging  very accurately tag the newsitems from that feed.

##### Ancestor tags

Includes the ancestors of applied tags.
Been tagged as Trains implies that this newsitem is related to Transport.

##### Geotag votes

Votes which contribute to the visible location of a newsitem. Could be an explict geocode of could be inferred from the
newsitems tags or publisher


### Autotagging

When newsitem text matches keywords associated with specific tags we apply an autotagging.
This is represented as a hand tagging applied by the autotagger user.



### Detecting page changes

Changes in pages can be detected by periodically downloading and checking them.
Changes in content checksums indicate potential new content.

Pages often contain elements such as timestamps which mean a pages checksum changes even if contains no new content.
This should be filtered out before checksum pages.




## Local dev

Use docker to provide local copies of the MongoDB, Elasticsearch, Memcached andRabbitMQ dependencies.

```
docker-compose -f docker/docker-compose.yml up
```

Start locally.
```
mvn spring-boot:run
```

## Cloud build

```
gcloud components install cloud-build-local
cloud-build-local --config=cloudbuild.yaml --dryrun=false --push=false .
```
