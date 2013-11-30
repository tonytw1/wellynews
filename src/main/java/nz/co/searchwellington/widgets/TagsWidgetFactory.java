package nz.co.searchwellington.widgets;

import java.util.Set;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagsWidgetFactory {
    
	// TODO remove duplication.
    private static Logger log = Logger.getLogger(TagsWidgetFactory.class);
    
    private TagDAO tagDAO;
    private HibernateResourceDAO resourceDAO;
   
    public TagsWidgetFactory() {
	}
    
    @Autowired
    public TagsWidgetFactory(TagDAO tagDAO, HibernateResourceDAO resourceDAO) {
		this.tagDAO = tagDAO;
		this.resourceDAO = resourceDAO;
	}
    
    public String createMultipleTagSelect(Set<Tag> selectedTags) {
        Select tagSelect= new Select("tags");
        tagSelect.setID("tags");
        tagSelect.setMultiple(true);
        
        log.debug("Selected tags are: " + selectedTags);
        for (Tag tag : selectedTags) {
            Option option = new Option(tag.getName());
            option.setFilterState(true);
            option.addElement(tag.getName());
            tagSelect.addElement(option);
        }
        return tagSelect.toString();
    }
    
    public Select createTagSelect(String name, Tag selectedTag, Set<Tag> tagsToExclude) {
        return createTagSelect(name, selectedTag, tagsToExclude, "No Parent");
    }
    
    public Select createRelatedFeedSelect(String name, Feed relatedFeed) {
        Select relatedFeedSelect = new Select(name);
        Option noFeedOption = new Option("0");
        noFeedOption.addElement("No related Feed");

        if (relatedFeed == null) {
            noFeedOption.setSelected(true);
        }
        relatedFeedSelect.addElement(noFeedOption);

        for (Feed feed : resourceDAO.getAllFeeds()) {
            Option option = new Option(Integer.toString(feed.getId()));
            option.setFilterState(true);
            option.addElement(feed.getName());
            if (relatedFeed != null && relatedFeed == feed) {
                option.setSelected(true);
            }
            relatedFeedSelect.addElement(option);
        }

        return relatedFeedSelect;
    }
        
    private Select createTagSelect(String name, Tag selectedTag, Set<Tag> tagsToExclude, String noneSelectedText) {       
        Select tagSelect= new Select(name);
        tagSelect.setMultiple(false);
        
        Option noParentOption = new Option("0");
        // TODO this should be an option.
        noParentOption.addElement(noneSelectedText);
        if (selectedTag == null) {
            noParentOption.setSelected(true);
        }
        tagSelect.addElement(noParentOption);
        
        for (Tag tag : tagDAO.getAllTags()) {
            final boolean tagIsNotExcluded = !tagsToExclude.contains(tag);
            if (tagIsNotExcluded) {
                Option option = new Option(tag.getName());
                option.setFilterState(true);
                option.addElement(tag.getDisplayName().toLowerCase());
                if (selectedTag != null && selectedTag == tag) {
                    option.setSelected(true);
                }            
                tagSelect.addElement(option);
            }
        }
        
        return tagSelect;
    }

}
