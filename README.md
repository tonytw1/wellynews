# wellynews

Source code for https://wellington.gen.nz and [@wellynews](https://twitter.com/wellynews).

Aggregates community news items and RSS feeds from my hometown Wellington, New Zealand into a newslog.

The content is automatically categorised and can be output as customised RSS feeds.

For example:
- [Transport related newsitems](https://wellington.gen.nz/transport) (a tag feed)
- [Consultation about the Central Library](https://wellington.gen.nz/consultation+central-library) (a tag combiner feed)
- Newsitems published by [Wellington City Council about the draft spatial plan](https://wellington.gen.nz/wellington-city-council+draft-spatial-plan) (a publisher tag combiner feed).

This a long running project (> 10 years of continuous operation) and the code base has changed alot over the years.

Currently implemented as Scala controllers served from Spring Boot and built with Maven.
MongoDB is used for persistence and Elasticsearch for indexing.


## Related services

Specific concerns have been pushed into these potentially reusable services:

- [Whakaoko](https://github.com/tonytw1/whakaoko) for RSS feed polling and aggregation.
- [RSS to Twitter](https://github.com/tonytw1/rsstotwitter) for automatic publishing to the @wellynews Twitter account.
- [Cards](https://github.com/tonytw1/cards) for decorating news items with social media images.
- [Brownbag](https://github.com/tonytw1/brownbag) for screen scraping interesting content into RSS feeds for easier ingression


## Model

### Website

The website of a content publisher such as Wellington City Council.  
Newsitems and feeds found on this website will be attributed to this publisher.

### Newsitem

A news item published on a publisher's website.
A page with a unique URL containing a press release or a match report.


### Feeds

An RSS feed published by a given website.
Newsitems accepted from this feed will be attributed to this feed's publisher.


### Watchlist

A page on a publishers website which is known to contain links to new newsitems.
This might be a homepage or a news page.

Used when a publisher with interesting content does not provide a feed.

Polled regularly to detect changes which might indicate new newsitems.
See detecting page changes (below)[(#detecting-changes)].


## Tagging

Been able to automatically arrange newsitems into meaningful categories like consultation and transport is something we really want.
We use tags todo this.

Tags have a name and an optional parent tag.

Tags can be arranged into a hierarchy.
ie. Trains is a child of Transport.

Newsitems about trains should be included in the transport tag's newsitems.



### Index tags

Index tags determine which tags give resources appear under.
For example an item tagged trains will also appear under transport.
Some slightly interesting things happen to automatically calculate the index tags

We can infer alot about an item's tags from what we know about where it came from.


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


#### RSS feed item categories

If an RSS feed item contains RSS `category` tags, the autotagger wil try to match the values of these category tags
to tag autotag hints.

An item with a category of 'events' will be matched to the tag Events.


### Detecting changes

Changes in pages can be detected by periodically downloading and checking them.
Changes in content checksums indicate potential new content.

Pages often contain elements such as timestamps which mean a pages checksum changes even if contains no new content.
This should be filtered out before checksum pages.


### Accepted feed items view

Shows the news items which have been accepted from feeds on a particular day.
This is useful for moderation and discovering items which could benefit from having additional tags applied.

![Accepted feed items](docs/accepted-feed-items.png)


### Social media Cards / Open Graph images

News items are decorated with Twitter Cards and Open Graph social media images
using the [Cards service](https://github.com/tonytw1/cards).

We try to detect and filter out images which are a publisher's generic filler images.

ie.
This generic logo should not be included but a article specific images should be.
![Duplicate images](docs/duplicate-images.png)


### Admin actions

#### Backfill new tag

The autotag prompt allows a new tag to be backfilled with existing news items which match the new 
tag's autotagging rules.

#### Gather publisher resources

Given a publisher find unassigned newsitems and feeds which probably belong to this publisher.

This decision is based on url hostnames.


## Local dev

Use docker to provide local copies of the MongoDB, Elasticsearch, Memcached andRabbitMQ dependencies.

```
docker compose -f docker/docker-compose.yml up
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
