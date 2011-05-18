package nz.co.searchwellington.model;

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


    public int getYear() {       
        return month.getYear()+ 1900;
    }
    

    public void setCount(int count) {
        this.count = count;
    }



    public Date getMonth() {
        return month;
    }
    
    public void setMonth(Date month) {
        this.month = month;
    }


	
    
   
    
           
}
