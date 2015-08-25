package nz.co.searchwellington.permissions

import com.google.common.base.Strings
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.FrontendResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EditPermissionService @Autowired() (loggedInUserFilter: LoggedInUserFilter) {

  def canEdit(resource: FrontendResource) {
    return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canEdit(resource: Resource) {
    return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canDelete(resource: FrontendResource) {
    return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canDelete(resource: Resource) {
    return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canAcceptAll {
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canAcceptAllFrom(feed: Feed) {
    return canAcceptAll
  }

  def canCheck(resource: FrontendResource) {
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canSeeLocalPage(newsitem: Newsitem) {
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canEditSuggestions {
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canAddTag {
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canEdit(tag: Tag) {
    return loggedInUserFilter.getLoggedInUser != null && loggedInUserFilter.getLoggedInUser.isAdmin
  }

  def canAddWatchlistAndTag {
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canAcceptFeedItems(loggedInUser: User) {
    return isAdmin(loggedInUser)
  }

  def canDeleteTags(loggedInUser: User) {
    return isAdmin(loggedInUser)
  }

  private def isAdminOrOwner(resource: FrontendResource, loggedInUser: User): Boolean = {
    if (isAdmin(loggedInUser)) {
      return true
    }

    val matchesOwnersName = !Strings.isNullOrEmpty(resource.getOwner) && loggedInUser.getProfilename == resource.getOwner
    matchesOwnersName
  }

  private def isAdmin(loggedInUser: User): Boolean = {
    loggedInUser != null && loggedInUser.isAdmin
  }

  private def isAdminOrOwner(resource: Resource, loggedInUser: User): Boolean = {
    if (isAdmin(loggedInUser)) {
      return true
    }

    val isOwner = resource.getOwner != null && loggedInUser.getId == resource.getOwner.getId
    isOwner
  }
  
}