package nz.co.searchwellington.model;

public enum FeedAcceptancePolicy {
        
    ACCEPT("accept", "Accept"),
    IGNORE("ignore", "Ignore"),
    SUGGEST("suggest", "Suggest"),
    ACCEPT_EVEN_WITHOUT_DATES("accept_without_dates", "Accept even without dates.");
    
    String name;
    String label;

    private FeedAcceptancePolicy(String name, String label) {
        this.name = name;
        this.label = label;
    }
    
    public String getName() {
        return name;
    }
    
    public String getLabel() {
        return label;
    }
    
}
