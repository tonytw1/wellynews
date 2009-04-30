package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.decoraters.highlighting.KeywordHighlightingNewsitemDecorator;
import nz.co.searchwellington.model.decoraters.highlighting.KeywordHighlightingWebsiteDecorator;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.LockObtainFailedException;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;




public class LuceneBackedResourceDAO extends HibernateResourceDAO implements ResourceRepository {

    Logger log = Logger.getLogger(LuceneBackedResourceDAO.class);
    
    protected String indexPath;
    private IndexReader reader;
    LuceneAnalyzer analyzer;
    DateFormatter dateFormatter;
    
    public LuceneBackedResourceDAO(SessionFactory sessionFactory, String indexPath) throws CorruptIndexException, LockObtainFailedException, IOException {
        super(sessionFactory);
        this.indexPath = indexPath;       
        analyzer = new LuceneAnalyzer();
        dateFormatter = new DateFormatter();
    }


    public String getIndexPath() {
        return indexPath;
    }

    
    public LuceneBackedResourceDAO() {
    }
    
    
    
    
    public List<Resource> getNewsitemsMatchingKeywords(String keywords, boolean showBroken) {
        log.debug("Searching for Newsitems matching: " + keywords);
        return getTypedItemsMatchingKeywords(keywords, showBroken, "N");        
    }

    
    
    public List<Resource> getWebsitesMatchingKeywords(String keywords, boolean showBroken) {
        log.debug("Searching for Wesites matching: " + keywords);
        return getTypedItemsMatchingKeywords(keywords, showBroken, "W");    
    }

    
    
