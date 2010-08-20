package nz.co.searchwellington.utils;

import java.math.BigDecimal;
import java.util.List;

public class ColumnSplitter <T extends Object> {
        
    public ColumnSplitter() {        
    }
        
    public List<T> left(List<T> source) {
        BigDecimal size  = new BigDecimal(source.size());        
        int splitPoint = size.divide(new BigDecimal(2), BigDecimal.ROUND_UP).intValue();
        return source.subList(0, splitPoint);               
    }
    
    public List<T> right(List<T> source) {
        BigDecimal size  = new BigDecimal(source.size());        
        int splitPoint = size.divide(new BigDecimal(2), BigDecimal.ROUND_UP).intValue();
        return source.subList(splitPoint, source.size());               
    }

}
