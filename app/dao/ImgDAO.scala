package dao

import javax.inject.{Singleton, Inject}

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import scala.concurrent.Future

@Singleton
class ImgDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  
  //protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._
  import java.sql.Timestamp

  //case class Img(id: Option[Int], name: String, desc: Option[String] = None, personId: Option[Int] = None,
  //             projectId: Option[Int] = None, fileName: String, created: Timestamp, updated: Timestamp)
  
  class ImgTable(tag: Tag) extends Table[Img](tag, "img"){
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def desc = column[Option[String]]("desc")
    def personId = column[Option[Int]]("personId")
    def projectId = column[Option[Int]]("projectId")
    def fileName = column[String]("fileName")
    def created = column[Timestamp]("created")
    def updated = column[Timestamp]("updated")
    
    def * = (id, name, desc, personId, projectId, fileName, created, updated) <> (Img.tupled, Img.unapply _)
  }
  
  private val imgs = TableQuery[ImgTable]
  
  def delete(id: Int): Future[Int] = db.run(imgs.filter(_.id === id).delete)
  
  def get(id: Int): Future[Option[Img]] = db.run(imgs.filter(_.id === id).result.headOption)
  def get(name: String): Future[Seq[Img]] = db.run(imgs.filter(_.name === name).result)
  def getPerson(personId: Int): Future[Seq[Img]] = db.run(imgs.filter(_.personId === personId).result)
  def getProject(projectId: Int): Future[Seq[Img]] = db.run(imgs.filter(_.projectId === projectId).result)
  def getFileName(fileName: String): Future[Seq[Img]] = db.run(imgs.filter(_.fileName === fileName).result)

  def insert(i: Img): Future[Option[Int]] = db.run(imgs returning imgs.map(_.id) += i)
  
  def update(i: Img): Future[Int] = {
    val upserAction = imgs.filter(_.id === i.id).result.headOption.flatMap{
      case None => imgs += i
      case Some(_) => imgs.update(i)
    }
    db.run(upserAction)
  }
  
  
  
  
  
}