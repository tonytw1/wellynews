package nz.co.searchwellington.model;

import java.util.Set;

public interface Tag {

    public int getId();

    public void setId(int id);

    public String getName();
    public void setName(String name);

    public Tag getParent();

    public Set<Tag> getChildren();
    
    public void addChild(Tag tag);

    public void setParent(Tag parent);

    public void setChildren(Set<Tag> children);

    public Set<Tag> getAncestors();
    
    // TODO Should this reall ybe methods on the Tag or DAO methods?
    public void setFlickrCount(int tagPhotoCount);
    public int getFlickrCount();

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public boolean isParentOf(Tag tag);

    public void setMainImage(String string);

    public void setSecondaryImage(String string);

    public String getMainImage();

    public String getSecondaryImage();

    public Set<Resource> getTaggedResources();

    public void setTaggedResources(Set<Resource> taggedResources);

    public Feed getRelatedFeed();

    public void setRelatedFeed(Feed relatedFeed);

    
   
}
