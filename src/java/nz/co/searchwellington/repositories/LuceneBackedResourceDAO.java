package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    
    
    private Sort dateDescendingSort() {
        SortField date = new SortField("date", true);
        SortField id = new SortField("id", true);
        SortField[] sortFields = { date, id };
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
