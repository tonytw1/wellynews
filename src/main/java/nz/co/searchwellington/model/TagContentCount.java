package nz.co.searchwellington.model;

import nz.co.searchwellington.model.frontend.FrontendTag;

public class TagContentCount {
    
    private FrontendTag tag;
    private int count;
    
    public TagContentCount(FrontendTag tag, int count) {
        this.tag = tag;
        this.count = count;
    }
    
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
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
