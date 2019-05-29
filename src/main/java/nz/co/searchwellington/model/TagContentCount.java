package nz.co.searchwellington.model;

import nz.co.searchwellington.model.frontend.FrontendTag;

public class TagContentCount {
    
    private FrontendTag tag;
    private long count;
    
    public TagContentCount(FrontendTag tag, long count) {
        this.tag = tag;
        this.count = count;
    }
    
    public long getCount() {
        return count;
    }
    public void setCount(long count) {
        this.count = count;
    }
    public FrontendTag getTag() {
        return tag;
    }
    public void setTag(FrontendTag tag) {
        this.tag = tag;
    }

	@Override
	public String toString() {
		return "TagContentCount [count=" + count + ", tag=" + tag + "]";
	}
    
}
