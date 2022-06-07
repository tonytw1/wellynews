package nz.co.searchwellington.views

import org.junit.Assert.assertEquals
import org.junit.Test

import java.util
import scala.jdk.CollectionConverters._

class ColumnSplitterTest {

  val splitter = new ColumnSplitter[Int]()

  @Test
  def shouldSplitEvenNumberedLengthGroupIntoEvenColumns(): Unit = {
    val source = util.Arrays.asList(1, 2, 3, 4, 5, 6)
    assertEquals(3, splitter.left(source).size())
    assertEquals(3, splitter.right(source).size())
  }

  @Test
  def shouldSplitOddNumberedLengthGroupSuchThatTheLeftHandColumnIsLongest(): Unit = {
    val source = Seq(1, 2, 3, 4, 5).asJava
    assertEquals(3, splitter.left(source).size())
    assertEquals(2, splitter.right(source).size())
  }

  @Test
  def shouldBehaveForEmptyList(): Unit = {
    val source = Seq[Int]().asJava
    assertEquals(0, splitter.left(source).size())
    assertEquals(0, splitter.right(source).size())
  }

  @Test
  def shouldBehaveForListOfOneItem(): Unit = {
    val source = Seq(1).asJava
    assertEquals(1, splitter.left(source).size())
    assertEquals(0, splitter.right(source).size())
  }

  @Test
  def shouldBehaveForListOfTwoItems(): Unit = {
    val source = Seq(1, 2).asJava
    assertEquals(1, splitter.left(source).size())
    assertEquals(1, splitter.right(source).size())
  }

}
