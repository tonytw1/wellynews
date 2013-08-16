package nz.co.searchwellington.model;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parent"})
public class Tag {
    
	private int id;
	private String name;
	private String displayName;
	private String description;
	private Tag parent;
	private Set <Tag> children;
	private boolean hidden;
	private boolean featured;
    
	private String mainImage;
	private String secondaryImage;
	
	private Feed relatedFeed;
	
	@Deprecated
	private String relatedTwitter;
	
    private String autotagHints;
    private Geocode geocode;
            
    public Tag() {        
    }
        
    public Tag(int id, String name, String displayName, Tag parent, Set <Tag> children, int flickrCount, boolean hidden, boolean featured) {  
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.parent = parent;
        this.children = children;
        this.hidden = hidden;
        this.relatedFeed = null;
        this.featured = featured;
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

    @JsonIgnore
    public Tag getParent() {
        return parent;
    }

    public void setParent(Tag parent) {
        this.parent = parent;
    }
    
    public void setChildren(Set<Tag> children) {
        this.children = children;
    }
    
    @JsonIgnore
    public Set<Tag> getChildren() {
        return children;
    }
    
    public void addChild(Tag tag) {
        children.add(tag);        
    }
    
    @JsonIgnore
    public Set<Tag> getAncestors() {
        HashSet<Tag> ancestors = new HashSet<Tag>();   
        Tag parent = getParent();
        while (parent != null) {
            ancestors.add(parent);
            parent = parent.getParent();
        }
        return ancestors;
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

	 public boolean isHidden() {
		 return hidden;
	}
	 
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public Geocode getGeocode() {
		return geocode;
	}

	public void setGeocode(Geocode geocode) {
		this.geocode = geocode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isFeatured() {
		return featured;
	}

	public void setFeatured(boolean featured) {
		this.featured = featured;
	}
	
}
