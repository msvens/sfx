package views

/**
 * Created by msvens on 17/08/14.
 */
object ViewHelper {

  import dao.Person

  def isSelf(other: Person, loggedIn: Option[Person]) = loggedIn match {
    case Some(p) => p.id == other.id
    case None => false
  }

  def isAuth(person: Option[Person]) = person match {
    case Some(p) => true
    case None => false
  }

}
