package nz.co.searchwellington.permissions

import com.google.common.base.Strings
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.FrontendResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EditPermissionService @Autowired()(loggedInUserFilter: LoggedInUserFilter) {

  def canEdit(resource: FrontendResource): Boolean = {
    isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canEdit(resource: Resource): Boolean = {
    isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canDelete(resource: FrontendResource): Boolean = {
    isAdminOrOwner(resource, loggedInUserFilter.getLoggedInUser)
  }

  def canDelete(resource: Resource, loggedinUser: Option[User]): Boolean = {
    isAdminOrOwner(resource, loggedinUser)
  }

  def canAcceptAll: Boolean = {
    isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canAcceptAllFrom(feed: Feed): Boolean = {
    canAcceptAll
  }

  def canCheck(resource: FrontendResource): Boolean = {
    isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canSeeLocalPage(newsitem: Newsitem): Boolean = {
    isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canEditSuggestions: Boolean = {
    isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canAddTag: Boolean = {
    isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canEdit(tag: Tag, loggedInUser: Option[User]): Boolean = {
    isAdmin(loggedInUser)
  }

  def canAddWatchlistAndTag: Boolean = {
    isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  def canAcceptFeedItems(loggedInUser: User): Boolean = {
    isAdmin(Some(loggedInUser))
  }

  def canDeleteTags(loggedInUser: Option[User]): Boolean = {
    isAdmin(loggedInUser)
  }

  def isAdmin: Boolean = {
    isAdmin(loggedInUserFilter.getLoggedInUser)
  }

  private def isAdmin(loggedInUser: Option[User]): Boolean = {
    loggedInUser.exists(_.isAdmin)
  }

  private def isAdminOrOwner(resource: FrontendResource, loggedInUser: Option[User]): Boolean = {
    if (isAdmin(loggedInUser)) {
      return true
    }
    loggedInUser.exists { u =>
      val matchesOwnersName = !Strings.isNullOrEmpty(resource.getOwner) && u.getProfilename == resource.getOwner  // TODO proper _id match please
      matchesOwnersName
    }
  }

  private def isAdminOrOwner(resource: Resource, loggedInUser: Option[User]): Boolean = {
    def ownedByLoggedInUser: Boolean = loggedInUser.exists { u =>
      resource.owner.contains(u._id)
    }
    isAdmin(loggedInUser) || ownedByLoggedInUser
  }

}