## Import MySQL dump into Mongo

```
mongify translation database.config > translation.rb
mongify process database.config  translation.rb
mongo searchwellington < transform/go.js
```
