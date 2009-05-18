package nz.co.searchwellington.repositories;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;

public class LuceneIndexRebuildService {

	Logger log = Logger.getLogger(LuceneIndexRebuildService.class);

	private ResourceRepository resourceDAO;
	private LuceneIndexUpdateService luceneIndexUpdateService;
	private String indexPath;
	Analyzer analyzer;
	

	public LuceneIndexRebuildService(ResourceRepository resourceDAO, LuceneIndexUpdateService luceneIndexUpdateService, String indexPath) {		
		this.resourceDAO = resourceDAO;
		this.luceneIndexUpdateService = luceneIndexUpdateService;
		this.indexPath = indexPath;
		analyzer = new LuceneAnalyzer();		
	}
	
}
