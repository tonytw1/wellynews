package nz.co.searchwellington.views

import java.math.{BigDecimal, RoundingMode}
import java.util
import java.util.List

import org.springframework.stereotype.Component

@Component
class ColumnSplitter[T <: Object] {

  val NUMBER_OF_COLUMNS: BigDecimal = new BigDecimal(2)

  def left(source: List[T]): List[T] = {
    source.subList(0, splitPointFor(source))
  }

  def right(source: List[T]): List[T] = {
    source.subList(splitPointFor(source), source.size)
  }

  private def splitPointFor(source: util.List[T]): Int = {
    new BigDecimal(source.size).divide(NUMBER_OF_COLUMNS, RoundingMode.UP).intValue
  }

}