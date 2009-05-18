package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

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
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.LockObtainFailedException;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;




public abstract class LuceneBackedResourceDAO extends HibernateResourceDAO implements ResourceRepository {

    Logger log = Logger.getLogger(LuceneBackedResourceDAO.class);
    
    protected String indexPath;
    private IndexReader reader;
    LuceneAnalyzer analyzer;   
	private LuceneIndexUpdateService luceneIndexUpdateService;
    
    public LuceneBackedResourceDAO(SessionFactory sessionFactory, String indexPath, LuceneIndexUpdateService luceneIndexUpdateService) throws CorruptIndexException, LockObtainFailedException, IOException {
        super(sessionFactory);
        this.indexPath = indexPath;       
        this.luceneIndexUpdateService = luceneIndexUpdateService;
        analyzer = new LuceneAnalyzer();        
    }
    
    
	public String getIndexPath() {
        return indexPath;
    }

    
    public LuceneBackedResourceDAO() {
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
    
    
    public void saveTag(Tag tag) {
        super.saveTag(tag);
        luceneIndexUpdateService.updateTag(tag);     
    }
       
    
    public void deleteTag(Tag tag) {     
        luceneIndexUpdateService.deleteTag(tag);	
        super.deleteTag(tag);         
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
