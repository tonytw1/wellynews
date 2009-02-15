package nz.co.searchwellington.model;

import java.util.Date;

public class Event implements Comparable {
    
    private String name;
    private Website publisher;
    private Date startDate;
    private Date endDate;
    private String description;

    public Event(String name, Date startDate, Date endDate, String description, Website publisher) {
        this.name = name;
        this.publisher = publisher;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int compareTo(Object event) {       
        if (event instanceof Event) {
            if (this.startDate == null) {                
                return -1;
            }            
            return this.startDate.compareTo(((Event) event).getStartDate());            
        }        
        return 0;
    }

    public Website getPublisher() {
        return publisher;
    }

    public void setPublisher(Website publisher) {
        this.publisher = publisher;
    }
    
    
    

}
