package nz.co.searchwellington.linkchecking

import java.util.Date

case class LinkCheckRequest(resourceId: String, lastScanned: Option[Date])
