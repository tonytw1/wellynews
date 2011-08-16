package nz.co.searchwellington.model;

import java.util.Date;

public class ArchiveLink {
    
    private Date month;
    private int count;
    
    public ArchiveLink(Date month, int count) {       
        this.month = month;
        this.count = count;
    }
    
    public int getCount() {
        return count;
    }
    
    public Date getMonth() {
        return month;
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((month == null) ? 0 : month.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArchiveLink other = (ArchiveLink) obj;
		if (month == null) {
			if (other.month != null)
				return false;
		} else if (!month.equals(other.month))
			return false;
		return true;
	}
	
}
