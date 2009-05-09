package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.decoraters.highlighting.KeywordHighlightingNewsitemDecorator;
import nz.co.searchwellington.model.decoraters.highlighting.KeywordHighlightingWebsiteDecorator;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
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
	private LuceneIndexUpdateService luceneIndexUpdateService;
    
    public LuceneBackedResourceDAO(SessionFactory sessionFactory, String indexPath, LuceneIndexUpdateService luceneIndexUpdateService) throws CorruptIndexException, LockObtainFailedException, IOException {
        super(sessionFactory);
        this.indexPath = indexPath;       
        analyzer = new LuceneAnalyzer();        
        this.luceneIndexUpdateService = luceneIndexUpdateService;
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
        luceneIndexUpdateService.updateResource(resource);       
    }
    
        
    public void saveTag(Tag tag) {
        super.saveTag(tag);
        luceneIndexUpdateService.updateTag(tag);     
    }
       
    
    @Override
    public void deleteResource(Resource resource) {        
        super.deleteResource(resource);
        luceneIndexUpdateService.deleteLuceneResource(resource);		  
    }

    
    public void deleteTag(Tag tag) {     
        luceneIndexUpdateService.deleteTag(tag);	
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

    
    public List<Resource> getTaggedNewitems(Tag tag, boolean showBroken, int startIndex, int maxItems) {        
        log.debug("Searching for newsitems tagged: " + tag.getName());
                   
        BooleanQuery query = makeTagNewsitemsQuery(tag, showBroken);        
		try {
			Searcher searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
			Sort sort = dateDescendingSort();
			Hits hits = searcher.search(query, sort);
			log.info("Found " + hits.length() + " matching.");                
			return loadResourcesFromHits(maxItems, startIndex, hits);
			
		} catch (IOException e) {
			log.error("IOException while getting tagged newsitems; return empty list", e);
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
    	return loadResourcesFromHits(number, 0, hits);
    }
    
    
    private List <Resource> loadResourcesFromHits(int numberOfItems, int startIndex, Hits hits) throws IOException {
        List<Resource> matchingNewsitems = new ArrayList<Resource>();        
        int endIndex = numberOfItems + startIndex;
		for (int i = startIndex; i < hits.length() && i < endIndex; i++) {
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
    
    
    public List<Resource> getTaggedFeeds(Tag tag, boolean showBroken) {
    	  List <Resource> matchingFeeds = new ArrayList<Resource>();
          
    	   BooleanQuery query = new BooleanQuery();                
           if (!showBroken) {
               addHttpStatusRestriction(query);            
           }           
           addTagRestriction(query, tag);
           addTypeRestriction(query, "F");

   		try {
   			Searcher searcher = new IndexSearcher(loadIndexReader(this.indexPath, false));
   			Sort sort = dateDescendingSort();
           
   			log.debug("Query: " + query.toString());
   			Hits hits = searcher.search(query, sort);
   			log.debug("Found " + hits.length() + " matching.");
   			matchingFeeds = loadResourcesFromHits(30, hits);
   			
   		} catch (IOException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		} 
   		return matchingFeeds;    	
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
