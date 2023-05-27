package nz.co.searchwellington.views

import org.springframework.stereotype.Component

import java.math.{BigDecimal, RoundingMode}
import java.util

@Component
class ColumnSplitter[T <: AnyVal] {

  private val NUMBER_OF_COLUMNS: BigDecimal = new BigDecimal(2)

  def left(source: util.List[T]): util.List[T] = {
    source.subList(0, splitPointFor(source))
  }

  def right(source: util.List[T]): util.List[T] = {
    source.subList(splitPointFor(source), source.size)
  }

  private def splitPointFor(source: util.List[T]): Int = {
    new BigDecimal(source.size).divide(NUMBER_OF_COLUMNS, RoundingMode.UP).intValue
  }

}