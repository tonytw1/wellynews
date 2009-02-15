package nz.co.searchwellington.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

public class ColumnSplitterTest extends TestCase {
    
    
    public void testShouldSplitEvenNumberedLengthGroupIntoEvenColumns() throws Exception {
        ColumnSplitter splitter = new ColumnSplitter();
        
        Integer[] numbers = {1, 2, 3, 4, 5, 6};
        List<Integer> source = Arrays.asList(numbers);
        List<Integer> lefthandSide = new ArrayList<Integer>();
        List<Integer> righthandSide = new ArrayList<Integer>();
        
        splitter.splitList(source, lefthandSide, righthandSide);
        assertEquals(3, lefthandSide.size());
        assertEquals(3, righthandSide.size());
    }

    public void testShouldSplitOddNumberedLengthGroupSuchThatTheLeftHandColumnIsLongest() throws Exception {
        ColumnSplitter splitter = new ColumnSplitter();
        
        Integer[] numbers = {1, 2, 3, 4, 5};
        List<Integer> source = Arrays.asList(numbers);
        List<Integer> lefthandSide = new ArrayList<Integer>();
        List<Integer> righthandSide = new ArrayList<Integer>();
        
        splitter.splitList(source, lefthandSide, righthandSide);
        assertEquals(3, lefthandSide.size());
        assertEquals(2, righthandSide.size());
    }
    
    
    
    public void testShouldBehaveForEmptyList() throws Exception {
        ColumnSplitter splitter = new ColumnSplitter();
        
        Integer[] numbers = {};
        List<Integer> source = Arrays.asList(numbers);
        List<Integer> lefthandSide = new ArrayList<Integer>();
        List<Integer> righthandSide = new ArrayList<Integer>();
        
        splitter.splitList(source, lefthandSide, righthandSide);
        assertEquals(0, lefthandSide.size());
        assertEquals(0, righthandSide.size());
    }
    
    
    
    public void testShouldBehaveForListOfOneItem() throws Exception {
        ColumnSplitter splitter = new ColumnSplitter();
        
        Integer[] numbers = {1};
        List<Integer> source = Arrays.asList(numbers);
        List<Integer> lefthandSide = new ArrayList<Integer>();
        List<Integer> righthandSide = new ArrayList<Integer>();
        
        splitter.splitList(source, lefthandSide, righthandSide);
        assertEquals(1, lefthandSide.size());
        assertEquals(0, righthandSide.size());
    }
    
    public void testShouldBehaveForListOfTwoItems() throws Exception {
        ColumnSplitter splitter = new ColumnSplitter();
        
        Integer[] numbers = {1, 2};
        List<Integer> source = Arrays.asList(numbers);
        List<Integer> lefthandSide = new ArrayList<Integer>();
        List<Integer> righthandSide = new ArrayList<Integer>();
        
        splitter.splitList(source, lefthandSide, righthandSide);
        assertEquals(1, lefthandSide.size());
        assertEquals(1, righthandSide.size());
    }
    

}
