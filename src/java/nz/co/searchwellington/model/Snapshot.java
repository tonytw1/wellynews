package nz.co.searchwellington.model;

public interface Snapshot {

    public String getBody();
    public void setBody(String newbody);

    // Snapshots are equal if the contents of the page is the same.
    // However, we preform some prefiltering before doing the comparsion.
    public boolean contentMatches(Snapshot o);

}