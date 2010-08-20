package nz.co.searchwellington.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ColumnSplitterTest {
	
	ColumnSplitter<Integer> splitter = new ColumnSplitter<Integer>();
	
	@Test
    public void testShouldSplitEvenNumberedLengthGroupIntoEvenColumns() throws Exception {
        List<Integer> source = Arrays.asList(new Integer[] {1, 2, 3, 4, 5, 6});        
        assertEquals(3, splitter.left(source).size());
        assertEquals(3, splitter.right(source).size());
    }

	@Test
	public void testShouldSplitOddNumberedLengthGroupSuchThatTheLeftHandColumnIsLongest() throws Exception {        
        List<Integer> source = Arrays.asList(new Integer[] {1, 2, 3, 4, 5});
        assertEquals(3, splitter.left(source).size());
        assertEquals(2, splitter.right(source).size());
    }
    
    @Test    
    public void testShouldBehaveForEmptyList() throws Exception {
        List<Integer> source = Arrays.asList(new Integer[] {});        
        assertEquals(0, splitter.left(source).size());
        assertEquals(0, splitter.right(source).size());
    }
        
    @Test
    public void testShouldBehaveForListOfOneItem() throws Exception {        
        List<Integer> source = Arrays.asList(new Integer[] {1});     
        assertEquals(1, splitter.left(source).size());
        assertEquals(0, splitter.right(source).size());
    }
    
    @Test
    public void testShouldBehaveForListOfTwoItems() throws Exception {        
        List<Integer> source = Arrays.asList(new Integer[] {1, 2});
        assertEquals(1, splitter.left(source).size());
        assertEquals(1, splitter.right(source).size());
    }
    
}
