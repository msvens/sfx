package controllers

import javax.inject.Inject
import play.api._
import play.api.i18n._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import jp.t2v.lab.play2.auth._
import dao.{ Person, PersonDAO }
import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.concurrent.{ Future, ExecutionContext, Await }
import scala.concurrent.duration._

sealed trait Role

object Role {

  case object Administrator extends Role
  case object NormalUser extends Role

  def valueOf(value: String): Role = value match {
    case "Administrator" => Administrator
    case "NormalUser"    => NormalUser
    case _               => throw new IllegalArgumentException()
  }

}

trait AuthSfxConfig extends AuthConfig {
  import Role._

  type Id = Int
  type User = Person
  type Authority = User => Future[Boolean]
  val idTag: ClassTag[Id] = classTag[Id]

  val sessionTimeoutInSeconds: Int = 3600

  def pDAO: PersonDAO

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = return pDAO.get(id)

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.SfxAuth.login)) //should be something different

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.SfxAuth.login)) //should be something different

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Forbidden("no permission"))
  }

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = authority(user)

  /*def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    //this is simply for testing...should be changed to actually do something meaningful
    authority match {
      case "administrator" => true
      case "user"          => true
      case _               => false
    }
  }*/
}

import play.api.libs.concurrent.Execution.Implicits.defaultContext

class SfxAuth @Inject() (val pDAO: PersonDAO, val messagesApi: MessagesApi) extends Controller with LoginLogout with AuthSfxConfig with I18nSupport {
  import play.api.data.validation.Constraints._
  import play.api.data.validation.Constraint
  import play.api.data.validation.Valid
  import play.api.data.validation.Invalid
  import play.api.data.validation.ValidationError

  //val personDAO = new PersonDAO

  def check(login: String, password: String): Option[Person] = {
    val f = pDAO.check(login, password)
    Await.result(f, 500 millis)
  }

  def hasUser(login: String): Boolean = {
    val f = pDAO.get(login)
    Await.result(f, 500 millis) match {
      case Some(p) => true
      case None    => false
    }
  }

  val loginForm = Form(
    mapping("login" -> text, "password" -> text)(check)(_.map(u => (u.login, "")))
      verifying ("Invalid login or password", result => result.isDefined))
  /*val loginForm = Form(
    tuple(
      "login" -> text,
      "password" -> text) verifying ("Invalid username or password", result => result match {
        case (login, password) => check(login, password)
      }))*/

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded.map(_.flashing("success" -> "You've been logged out"))
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors))),
      user => gotoLoginSucceeded(user.get.id.get))
  }
  
  //REGISTRATION:

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

  val userCheck: Constraint[String] = Constraint("constraints.userexist")({
    user =>
      hasUser(user) match {
        case false => Valid
        case true  => Invalid("User already exits")
      }
  })
  
  val registerForm = Form(
    tuple("login" -> nonEmptyText(minLength = 4).verifying(userCheck),
      "email" -> email,
      "fullName" -> text.verifying(nonEmpty),
      "password" -> tuple(
        "main" -> nonEmptyText(minLength = 8).verifying(passwordCheck),
        "check" -> nonEmptyText).verifying("Passwords don't match", password => password._1 == password._2),
      "about" -> optional(text)))
      
  def register = Action { implicit request =>
    Ok(views.html.register(registerForm))
  }
  
  def doRegister = Action.async { implicit r =>
    println("trying to create user")
    registerForm.bindFromRequest.fold(
      formWithErrors => {
        println("form with errors")
        Future.successful(BadRequest(views.html.register(formWithErrors)))
      },
      register => {
        println("about to create a new user: " + register._1)
        for(id <- pDAO.insert(register._1, register._4._1, register._2, register._3, register._5)) yield Redirect(routes.SfxAuth.login)
        /*DB.withSession { implicit s: Session =>
          val p = PersonsD.person_()
          println("created user: " + p)
        }
        println("created user")
        Redirect(routes.Auth.login)*/
      })
  }

}