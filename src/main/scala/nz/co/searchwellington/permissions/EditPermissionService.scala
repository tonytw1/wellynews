package nz.co.searchwellington.permissions

import com.google.common.base.Strings
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.FrontendResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EditPermissionService @Autowired()() {

  def canEdit(resource: Resource, loggedInUser: Option[User]): Boolean = {
    isAdminOrOwner(resource, loggedInUser)
  }

  def canDelete(resource: FrontendResource, loggedInUser: Option[User]): Boolean = {
    isAdminOrOwner(resource, loggedInUser)
  }

  def canDelete(resource: Resource, loggedInUser: Option[User]): Boolean = {
    isAdminOrOwner(resource, loggedInUser)
  }

  def canAcceptAll(loggedInUser: Option[User]): Boolean = {
    isAdmin(loggedInUser)
  }

  def canAcceptAllFrom(feed: Feed, loggedInUser: Option[User]): Boolean = {
    canAcceptAll(loggedInUser)
  }

  def canCheck(resource: FrontendResource, loggedInUser: Option[User]): Boolean = {
    isAdmin(loggedInUser)
  }

  def canSeeLocalPage(newsitem: Newsitem, loggedInUser: Option[User]): Boolean = {
    isAdmin(loggedInUser)
  }

  def canEditSuggestions(loggedInUser: Option[User]): Boolean = {
    isAdmin(loggedInUser)
  }

  def canAddTag(loggedInUser: Option[User]): Boolean = {
    isAdmin(loggedInUser)
  }

  def canEdit(tag: Tag, loggedInUser: Option[User]): Boolean = {
    isAdmin(loggedInUser)
  }

  def canAddWatchlistAndTag(loggedInUser: Option[User]): Boolean = {
    isAdmin(loggedInUser)
  }

  def canAcceptFeedItems(loggedInUser: User): Boolean = {
    isAdmin(Some(loggedInUser))
  }

  def canDeleteTags(loggedInUser: Option[User]): Boolean = {
    isAdmin(loggedInUser)
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