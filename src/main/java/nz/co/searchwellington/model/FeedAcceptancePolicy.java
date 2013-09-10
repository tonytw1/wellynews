package nz.co.searchwellington.model;

public enum FeedAcceptancePolicy {
        
    ACCEPT("Accept"),
    IGNORE("Ignore"),
    SUGGEST("Suggest"),
    ACCEPT_EVEN_WITHOUT_DATES("Accept even without dates.");
    
    private String label;

    private FeedAcceptancePolicy(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
    
}
