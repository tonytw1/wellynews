package nz.co.searchwellington.instrumentation

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, SpanBuilder}
import io.opentelemetry.context.Context

object SpanFactory {

  def childOf(currentSpan: Span, spanName: String): SpanBuilder = {
    val tracer = GlobalOpenTelemetry.getTracer("wellynews")
    tracer.spanBuilder(spanName).
      setParent(Context.current().`with`(currentSpan))
  }

}
