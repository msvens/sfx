package controllers

import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.data.Forms._
import play.api.data.Form
import play.api.libs.functional.syntax._

object ApiContainers {
  
  val SUCCESS = "success"
  val ERROR = "error"
  
  case class ApiResponse(status: String, message: Option[String] = None, content: Option[JsObject] = None)
  case class PostForm(title: String, body: String, blogRef: Option[Int], projectRef: Option[Int])
  case class ImageForm(name: Option[String], desc: Option[String], clipped: Boolean, clipType: String)
  case class PersonForm(fullName: Option[String], email: Option[String], img: Option[Int], bio: Option[String],
      password: Option[String], passwordCheck: Option[String])
  
  //Forms
  val imageFormForm = Form({
    import play.api.data.Forms.optional
    mapping(
      "name" -> optional(text),
      "description" -> optional(text),
      "clipped" -> default(boolean, false),
      "clipType" -> default(text, "Center"))(ImageForm.apply)(ImageForm.unapply)
  })
  
  val postFormForm = Form({
    import play.api.data.Forms.optional
    mapping(
      "title" -> text,
      "body" -> text,
      "blogRef" -> optional(number),
      "projectRef" -> optional(number))(PostForm.apply)(PostForm.unapply)
  })
      
  //JSON Converters
  implicit val apiResponseFmt = Json.format[ApiResponse]
  implicit val personFormFmt = Json.format[PersonForm]
  
  implicit val Rds = (
    (__ \ 'name).read[String](minLength[String](5)) and
    (__ \ 'description).read[String](minLength[String](5))) tupled

  /*implicit val ImgUpdate = (
    (__ \ 'name).read[String](minLength[String](5)) and
    (__ \ 'description).read[String](minLength[String](5)) and
    (__ \ 'copy).read[Boolean]) tupled
				 */

  implicit val ImgUpdate = (
    (__ \ 'name).read[String](minLength[String](5)) and
    (__ \ 'description).read[String] and
    (__ \ 'copy).read[Boolean]) tupled

  implicit val ThumbCoords = (
    (__ \ 'x).read[Int] and
    (__ \ 'y).read[Int] and
    (__ \ 'w).read[Int] and
    (__ \ 'h).read[Int]) tupled
  
  
}