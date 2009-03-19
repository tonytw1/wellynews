package nz.co.searchwellington.model;

import java.util.Set;
import java.util.HashSet;

public class Tag {
    
    int id;
    String name;
    String displayName;
    Tag parent;
    Set <Tag> children;
    int flickrCount;
    
    String mainImage;
    String secondaryImage;
   
    Feed relatedFeed;
    String relatedTwitter;
    private String autotagHints;
        
    public Tag() {        
    }
    
    
    public Tag(int id, String name, String displayName, Tag parent, Set <Tag> children, int flickrCount) {        
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.parent = parent;
        this.children = children;
        this.relatedFeed = null;
    }
    
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Tag getParent() {
        return parent;
    }

    public void setParent(Tag parent) {
        this.parent = parent;
    }
    
    public void setChildren(Set<Tag> children) {
        this.children = children;
    }

    public Set<Tag> getChildren() {
        return children;
    }
    
    public void addChild(Tag tag) {
        children.add(tag);        
    }
    
    public Set<Tag> getAncestors() {
        HashSet<Tag> ancestors = new HashSet<Tag>();   
        Tag parent = getParent();
        while (parent != null) {
            ancestors.add(parent);
            parent = parent.getParent();
        }
        return ancestors;
    }

    public int getFlickrCount() {
        return flickrCount;
    }

    public void setFlickrCount(int flickrCount) {
        this.flickrCount = flickrCount;
    }
        
    // TODO only used in tag tree vm. Can come off the interface.
    public boolean isParentOf(Tag tag) {        
        return tag.getAncestors().contains(this);            
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }
  
    public String getSecondaryImage() {
        return secondaryImage;
    }
        
    public void setSecondaryImage(String secondaryImage) {
        this.secondaryImage = secondaryImage;
    }

    public Feed getRelatedFeed() {
        return this.relatedFeed;
    }

    public void setRelatedFeed(Feed relatedFeed) {
        this.relatedFeed = relatedFeed;
    }

    @Override
    public String toString() {
        return "Tag: " + name;
    }


	public String getRelatedTwitter() {
		return relatedTwitter;
	}

	public void setRelatedTwitter(String relatedTwitter) {
		this.relatedTwitter = relatedTwitter;
	}


	public void setAutotagHints(String autotagHints) {
		this.autotagHints = autotagHints;
	}


	public String getAutotagHints() {
		return autotagHints;
	}
        
}
