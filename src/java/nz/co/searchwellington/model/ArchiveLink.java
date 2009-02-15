package nz.co.searchwellington.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ArchiveLink {
    
    private Date month;
    private int count;
    
    
    
    @Override
    public boolean equals(Object obj) {      
        if (obj instanceof ArchiveLink) {
            return ((ArchiveLink) obj).getMonth().equals(this.getMonth());
        }
        return false;
    }
    
    
    public ArchiveLink(Date month, int count) {       
        this.month = month;
        this.count = count;
    }



    public int getCount() {
        return count;
    }



    public void setCount(int count) {
        this.count = count;
    }



    public Date getMonth() {
        return month;
    }
    
    
    
    public int getYear() {       
        return month.getYear()+ 1900;
    }



    public void setMonth(Date month) {
        this.month = month;
    }
    
    
    public String getHref() {
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("yyyy");
        String year = df.format(getMonth().getTime());
        df.applyPattern("MMM");
        String month = df.format(getMonth().getTime());
        return "/archive/" + year + "/" + month.toLowerCase();
    }
    
        
}
