package nz.co.searchwellington.model;

public class TagContentCount {
    
    private Tag tag;
    private long count;
    
        
    public TagContentCount(Tag tag, long count) {
        this.tag = tag;
        this.count = count;
    }
    
    public long getCount() {
        return count;
    }
    public void setCount(long count) {
        this.count = count;
    }
    public Tag getTag() {
        return tag;
    }
    public void setTag(Tag tag) {
        this.tag = tag;
    }
    
}
