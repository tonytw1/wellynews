# wellynews

Source code for https://wellington.gen.nz/ and [@wellynews](https://twitter.com/wellynews).

Aggregates community news from my hometown Wellington, New Zealand.
Content is curated from community group websites and RSS feeds.

This a long running project (> 10 years of continuous operation).

The code base has changed alot over the years as development practises have evolved.
Currently implemented as Scala controllers served from Spring Boot and built with Maven :grimace:

## Related services

Specific details of RSS and Twitter have been pushed into these potentially reusable services:

- [Whakaoko](https://github.com/tonytw1/whakaoko) for RSS feed polling and aggregation.
- [RSS to Twitter](https://github.com/tonytw1/rsstotwitter) for automatic publishing to the @wellynews Twitter account.


## Local dev

Use docker to provide local copies of the MongoDB, Elasticsearch, Memcached andRabbitMQ dependencies.

```
docker-compose -f docker/docker-compose.yml up
```

Start locally.
```
mvn spring-boot:run
```


## Index tags

Index tags determine which tags give resources appear under.
Some slightly interesting things happen to automatically calculate the index tags

### Tagging votes

#### Hand tagging

Represents a tag applied directly by a user.

#### Publisher tags

News items inherit tags from their publisher.

For well categorised publishers such as transport operators or sports clubs this approach places their newsitems with a
high level of confidence.

#### Feed tags

News items accepted from a feed inherit the hand taggings applied to the feed.

When a publisher has multiple feeds each feed generally has a very specific topic (such as a city council's planning applications).
In this case a feed tagging can be be very accurate.

#### Ancestor tags

Includes the ancestors of applied tags.
Been tagged as Trains implies that this newsitem is related to Transport.

## Autotagger

