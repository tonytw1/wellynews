package nz.co.searchwellington.permissions

import com.google.common.base.Strings
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.FrontendResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EditPermissionService @Autowired() (loggedInUserFilter: LoggedInUserFilter) {

  def canEdit(resource: FrontendResource): Boolean = {
    return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canEdit(resource: Resource): Boolean = {
    return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canDelete(resource: FrontendResource): Boolean = {
    return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canDelete(resource: Resource): Boolean = {
    return isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canAcceptAll: Boolean ={
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canAcceptAllFrom(feed: Feed): Boolean = {
    return canAcceptAll
  }

  def canCheck(resource: FrontendResource): Boolean = {
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canSeeLocalPage(newsitem: Newsitem): Boolean ={
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canEditSuggestions: Boolean ={
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canAddTag: Boolean = {
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canEdit(tag: Tag): Boolean ={
    return loggedInUserFilter.getLoggedInUser != null && loggedInUserFilter.getLoggedInUser.isAdmin
  }

  def canAddWatchlistAndTag: Boolean = {
    return isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canAcceptFeedItems(loggedInUser: User): Boolean = {
    return isAdmin(loggedInUser)
  }

  def canDeleteTags(loggedInUser: User): Boolean = {
    return isAdmin(loggedInUser)
  }

  def isAdmin(): Boolean = {
    isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  private def isAdmin(loggedInUser: User): Boolean = {
    loggedInUser != null && loggedInUser.isAdmin
  }

  private def isAdminOrOwner(resource: FrontendResource, loggedInUser: User): Boolean = {
    if (isAdmin(loggedInUser)) {
      return true
    }

    val matchesOwnersName = loggedInUser != null && !Strings.isNullOrEmpty(resource.getOwner) && loggedInUser.getProfilename == resource.getOwner
    matchesOwnersName
  }

  private def isAdminOrOwner(resource: Resource, loggedInUser: User): Boolean = {
    if (isAdmin(loggedInUser)) {
      return true
    }

    val isOwner = if (loggedInUser != null) {
      resource.owner.map { o =>
        loggedInUser.id == o
      }.getOrElse(false)
    } else {
      false
    }
    isOwner
  }
  
}