package controllers

import javax.inject.Inject

import dao.{DataAccess, Person}
import jp.t2v.lab.play2.auth.AuthElement
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsSuccess, JsError}
import play.api.mvc.Result
import util.ImageFile


import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
/**
 * Created by msvens on 09/10/15.
 */
class AuthApi @Inject() (da: DataAccess, val messagesApi: MessagesApi) extends Api with AuthElement with AuthSfxConfig {

  import ApiContainers._
  import util.async._

  def pDAO = da.personDAO

  private def sameUser(id: Int)(p: Person): Future[Boolean] = Future.successful(id == p.id.get)

  private def anyUser(id: Int)(p: Person): Future[Boolean] = Future.successful(true)

  private def imageOwner(imgId: Int)(p: Person): Future[Boolean] = for {
    img <- da.imgDAO.get(imgId)
  } yield img match {
      case None => false
      case Some(i) => i.owner.get == p.id.get
    }

  //the id is not needed...will be removed...keep for refactoring purpose
  def addPost(id: Int, blgId: Int) = AsyncStack(AuthorityKey -> sameUser(id)){ implicit r =>
    def add(f: PostForm): Future[Result] =
      for(i <- da.postDAO.insert(blgId,loggedIn.id.get, f.title, f.body, f.blogRef)) yield ok("addPost")

    postFormForm.bindFromRequest.fold(err => Future.successful(badForm(err)), frm => add(frm))

  }

  //the id is not needed...will be removed...keep for refactoring purpose
  def addProject(id: Int) = AsyncStack(parse.json, AuthorityKey -> sameUser(id)) {implicit r =>
    r.body.validate[(String,String)].map {case (name,description) =>
      (for{
        id <- da.projectDAO.insert(name, description, None, loggedIn.id.get) if id.isDefined
      } yield ok("addProject")).recover{case e: Exception => InternalServerError(error("addProject"))}
    }.recoverTotal(e => Future.successful(bad("error in request", JsError.toJson(e))))
  }

  //the id is not needed...keep for refactoring purpose
  def addImage(id: Int) = AsyncStack(parse.multipartFormData, AuthorityKey -> sameUser(id)) { implicit r =>

    def add(f: ImageForm): Future[Result] = r.body.file("file").map { pic =>
      (for {
        v <- Future(ImageFile(f.name.getOrElse(pic.filename), f.desc, loggedIn.id, pic.contentType.get, pic.ref, f.clipType, f.clipped))
        id <- da.imgDAO.insert(v.img) if id.isDefined
      } yield ok("added image")).recover{case e: Exception => bad("could not add image")}
    }.getOrElse(Future.successful(bad("no image uploaded")))

    imageFormForm.bindFromRequest.fold(formWithErrors => Future.successful(badForm(formWithErrors)), imgForm => add(imgForm))
  }

  //the id is not needed...keep for refactoring purpose
  def delImage(id: Int, imgId: Int) = AsyncStack(AuthorityKey -> imageOwner(imgId)) {implicit r =>
    (for {
      img <- da.imgDAO.get(imgId) if img.isDefined
      i <- da.imgDAO.delete(imgId)
      d <- tryToFuture(ImageFile(img.get).deleteImages)
    } yield ok("image deleted: "+d)).recover{case e: Exception => bad("could not delete")}
  }

  //the id is not needed...keep for refactoring purpose
  def person_(id: Int) = AsyncStack(parse.json, AuthorityKey -> sameUser(id)) { implicit r =>
    r.body.validate[PersonForm] match {
      case s: JsSuccess[PersonForm] =>
        val pf = s.get
        //do nothing for now just return
        Future.successful(ok("person updated"))
      case e: JsError => Future.successful(bad("error in request", JsError.toJson(e)))
    }
  }

  //the id is not needed...keep for refactoring purpose
  def updateImage(id: Int, imgId: Int) = AsyncStack(parse.json, AuthorityKey -> imageOwner(imgId)) { implicit r =>
    r.body.validate[(String,String,Boolean)].map {
      case(name,description,copy) =>
        (for {
        img <- da.imgDAO.get(imgId)
        newImg = img.get.copy(name = name, desc = Some(description))
        up <- da.imgDAO.update(newImg)
        } yield ok("image updated: "+up)).recover{case e: Exception => bad("could not update image")}
    }.recoverTotal(e => Future.successful(bad("error in request", JsError.toJson(e))))
  }

  //the is is not needed....keep for refactoring purpose
  def updateThumb(id: Int, imgId: Int) = AsyncStack(parse.json, AuthorityKey -> imageOwner(imgId)) { implicit r =>
    r.body.validate[(Int,Int,Int,Int)].map {
      case (x, y, w, h) =>
        (for {
          img <- da.imgDAO.get(imgId)
          imgf = ImageFile(img.get)
          u <- tryToFuture(imgf.thumb_(x,y,w,h))
        } yield ok("thumb updated")).recover{case e: Exception => bad("could not update thumb")}
    }.recoverTotal(e => bad("error in request", JsError.toJson(e)))
    null
  }



}
