package nz.co.searchwellington.model.frontend

case class Action(label: String, link: String) {
  def  getLabel: String = label
  def  getLink: String = link
}
