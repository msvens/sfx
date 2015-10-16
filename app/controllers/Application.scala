package controllers

import javax.inject.Inject

import play.api.mvc._
import jp.t2v.lab.play2.auth.OptionalAuthElement
import dao._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.ImageUtils
import scala.concurrent.Future

class Application @Inject() (da: DataAccess) extends Controller with OptionalAuthElement with AuthSfxConfig {

  def pDAO = da.personDAO
  /*
	 * TOP LEVEL ACTIONS:
	 */
  def index = StackAction { implicit r =>
    Ok(views.html.index())
  }


  def people = AsyncStack {implicit r =>
    for(l <- da.personDAO.list) yield Ok(views.html.people(l))
  }

  def stream(from: Int, n: Int) = AsyncStack {implicit r =>
    for(s <- queries.itemsWithLinks(from, n, da.itemDAO)) yield Ok(views.html.stream(s))
  }


  def contact = StackAction(implicit r => Ok(views.html.contact()))

  /***************PERSON LEVEL ACTION***********************************/
  def auth(person: Option[Person], res: Option[Any])(auth: => Result)
    (implicit req: Request[AnyContent], loggedin: Option[Person]): Result = {
    authNoAuth(person, res)(auth, auth)
  }

  def authNoAuth(person: Option[Person], res: Option[Any])(a: => Result, n: => Result)
    (implicit r: Request[AnyContent], loggedin: Option[Person]): Result = {
    if(person.isEmpty || res.isEmpty)
      NotFound
    else{
      val o = for{
        p1 <- person
        p2 <- loggedin
        if p1.id.get == p2.id.get
      } yield a
      o.getOrElse(n)
    }
  }
  
  def asyncAuth(person: Option[Person], res: Option[Any])(a: Future[Result])
    (implicit r: Request[AnyContent], loggedin: Option[Person]): Future[Result] = asyncNoAuth(person, res)(a,a)
    
  //rewrite to be clearer
  def asyncNoAuth(person: Option[Person], res: Option[Any])
    (a: => Future[Result], n: => Future[Result])(implicit r: Request[AnyContent], loggedin: Option[Person]): Future[Result] = person match {
    case None => Future.successful(NotFound)
    case Some(p) => res match {
      case None => Future.successful(NotFound)
      case Some(r) => loggedin match {
        case None => n
        case Some(l) => l.id.get.equals(p.id.get) match {
          case true => a
          case false => n
        }
      }
    }
  } 

  def blogs(id: Int) = AsyncStack { implicit r =>
    for {
      other <- da.personDAO.get(id)
      blgs <- da.blogDAO.getPerson(id)
    } yield authNoAuth(other, Some(true))(Ok(views.html.authblogs(blgs)), Ok(views.html.blogs(other.get, blgs))) 
  }
  
  def blog(id: Int, blogId: Int, render: Boolean) = AsyncStack {implicit r =>
    for {
      blg <- da.blogDAO.get(blogId)
      other <- da.personDAO.get(id)
      posts <- queries.blogPosts(blg, render, da.postDAO)
    } yield auth(other, blg)(Ok(views.html.blog(other.get, blg.get, posts)))
  }
  //other.get could lead to an error being thrown...maybe not because of call by name in authNoAuth
  def person(id: Int) = AsyncStack {implicit r =>
    for {
      prjs <- da.projectDAO.byUser(id)
      other <- da.personDAO.get(id)
    } yield authNoAuth(other, Some(true))(Ok(views.html.authperson(prjs)),Ok(views.html.person(other.get, prjs)))
  }
  
  def images(id: Int) = AsyncStack {implicit r =>
    for{
      other <- da.personDAO.get(id)
      imgs <- da.imgDAO.getPerson(id)
    } yield authNoAuth(other, Some(true))(Ok(views.html.authimages(imgs)), Ok(views.html.images(other.get, imgs)))
  }
  
  def image(id: Int, imgId: Int) = AsyncStack {implicit r =>
    for {
      other <- da.personDAO.get(id)
      img <- da.imgDAO.get(imgId)
    } yield authNoAuth(other, img)(Ok(views.html.authimage(img.get)),Ok(views.html.image(other.get,img.get))) 
  }
  
  def rawThumb(id: Int, imgId: Int) = AsyncStack {implicit r =>
    val nf = Future.successful(NotFound)
    val fb = for {
      img <- da.imgDAO.get(imgId)
      if img.isDefined
      b <- ImageUtils.readThumbAsync(img.get)
    } yield Ok(b).as(ImageUtils.mediaType(img.get.fileName).get)
    fb.recoverWith{case e: Exception => nf}
    
  }

  def rawImage(id: Int, imgId: Int) = AsyncStack {implicit r =>
    val fb = for {
      img <- da.imgDAO.get(imgId)
      if img.isDefined
      b <- ImageUtils.readImageAsync(img.get)()
    } yield Ok(b).as(ImageUtils.mediaType(img.get.fileName).get)
    fb.recoverWith{case e: Exception => Future.successful(NotFound)}
  }

  def origImage(id: Int, imgId: Int) = AsyncStack {implicit r =>
    val fb = for {
      img <- da.imgDAO.get(imgId) if img.isDefined
      b <- ImageUtils.origImageAsync(img.get)()
    } yield Ok(b).as(ImageUtils.mediaType(img.get.fileName).get)
    fb.recoverWith{case e: Exception => Future.successful(NotFound)}
  }

  def projects(id: Int) = AsyncStack { implicit r =>
    for {
      other <- da.personDAO.get(id)
      prjs <- da.projectDAO.byUser(id)
    } yield authNoAuth(other, Some(true))(Ok(views.html.authprojects(prjs)), Ok(views.html.projects(other.get, prjs)))
  }

  //This should be updated...now just returns the general index
  def project(id: Int, prjId: Int) = AsyncStack { implicit r =>
    Future(Ok(views.html.index()))
  }

}