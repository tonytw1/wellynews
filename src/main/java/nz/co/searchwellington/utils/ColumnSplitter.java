package nz.co.searchwellington.utils;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ColumnSplitter <T extends Object> {
        
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
