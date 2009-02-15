package nz.co.searchwellington.model;

public class TagContentCount implements Comparable<TagContentCount> {
    
    private Tag tag;
    private int count;
    
        
    public TagContentCount(Tag tag, int count) {
        this.tag = tag;
        this.count = count;
    }
    
    
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public Tag getTag() {
        return tag;
    }
    public void setTag(Tag tag) {
        this.tag = tag;
    }
    
    
    public int compareTo(TagContentCount link) {
        return (new Integer(this.count).compareTo(link.count)) * -1;
    }

}
