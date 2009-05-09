package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;

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


	public void buildIndex() {		
		// A new index is created by opening an IndexWriter with the create argument set to true.
		try {
		IndexWriter createWriter = new IndexWriter(indexPath, analyzer, true);
			Set<Integer> newsitemIdsToIndex = resourceDAO.getAllResourceIds();
			log.info("Number of newsitems to update in lucene index: " + newsitemIdsToIndex.size());
			for (Integer id : newsitemIdsToIndex) {
				Resource resource = resourceDAO.loadResourceById(id);
				log.info("Adding lucene record: " + resource.getId() + " - " + resource.getName() + " - " + resource.getType());
				luceneIndexUpdateService.writeResourceToIndex(resource, createWriter);
			}

			for (Tag tag : resourceDAO.getAllTags()) {
				log.debug("Adding lucene tag record: " + tag.getId() + " - " + tag.getName());
				luceneIndexUpdateService.writeTagToIndex(tag, createWriter);
			}
		
			log.debug("Added " + createWriter.docCount() + " items to the lucene index.");
			createWriter.close();
				
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