    private List<Resource> getTypedItemsMatchingKeywords(String keywords, boolean showBroken, final String type) {
        List <Resource> matchingItems = new ArrayList<Resource>();
        
        // Compose a lucene query.        
        BooleanQuery query = new BooleanQuery();
        
        if (!showBroken) {
            addHttpStatusRestriction(query);            
        }
        
        // Keyword restriction
        if (keywords != null) {
            try {
				addKeywordRestriction(query, keywords, analyzer);
				addTypeRestriction(query, type);

				Searcher searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
				Sort sort = dateDescendingSort();
            
				log.info("Lucene keyword query is: " + query.toString());
				Hits hits = searcher.search(query, sort);
				matchingItems = loadResourcesFromHits(100, hits);            
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();			
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
        }
        
        List<Resource> decoratedMatchingItems = new ArrayList<Resource>();
        for (Resource resource : matchingItems) {
            if (resource.getType().equals("W")) {
                decoratedMatchingItems.add(new KeywordHighlightingWebsiteDecorator( (Website) resource, query, analyzer));
            } else if (resource.getType().equals("N")) {
                decoratedMatchingItems.add(new KeywordHighlightingNewsitemDecorator( (Newsitem) resource, query, analyzer));                
            } else {
                decoratedMatchingItems.add(resource);
            }
        }
        return decoratedMatchingItems;
    
    }
    
    

    
    

    
    
    
    
    
    
    
    


    private BooleanQuery makeKeywordQuery(String keywords, LuceneAnalyzer analyzer) throws ParseException {
        BooleanQuery keywordQuery = new BooleanQuery();
        
        QueryParser nameParser = new QueryParser("name", analyzer);
        Query queryName = nameParser.parse(keywords);
        
        QueryParser descriptionParser = new QueryParser("description", analyzer);        
        Query queryDescription = descriptionParser.parse(keywords);
        
        QueryParser commentsParser = new QueryParser("comment", analyzer);        
        Query queryComments = commentsParser.parse(keywords);
        
        keywordQuery.add(queryName, Occur.SHOULD);
        keywordQuery.add(queryDescription, Occur.SHOULD);
        keywordQuery.add(queryComments, Occur.SHOULD);  
        return keywordQuery;
    }
    
    
    
    
  
   
    
    
    
    @Override
    public void saveResource(Resource resource) {
        super.saveResource(resource);        
        // Then update the lucene index with a partial update.
        try {
            updateResource(resource);
        } catch (IOException e) {
           log.error(e);
        }
    }
    
    
    
    public void saveTag(Tag tag) {
        super.saveTag(tag);
        // Then try to update the lucene index.
        try {
            updateTag(tag);            
        } catch (IOException e) {
            log.error(e);
        }
    }
    


    

    @Override
    public void deleteResource(Resource resource) {        
        super.deleteResource(resource);
        try {
			deleteLuceneResource(resource);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
    }

    
    public void deleteTag(Tag tag) {        
        try {
			deleteLuceneTag(tag);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        super.deleteTag(tag);         
    }
    
    
    private BooleanQuery makeTagNewsitemsQuery(Tag tag, boolean showBroken) {
    	BooleanQuery query = new BooleanQuery();        
        if (!showBroken) {
            addHttpStatusRestriction(query);            
        }                
        addTagRestriction(query, tag);        
        addTypeRestriction(query, "N");        
        return query;
    }

    
    public List<Resource> getTaggedNewitems(Tag tag, boolean showBroken, int max_items) {        
        log.debug("Searching for newsitems tagged: " + tag.getName());
                   
        BooleanQuery query = makeTagNewsitemsQuery(tag, showBroken);
 
        Searcher searcher;
		try {
			searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
			Sort sort = dateDescendingSort();
			Hits hits = searcher.search(query, sort);
			log.debug("Found " + hits.length() + " matching.");
                
			return loadResourcesFromHits(max_items, hits);                
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<Resource>();
    }
    
    
    
    
    public List<Resource> getPublisherTagCombinerNewsitems(Website publisher, Tag tag, boolean showBroken) {         
    	BooleanQuery query = makeTagNewsitemsQuery(tag, showBroken);
        addPublisherRestriction(query, publisher);
    	    	
        Sort sort = dateDescendingSort();
        Searcher searcher;
		try {
			searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
        
			log.debug("Query: " + query.toString());
			Hits hits = searcher.search(query, sort);
			log.debug("Found " + hits.length() + " matching.");
                
			return loadResourcesFromHits(500, hits);      	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<Resource>();
    }
    
    
    
    @Override
    public List<Resource> getCommentedNewsitemsForTag(Tag tag, boolean showBroken, int maxItems) {          
        // TODO massive duplication with the above method.
        log.debug("Searching for Newsitems tagged: " + tag.getName());
        
        // Compose a lucene query.        
        BooleanQuery query = new BooleanQuery();
        
        if (!showBroken) {
            addHttpStatusRestriction(query);            
        }                
        addTagRestriction(query, tag);        
        addTypeRestriction(query, "N");
        
        addCommentRestriction(query);
        
        
        Sort sort = dateDescendingSort();
		try {
			Searcher searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
        
			log.debug("Query: " + query.toString());
			Hits hits = searcher.search(query, sort);
			log.debug("Found " + hits.length() + " matching.");
        
			return loadResourcesFromHits(maxItems, hits);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Empty Collections instead?
		return new ArrayList<Resource>();
    }
    
    
    public Date getLastLiveTimeForTag(Tag tag) {
        BooleanQuery query = new BooleanQuery();
        addTagRestriction(query, tag);      
        
        Sort sort = lastLiveDescendingSort();
        Searcher searcher;
		try {
			searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
        
	        log.debug("Query: " + query.toString());
	        Hits hits = searcher.search(query, sort);
	        log.debug("Found " + hits.length() + " matching.");
	        
	        if (hits.length() > 0) {
	            final String lastlive = hits.doc(0).get("last_live");
	            if (lastlive != null) {                       	
	                DateTime latestLiveTime = new DateTime(new Long(lastlive).longValue());        
					log.info("Latest live time for tag " + tag.getName() + " was: " + latestLiveTime.toString());                
	                return latestLiveTime.toDate();
	            }
	        }
        } catch (IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }                        
        return null;        
    }
    
    
    public int getTaggedNewitemsCount(Tag tag, boolean showBroken) {        
        log.debug("Searching for Newsitems tagged: " + tag.getName());
        
        // Compose a lucene query.        
        BooleanQuery query = new BooleanQuery();
        
        if (!showBroken) {
            addHttpStatusRestriction(query);            
        }                
        addTagRestriction(query, tag);        
        addTypeRestriction(query, "N");
        
        Sort sort = dateDescendingSort();
        Searcher searcher;
		try {
			searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
			log.debug("Query: " + query.toString());
			Hits hits = searcher.search(query, sort);
			log.debug("Found " + hits.length() + " matching.");
			return hits.length();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
    }
    
    
    private List <Resource> loadResourcesFromHits(int number, Hits hits) throws IOException {
        List<Resource> matchingNewsitems = new ArrayList<Resource>();
        for (int i = 0; i < hits.length() && i < number; i++) {
            int loadID = new Integer(hits.doc(i).get("id")).intValue();
            Resource loadedResource = this.loadResourceById(loadID);
            if (loadedResource != null) {
                matchingNewsitems.add(loadedResource);
            } else {
                log.warn("Loaded a null resource; lucene index could lag behind the database.");
            }
        }
        return matchingNewsitems;
    }
    
    
   
    


    private void addHttpStatusRestriction(BooleanQuery query) {
        BooleanQuery queryBroken = new BooleanQuery();
        queryBroken.add(new TermQuery(new Term("http_status", "200")), Occur.SHOULD);
        query.add(queryBroken, Occur.MUST);
    }

    
    private void addCommentRestriction(BooleanQuery query) {   
        // TODO is this really how you do an int range?
        RangeQuery queryBroken = new RangeQuery(new Term("comment_count", "1"), new Term("comment_count", "999"), true);
        query.add(queryBroken, Occur.MUST);
    }
    
    

    private Sort dateDescendingSort() {
        SortField date = new SortField("date", true);
        SortField id = new SortField("id", true);
        SortField[] sortFields = { date, id };
        Sort sort = new Sort(sortFields);
        return sort;
    }
    
    
    private Sort lastLiveDescendingSort() {
        SortField date = new SortField("last_live", true);        
        SortField[] sortFields = { date };
        Sort sort = new Sort(sortFields);
        return sort;
    }
    
   
    private void addKeywordRestriction(BooleanQuery query, String keywords, LuceneAnalyzer analyzer) throws ParseException {
        if (keywords != null) {          
            BooleanQuery keywordQuery = makeKeywordQuery(keywords, analyzer);
            query.add(keywordQuery, Occur.MUST);
        }
    }

    private void addTypeRestriction(BooleanQuery query, String type) { 
        query.add(new TermQuery(new Term("type", type)), Occur.MUST);
    }

    
    private void addTagRestriction(BooleanQuery query, Tag tag) {    
        if (tag != null) {               
            query.add(new TermQuery(new Term("tag_id", new Integer(tag.getId()).toString())), Occur.MUST);            
        }
    }
    
    
    private void addTagsRestriction(BooleanQuery query, Set<Tag> tags) {
        for (Tag tag : tags) {
            log.debug("Adding tag restriction for tag: " + tag.getName());
            addTagRestriction(query, tag);
        }
    }
    
    private void addPublisherRestriction(BooleanQuery query, Website publisher) {
    	  query.add(new TermQuery(new Term("publisher", new Integer(publisher.getId()).toString())), Occur.MUST);      
    }
    
    
    
    
  
    public List<Resource> getCalendarFeedsForTag(Tag tag) {
    	List <Resource> matchingWebsites = new ArrayList<Resource>();        
        log.info("Searching for calendars for tag: " + tag.getName());
        
        boolean showBroken = false;     // TODO pull up onto method.
        
        // Compose a lucene query.        
        BooleanQuery query = new BooleanQuery();                
        if (!showBroken) {
            addHttpStatusRestriction(query);            
        }
        addTagRestriction(query, tag);               
        addTypeRestriction(query, "C");

        Searcher searcher;
		try {
			searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
			Sort sort = dateDescendingSort();
        
			log.debug("Query: " + query.toString());
			Hits hits = searcher.search(query, sort);
			log.debug("Found " + hits.length() + " matching.");
			matchingWebsites = loadResourcesFromHits(10, hits);                     
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        return matchingWebsites;        
    }


    public List<Resource> getTaggedNewsitems(Set<Tag> tags, boolean showBroken, int max_websites) {
        List <Resource> matchingNewsitems = new ArrayList<Resource>();
        
        // Compose a lucene query.        
        BooleanQuery query = new BooleanQuery();                
        if (!showBroken) {
            addHttpStatusRestriction(query);            
        }
        addTagsRestriction(query, tags);               
        addTypeRestriction(query, "N");

		try {
			Searcher searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
			Sort sort = dateDescendingSort();
        
			log.debug("Query: " + query.toString());
			Hits hits = searcher.search(query, sort);
			log.debug("Found " + hits.length() + " matching.");
			matchingNewsitems = loadResourcesFromHits(max_websites, hits);
                     
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
        return matchingNewsitems;
    }
    
     
	public List<Resource> getAllValidGeocodedForTag(Tag tag, int maxItems, boolean showBroken) {
    	  List <Resource> matchingNewsitems = new ArrayList<Resource>();
    	  
          BooleanQuery query = new BooleanQuery();                
          if (!showBroken) {
              addHttpStatusRestriction(query);            
          }
          
          Set<Tag> tags = new HashSet<Tag>();
          tags.add(tag);
          addTagsRestriction(query, tags);               
          addTypeRestriction(query, "N");
          query.add(new TermQuery(new Term("geotagged", "1")), Occur.MUST);

          try {
        	  Searcher searcher;
        	  searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
        	  Sort sort = dateDescendingSort();
          
        	  log.debug("Query: " + query.toString());
        	  Hits hits = searcher.search(query, sort);
        	  log.debug("Found " + hits.length() + " matching.");
        	  matchingNewsitems = loadResourcesFromHits(maxItems, hits);
          } catch (IOException e) {
        	  log.error("IO exception; returning empty list:", e);        	 
          }                       
          return matchingNewsitems;
	}


	private IndexReader loadIndexReader(String indexPath, boolean createNew) throws IOException {
        IndexReader localReader = null;
        if (reader == null || !reader.isCurrent() ) {
            localReader = IndexReader.open(indexPath);
            reader = localReader;
        } else {
            if (createNew == false) {
                localReader = reader;
               
            } else {
                localReader = IndexReader.open(indexPath);
            }
        }
        return localReader;
    }
    
    
    
    
  
    public void buildIndex() throws IOException {
        // Use our our Analyzer so that we can make use of stemming.
        Analyzer analyzer = new LuceneAnalyzer();

        // A new index is created by opening an IndexWriter with the create argument set to true.
        IndexWriter createWriter = new IndexWriter(indexPath, analyzer, true);

 
        Set <Integer> newsitemIdsToIndex = getAllResourceIds();                
        log.info("Number of newsitems to update in lucene index: " + newsitemIdsToIndex.size());
        for (Integer id : newsitemIdsToIndex) {
            Resource resource = loadResourceById(id);
            log.info("Adding lucene record: " + resource.getId() + " - " + resource.getName() + " - " + resource.getType());
            writeResourceToIndex(resource, createWriter);
        }
    
        
                 
        for (Tag tag : this.getAllTags()) {
            log.debug("Adding lucene tag record: " + tag.getId() + " - " + tag.getName());
            writeTagToIndex(tag, createWriter);            
        }
        
        log.debug("Added " + createWriter.docCount() + " items to the lucene index.");
        createWriter.close();
    }

    
    
    private void writeResourceToIndex(Resource resource, IndexWriter writer) throws IOException {
        Document doc = indexResource(resource);      
        writer.addDocument(doc);
    }
    
    private void writeTagToIndex(Tag tag, IndexWriter writer) throws IOException {
        Document doc = indexTag(tag);      
        writer.addDocument(doc);
    }


    
    
    private void deleteLuceneResource(Resource resource) throws IOException {
        // Use an IndexReader to delete the current records (so we can update them).      
        Term deleteTerm = new Term("id", Integer.toString(resource.getId()));
        IndexWriter updater = new IndexWriter(indexPath, analyzer, false);
        updater.deleteDocuments(deleteTerm);
        updater.flush();
        updater.close();
    }
    
    
    
    private void deleteLuceneTag(Tag tag) throws IOException {
        // Use an IndexReader to delete the current records (so we can update them).      
        Term deleteTerm = new Term("id", "TAG:" + Integer.toString(tag.getId()));                
        IndexWriter updater = new IndexWriter(indexPath, analyzer, false);
        updater.deleteDocuments(deleteTerm);
        updater.flush();
        updater.close();
    }

    
    private void updateResource(Resource resource) throws IOException {
        // Update an existing resource in the lucene index by deleting and reinserting it.
        log.debug("updateResource, updating lucene record: " + resource.getId() + " - " + resource.getName() + " - " + resource.getType());

        deleteLuceneResource(resource);

        IndexWriter updater = new IndexWriter(indexPath, analyzer, false);
        writeResourceToIndex(resource, updater);
        updater.flush();
        updater.close();
    }
    
    
    private void updateTag(Tag tag) throws IOException {
        log.debug("updateTag, updating lucene record: " + tag.getId() + " - " + tag.getDisplayName());
        deleteLuceneTag(tag);
     
        
        IndexWriter updater = new IndexWriter(indexPath, analyzer, false);
        writeTagToIndex(tag, updater);
        updater.flush();
        updater.close();
    }

    

    
    

    private Document indexTag(Tag tag) {
        Document doc = new Document();
        doc.add(new Field("id", "TAG:" + Integer.toString(tag.getId()), Field.Store.YES, Field.Index.UN_TOKENIZED));   
        doc.add(new Field("type", "T", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("name", tag.getDisplayName(), Field.Store.YES, Field.Index.TOKENIZED));
        return doc;
    }
    

    private Document indexResource(Resource resource) {
    
        Document doc = new Document();
        doc.add(new Field("id", Integer.toString(resource.getId()), Field.Store.YES, Field.Index.UN_TOKENIZED));   
        doc.add(new Field("type", resource.getType(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("http_status", Integer.toString(resource.getHttpStatus()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        
        doc.add(new Field("name_sort", resource.getName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("name", resource.getName(), Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("description", resource.getDescription(), Field.Store.YES, Field.Index.TOKENIZED));            
                    
        if (resource.getDate() != null) {           
            doc.add(new Field("date", dateFormatter.formatDate(resource.getDate(), "yyyy-MM-dd"), Field.Store.YES, Field.Index.UN_TOKENIZED));
        }
        
        if (resource.getLiveTime() != null) {
            doc.add(new Field("last_live", Long.toString(resource.getLiveTime().getTime()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        }
        
        addResourceTags(resource, doc);
        
        if (resource.getType().equals("N")) {
            Newsitem newsitem = (Newsitem) resource;
            int commentCount = 0;
            if (newsitem.getCommentFeed() != null) {
                commentCount = newsitem.getCommentFeed().getComments().size();
            }
            
            if (newsitem.getPublisher() != null) {
            	doc.add(new Field("publisher", Integer.toString(newsitem.getPublisher().getId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
            }
            
            doc.add(new Field("comment_count", Integer.toString(commentCount), Field.Store.YES, Field.Index.UN_TOKENIZED));            
            if (newsitem.getCommentFeed() != null) {
                for (Comment comment : newsitem.getCommentFeed().getComments()) {
                    doc.add(new Field("comment", comment.getTitle(), Field.Store.YES, Field.Index.TOKENIZED));                
                }
            }
        }
        
        if (resource.getGeocode() != null && resource.getGeocode().isValid()) {
            doc.add(new Field("geotagged", "1", Field.Store.YES, Field.Index.UN_TOKENIZED));
        }        
        return doc;
    }


    private void addResourceTags(Resource resource, Document doc) {
        Set <Tag> resourceTags = new HashSet<Tag>();
        
        
        // TODO this null check should not be needed; check logs then remove.
        Set <Tag> tags = resource.getTags();
        if (tags != null) {
            resourceTags.addAll(tags);
        } else {
            log.warn("Resource has null tag set: " + resource.getName());
        }
        
        
        final boolean shouldAppearOnPublisherAndParentTagtPages = 
            resource.getType().equals("L") || resource.getType().equals("N")
            || resource.getType().equals("C");
        
        // TODO is the watchlist one uses; ie. is that method still implemented in hibernate?
        
        if (shouldAppearOnPublisherAndParentTagtPages) {            
            Set <Tag> existingTags = new HashSet<Tag>(resourceTags);
            for (Tag tag : existingTags) {                
                resourceTags.addAll(tag.getAncestors());
            }
            
            if (((PublishedResource) resource).getPublisher() != null) {              
                for (Tag publisherTag : ((PublishedResource) resource).getPublisher().getTags()) {                
                    log.debug("Adding publisher tag " + publisherTag.getName() + " to record.");
                    resourceTags.add(publisherTag);
                    resourceTags.addAll(publisherTag.getAncestors());
                }
            }
        }
        
        for (Tag tag : resourceTags) {
            log.debug("Adding tag " + tag.getName() + " to record.");
            doc.add(new Field("tag_id", Integer.toString(tag.getId()), Field.Store.YES, Field.Index.UN_TOKENIZED));            
        }
    }

    
    
   
    
  
    
    

    
    
    @Override
    public List<Tag> getTagsMatchingKeywords(String keywords) {
        log.debug("Searching for Tags matching: " + keywords);
        List <Tag> matchingTags = new ArrayList<Tag>();
        
        LuceneAnalyzer analyzer = new LuceneAnalyzer();
        
        // Compose a lucene query.        
        BooleanQuery query = new BooleanQuery();
                      
        addTypeRestriction(query, "T");
        try {
			addKeywordRestriction(query, keywords, analyzer);
        
			Searcher searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
			Sort sort = dateDescendingSort();
                
			Hits hits = searcher.search(query, sort);
        
			for (int i = 0; i < hits.length(); i++) {
				String luceneTagId = hits.doc(i).get("id");
				log.info("Raw lucene tag id is: " + luceneTagId);
				String id = luceneTagId.split(":")[1];
				log.info("Parser tag id is: " + id);
				int loadID = new Integer(id).intValue();
				Tag loadedTag = this.loadTagById(loadID);
				matchingTags.add(loadedTag);
			}
        } catch (ParseException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
              
        return matchingTags;   
    }
   
    
}
