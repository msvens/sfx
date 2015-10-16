/**
 *
 */
package controllers


import dao.{PostDAO, ImgDAO, ProjectDAO}
import play.api._

import play.api.mvc._

import play.api.libs.json._

import play.api.data.Form

import scala.util.{Try,Success,Failure}
import ApiContainers._


/**
 * @author esvmart
 *
 */
trait Api extends Controller {


  /*******************REQUEST AND RESPONSE CONTAINERS********************************************/
  
  //case class ApiReturn(status: String, message: Option[String] = None, content: Option[JsObject] = None)

  //val SUCCESS = "success"
  //val ERROR = "error"

  /*implicit val ApiReturnFormat = (
    (__ \ "status").format[String] and
    (__ \ "message").formatNullable[String] and
    (__ \ "content").formatNullable[JsObject])(ApiReturn.apply, unlift(ApiReturn.unapply))*/

  /*implicit val Rds = (
    (__ \ 'name).read[String](minLength[String](5)) and
    (__ \ 'description).read[String](minLength[String](5))) tupled*/

  /*implicit val ImgUpdate = (
    (__ \ 'name).read[String](minLength[String](5)) and
    (__ \ 'description).read[String](minLength[String](5)) and
    (__ \ 'copy).read[Boolean]) tupled
				 */

  /*implicit val ImgUpdate = (
    (__ \ 'name).read[String](minLength[String](5)) and
    (__ \ 'description).read[String] and
    (__ \ 'copy).read[Boolean]) tupled*/

  /*implicit val ThumbCoords = (
    (__ \ 'x).read[Int] and
    (__ \ 'y).read[Int] and
    (__ \ 'w).read[Int] and
    (__ \ 'h).read[Int]) tupled*/
    
    //case class PostForm(title: String, body: String, blogRef: Option[Int], projectRef: Option[Int])

  /*val addPostForm = Form({
    import play.api.data.Forms.optional
    mapping(
      "title" -> text,
      "body" -> text,
      "blogRef" -> optional(number),
      "projectRef" -> optional(number))(PostForm.apply)(PostForm.unapply)
  })*/

  /*case class ImageForm(name: Option[String], desc: Option[String], clipped: Boolean, clipType: String)

  val addImageForm = Form({
    import play.api.data.Forms.optional
    mapping(
      "name" -> optional(text),
      "description" -> optional(text),
      "clipped" -> default(boolean, false),
      "clipType" -> default(text, "Center"))(ImageForm.apply)(ImageForm.unapply)
  })*/
  
  /*case class PersonForm(fullName: Option[String], email: Option[String], img: Option[Int], bio: Option[String],
      password: Option[String], passwordCheck: Option[String])*/
      
  /*implicit val PersonFormReads: Reads[PersonForm] = (
      (JsPath \ "fullname").readNullable[String] and
      (JsPath \ "email").readNullable[String] and
      (JsPath \ "img").readNullable[Int] and
      (JsPath \ "bio").readNullable[String] and
      (JsPath \ "password").readNullable[String] and
      (JsPath \ "check").readNullable[String]
  )(PersonForm.apply _)*/
  

  

  /********************RESULTS**************************************/
  def badRequest(): Result = { bad("person not logged in or don't match") }

  def success(message: String, content: JsObject = null): JsValue = {
    val ret = ApiResponse(SUCCESS, Some(message), Option(content))
    Json.toJson(ret)
  }

  def error(message: String, content: JsObject = null): JsValue = {
    val ret = ApiResponse(ERROR, Some(message), Option(content))
    Json.toJson(ret)
  }

  def someOrBad[A](message: String, a: Option[A]): Result = a match {
    case None => bad("fail: " + message)
    case Some(a) => ok(message)
  }

  def someOrFail[A](message: String, a: Option[A])(f: JsValue => Result): Result = a match {
    case Some(a) => ok(message)
    case None => fail(message)(f)
  }

  def tryOrBad[A](message: String, a: Try[A]): Result = a match {
    case Success(v) => ok(message)
    case Failure(e) => bad("fail: " + message);
  }

  def ok(message: String, content: JsObject = null): Result = {
    Logger.info(message)
    Ok(success(message, content))
  }

  def bad(message: String, content: JsObject = null): Result = {
    Logger.info(message)
    BadRequest(error(message, content))
  }

  def succ(message: String, content: JsObject = null)(f: JsValue => Result): Result = {
    Logger.info(message)
    f(success(message, content))
  }
  def fail(message: String, content: JsObject = null)(f: JsValue => Result): Result = {
    Logger.info(message)
    f(error(message, content))
  }

  def badForm[A](f: Form[A]): Result = {
    val err = f.globalError.map(mess => mess.message).getOrElse("")
    bad("bad form: " + err)
  }

}