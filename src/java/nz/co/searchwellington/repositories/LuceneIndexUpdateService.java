package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.LockObtainFailedException;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.PublishedResource;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

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

	public void updateResource(Resource resource) {
        // Update an existing resource in the lucene index by deleting and reinserting it.
        log.debug("updateResource, updating lucene record: " + resource.getId() + " - " + resource.getName() + " - " + resource.getType());        
        deleteLuceneResource(resource);
        
		try {
			IndexWriter updater = new IndexWriter(indexPath, analyzer, false);
			writeResourceToIndex(resource, updater);
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

	
	public void deleteLuceneResource(Resource resource) {
        // Use an IndexReader to delete the current records (so we can update them).      
        Term deleteTerm = new Term("id", Integer.toString(resource.getId()));
        
        Analyzer analyzer = new LuceneAnalyzer();
		try {
			IndexWriter updater = new IndexWriter(indexPath, analyzer, false);
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
	
	
	public void writeResourceToIndex(Resource resource, IndexWriter writer) throws IOException {
		 Document doc = indexResource(resource);      
		 writer.addDocument(doc);
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
	        Set <Tag> indexTags = getIndexTagsForResource(resource);	        
	        for (Tag tag : indexTags) {
	            log.debug("Adding tag " + tag.getName() + " to record.");
	            doc.add(new Field("tag_id", Integer.toString(tag.getId()), Field.Store.YES, Field.Index.UN_TOKENIZED));            
	        }
	    }

	 
	private Set<Tag> getIndexTagsForResource(Resource resource) {	
		Set <Tag> indexTags = new HashSet<Tag>();
		indexTags.addAll(resource.getTags());
		
		final boolean shouldAppearOnPublisherAndParentTagPages = 
		    resource.getType().equals("L") || resource.getType().equals("N")
		    || resource.getType().equals("C") || resource.getType().equals("F");
				
		if (shouldAppearOnPublisherAndParentTagPages) {            
		    Set <Tag> existingTags = new HashSet<Tag>(indexTags);
		    for (Tag tag : existingTags) {
		        indexTags.addAll(tag.getAncestors());
		    }
		    
		    if (((PublishedResource) resource).getPublisher() != null) {              
		        for (Tag publisherTag : ((PublishedResource) resource).getPublisher().getTags()) {                
		            log.debug("Adding publisher tag " + publisherTag.getName() + " to record.");
		            indexTags.add(publisherTag);
		            indexTags.addAll(publisherTag.getAncestors());
		        }
		    }
		}
		
		return indexTags;
	}
	 	 
}
