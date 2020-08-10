# wellynews

Source code for https://wellington.gen.nz/

Aggregates content from my hometown and attempts to automagically tag it.

A long running code base where new ideas are tried out. Mileage may vary.

Currently Scala controllers behind Spring framework built with Maven :grimace:


## Local dev

Use docker to provide local MongoDB, Elasticsearch, Memcached andRabbitMQ dependencies.

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

Represents a tag applied directly by a user

#### Publisher tags

News items inherit the tags their publisher have

#### Feed tags

News items accepted from a feed inherit the hand taggings applied to the feed.

#### Ancestor

Includes the ancestors of applied tags.
ie. Trains implies Transport.


## Autotagger

