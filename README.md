# wellynews

Source code for http://wellington.gen.nz/

Aggregates content from my hometown and attempts to automagically tag it.

A long running code base where new ideas are tried out.
Milage may vary.

## Local dev

docker-compose -f docker/docker-compose.yml up

mvn tomcat7:run





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

