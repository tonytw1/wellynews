package nz.co.searchwellington.controllers

import nz.co.searchwellington.forms.NewNewsitem
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.modification.ContentUpdateService
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.{mock, verify}
import org.mockito.{ArgumentCaptor, Matchers}
import org.springframework.validation.BindingResult

class NewNewsitemControllerTest {

  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])

  val controller = new NewNewsitemController(contentUpdateService, loggedInUserFilter)

  @Test
  def canSubmitNewsitems: Unit = {
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    val newNewsitemSubmission: NewNewsitem = new NewNewsitem()
    newNewsitemSubmission.setTitle("A newsitem")
    newNewsitemSubmission.setUrl("https://localhost/a-newsitem")
    val bindingResultWithNoErrors = mock(classOf[BindingResult])
    val createdNewsitem = ArgumentCaptor.forClass(classOf[Newsitem])

    controller.submit(newNewsitemSubmission, bindingResultWithNoErrors)

    verify(contentUpdateService).create(createdNewsitem.capture)(Matchers.eq(ec))
    assertEquals(Some("A newsitem"), createdNewsitem.getValue.title)
    assertEquals(Some("https://localhost/a-newsitem"), createdNewsitem.getValue.page)
   }

}
