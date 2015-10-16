/**
 *
 */
package controllers

//import play.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
//import play.api.db.slick.DB
import models._
import play.api.data.validation.Constraints._
import play.api.data.validation.Constraint
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import play.api.data.validation.ValidationError
//import play.api.db.slick.DBAction
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc.Security.AuthenticatedRequest
import scala.concurrent.Future

/**
 * @author esvmart
 *
 */
object Auth extends Controller {
/*
  /*******CHECKS*************************/
  val allNumbers = """\d*""".r
  val allLetters = """[A-Za-z]*""".r

  val passwordCheck: Constraint[String] = Constraint("constraints.passwordcheck")({
    plainText =>
      val errors = plainText match {
        case allNumbers() => Seq(ValidationError("Password is all numbers"))
        case allLetters() => Seq(ValidationError("Password is all letters"))
        case _            => Nil
      }
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
  })

  val userExist: Constraint[String] = Constraint("constraints.userexist")({
    user =>
      hasUser(user) match {
        case false => Valid
        case true  => Invalid("User already exits")
      }
  })

  val loginForm = Form(
    tuple(
      "login" -> text,
      "password" -> text) verifying ("Invalid email or password", result => result match {
        case (login, password) => check(login, password)
      }))

  val registerForm = Form(
    tuple("login" -> nonEmptyText(minLength = 4).verifying(userExist),
      "email" -> email,
      "fullName" -> text.verifying(nonEmpty),
      "password" -> tuple(
        "main" -> nonEmptyText(minLength = 8).verifying(passwordCheck),
        "check" -> nonEmptyText).verifying("Passwords don't match", password => password._1 == password._2),
      "about" -> optional(text)))

  def hasUser(login: String): Boolean = {
    import play.api.db.slick.Session
    DB.withSession { implicit s: Session =>
      PersonsD.byLogin(login) match {
        case None    => false
        case Some(p) => true
      }
    }
  }

  def check(login: String, password: String): Boolean = {
    import play.api.db.slick.Session
    DB.withSession { implicit s: Session =>
      PersonsD.check(login, password) match {
        case None    => false
        case Some(b) => b
      }
    }
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action {
    Redirect(routes.Auth.login).withNewSession.flashing(
      "success" -> "You are now logged out.")
  }

  def authenticate = Action { implicit request =>
    println("in authenticate")
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      login => Redirect(routes.Application.index).withSession(Security.username -> login._1))
  }

  def register = Action { implicit request =>
    Ok(views.html.register(registerForm))
  }

  def doRegister = Action { implicit r =>

    println("trying to create user")
    registerForm.bindFromRequest.fold(
      formWithErrors => {
        println("form with errors")
        BadRequest(views.html.register(formWithErrors))
      },
      register => {
        import play.api.db.slick.Session
        println("about to create a new user: " + register._1)
        DB.withSession { implicit s: Session =>
          val p = PersonsD.person_(register._1, register._4._1, register._2, register._3, register._5)
          println("created user: " + p)
        }
        println("created user")
        Redirect(routes.Auth.login)
      })
  }

  def r = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      login => Redirect(routes.Application.index).withSession(Security.username -> login._1))

  }
*/
}

/*
class DbRequest[A](val person: Option[Person], val s: play.api.db.slick.Session,
                   request: Request[A]) extends WrappedRequest[A](request)

trait secured {

  private def username(request: RequestHeader) = request.session.get("username")

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login);

  private def getUserFromRequest(request: RequestHeader): Option[Person] = {
    null
  }

  class AuthenticatedDbRequest[A](val person: Person, val s: play.api.db.slick.Session,
                                  request: Request[A]) extends WrappedRequest[A](request)

  object PAction extends ActionBuilder[DbRequest] {
    def invokeBlock[A](request: Request[A], block: (DbRequest[A]) => Future[Result]) = {
      DB.withSession { implicit s: play.api.db.slick.Session =>
        username(request) match {
          case None         => block(new DbRequest[A](None, s, request))
          case Some(person) => block(new DbRequest[A](PersonsD.byLogin(person), s, request))
        }
      }
    }
  }

  object Authenticated extends ActionBuilder[AuthenticatedDbRequest] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedDbRequest[A]) => Future[Result]) = {
      AuthenticatedBuilder(req => getUserFromRequest(req)).authenticate(request, { authRequest: AuthenticatedRequest[A, Person] =>
        DB.withSession { implicit s: play.api.db.slick.Session =>
          block(new AuthenticatedDbRequest[A](authRequest.user, s, request))
        }
      })
    }
  }

  def withAuth(f: => String => Request[AnyContent] => Result)(u: RequestHeader => Result = onUnauthorized) = {
    //println("this is username: "+ username(request))
    Security.Authenticated(username, u) { user =>
      Action(request => f(user)(request))
    }
  }

  /*def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }*/

  /**
   * This method shows how you could wrap the withAuth method to also fetch your user
   * You will need to implement UserDAO.findOneByUsername
   */
  /*def withUser(f: Person => Request[AnyContent] => Result)
      (u: RequestHeader => Result = onUnauthorized) = withAuth { username => implicit request =>
        val p = PersonsD.findByLogin(username)(r.s)
        println(username + p)
        p.map { user =>
      f(user)(request)
    }.getOrElse(u(request))
  }{u}*/

}
*/