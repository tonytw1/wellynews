# wellynews

Source code for https://wellington.gen.nz/ and [@wellynews](https://twitter.com/wellynews).

Aggregates community news from my hometown Wellington, New Zealand into a newslog.
Content is curated from community group websites and RSS feeds.

The content is tagged and can be output as customised RSS feeds.

For example:

- Transport related newsitems (a tag feed)
- Consultation newsitems relating to the Central Library (a tag combiner feed)
- Newsitems Wellington City Council has published about the airport runway extenstion (a publisher tag combinder feed).

This a long running project (> 10 years of continuous operation).

The code base has changed alot over the years as development practises have evolved.
Currently implemented as Scala controllers served from Spring Boot and built with Maven :grimace:


## Local dev

Use docker to provide local copies of the MongoDB, Elasticsearch, Memcached andRabbitMQ dependencies.

```
docker-compose -f docker/docker-compose.yml up
```

Start locally.
```
mvn spring-boot:run
```


## Related services

Specific details of RSS and Twitter have been pushed into these potentially reusable services:

- [Whakaoko](https://github.com/tonytw1/whakaoko) for RSS feed polling and aggregation.
- [RSS to Twitter](https://github.com/tonytw1/rsstotwitter) for automatic publishing to the @wellynews Twitter account.


## Model

### Website

The website of a content publisher such as Wellington City Council.  
Newsitems and feeds found on this website will be attributed to this publisher.

### Newsitems

A news item published on a publisher's website. A page with a unique URL containing a press release or a match report.

### Feeds

An RSS feed published by a given website. Newsitems accepted from this feed will be attributed to the feed's publisher.

### Watchlist

A page on a publishers website which is known to contain links to new newsitems. This might be a homepage or a news page.
For publishers with interesting content who do not provide a feed watchlist page can be regularly polled to detect changes which might
indicate new newsitems.


## Tagging

### Index tags

Index tags determine which tags give resources appear under.
Some slightly interesting things happen to automatically calculate the index tags

#### Tagging votes

##### Hand tagging

Represents a tag applied directly by a user.

##### Publisher tagsNews items inherit tags from their publisher.For well categorised publishers such as transport operators or sports clubs this approach places their newsitems with a
high level of confidence.

##### Feed tags

News items accepted from a feed inherit the hand taggings applied to the feed.

When a publisher has multiple feeds each feed generally has a very specific topic (such as a city council's planning applications).
In this case a feed tagging can be be very accurate.

##### Ancestor tags

Includes the ancestors of applied tags.
Been tagged as Trains implies that this newsitem is related to Transport.

### Autotagging

