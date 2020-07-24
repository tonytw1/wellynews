package nz.co.searchwellington.views

import java.util
import java.util.Arrays

import org.junit.Assert.assertEquals
import org.junit.Test

class ColumnSplitterTest {

  val splitter: ColumnSplitter[Integer] = new ColumnSplitter[Integer]()

  @Test
  def testShouldSplitEvenNumberedLengthGroupIntoEvenColumns() = {
    val source: util.List[Integer] = Arrays.asList(1, 2, 3, 4, 5, 6);
    assertEquals(3, splitter.left(source).size());
    assertEquals(3, splitter.right(source).size());
  }

  @Test
  def testShouldSplitOddNumberedLengthGroupSuchThatTheLeftHandColumnIsLongest() {
    val source: util.List[Integer] =   Arrays.asList(1, 2, 3, 4, 5);
    assertEquals(3, splitter.left(source).size());
    assertEquals(2, splitter.right(source).size());
  }

  @Test
  def testShouldBehaveForEmptyList() {
    val source: util.List[Integer] = Arrays.asList();
    assertEquals(0, splitter.left(source).size());
    assertEquals(0, splitter.right(source).size());
  }

  @Test
  def testShouldBehaveForListOfOneItem() {
    val source: util.List[Integer] =  Arrays.asList(1);
    assertEquals(1, splitter.left(source).size());
    assertEquals(0, splitter.right(source).size());
  }

  @Test
  def testShouldBehaveForListOfTwoItems() {
    val source: util.List[Integer] =   Arrays.asList(1, 2);
    assertEquals(1, splitter.left(source).size());
    assertEquals(1, splitter.right(source).size());
  }

}
