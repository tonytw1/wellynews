package nz.co.searchwellington.model;

public enum FeedAcceptancePolicy {
        
    ACCEPT("Accept", true),
    IGNORE("Ignore", false),
    SUGGEST("Suggest", false),
    ACCEPT_EVEN_WITHOUT_DATES("Accept even without dates.", true),
    ACCEPT_IGNORING_DATE("Accept ignoring date.", true);

    private final String label;
    private final boolean shouldAcceptFeedItems;
    
    FeedAcceptancePolicy(String label, boolean shouldAcceptFeedItems) {
        this.label = label;
		this.shouldAcceptFeedItems = shouldAcceptFeedItems;
    }
    
    public String getLabel() {
        return label;
    }
    
    public boolean shouldAcceptFeedItems() {
    	return shouldAcceptFeedItems;
    }
    
}
