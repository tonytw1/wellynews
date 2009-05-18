package nz.co.searchwellington.repositories;

import java.io.IOException;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Tag;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.LockObtainFailedException;

public class LuceneIndexUpdateService {
	
	Logger log = Logger.getLogger(LuceneIndexUpdateService.class);
	
	private DateFormatter dateFormatter;	
	private String indexPath;
	private Analyzer analyzer;
	
				
	public LuceneIndexUpdateService(DateFormatter dateFormatter, String indexPath) {	
		this.dateFormatter = dateFormatter;
		this.indexPath = indexPath;
		analyzer = new LuceneAnalyzer();
	}

	
	
	public void updateTag(Tag tag) {
        log.debug("updateTag, updating lucene record: " + tag.getId() + " - " + tag.getDisplayName());
        deleteTag(tag);
        
        try {
        	IndexWriter updater = new IndexWriter(indexPath, analyzer, false);
			writeTagToIndex(tag, updater);
			updater.flush();
			updater.close();
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
	
		
	public void deleteTag(Tag tag) {
		// Use an IndexReader to delete the current records (so we can update them).      
		Term deleteTerm = new Term("id", "TAG:" + Integer.toString(tag.getId()));                
		IndexWriter updater;
		try {
			updater = new IndexWriter(indexPath, analyzer, false);
			updater.deleteDocuments(deleteTerm);
			updater.flush();
			updater.close();
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

		

	 
	public void writeTagToIndex(Tag tag, IndexWriter writer) throws IOException {
		Document doc = indexTag(tag);      
		 writer.addDocument(doc);
	}
	 
	 

	    private Document indexTag(Tag tag) {
	        Document doc = new Document();
	        doc.add(new Field("id", "TAG:" + Integer.toString(tag.getId()), Field.Store.YES, Field.Index.UN_TOKENIZED));   
	        doc.add(new Field("type", "T", Field.Store.YES, Field.Index.UN_TOKENIZED));
	        doc.add(new Field("name", tag.getDisplayName(), Field.Store.YES, Field.Index.TOKENIZED));
	        return doc;
	    }
	 
	
	 
	 
	 
	 	 
}
