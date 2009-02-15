package nz.co.searchwellington.utils;

import java.math.BigDecimal;
import java.util.List;

public class ColumnSplitter {

        
    public ColumnSplitter() {        
    }
    
    
    @SuppressWarnings("unchecked")
    @Deprecated
    public void splitList(List source, List lefthandSide, List righthandSide) {        
        BigDecimal size  = new BigDecimal(source.size());        
        int splitPoint = size.divide(new BigDecimal(2), BigDecimal.ROUND_UP).intValue();
        lefthandSide.addAll(source.subList(0, splitPoint));
        righthandSide.addAll(source.subList(splitPoint, source.size()));
    }
    
    
    public List left(List source) {
        BigDecimal size  = new BigDecimal(source.size());        
        int splitPoint = size.divide(new BigDecimal(2), BigDecimal.ROUND_UP).intValue();
        return source.subList(0, splitPoint);               
    }
    
    
    public List right(List source) {
        BigDecimal size  = new BigDecimal(source.size());        
        int splitPoint = size.divide(new BigDecimal(2), BigDecimal.ROUND_UP).intValue();
        return source.subList(splitPoint, source.size());               
    }

}
