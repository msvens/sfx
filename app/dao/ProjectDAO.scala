package dao

import javax.inject.{Singleton, Inject}

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import scala.concurrent.Future
import play.api.Play

@Singleton
class ProjectDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  
  //protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._
  import java.sql.Timestamp
  
  //case class Project(id: Option[Int], name: String, desc: String, imgId: Option[Int] = None,
  //                 personId: Int, created: Timestamp, updated: Timestamp) 
  
  
  class ProjectTable(tag: Tag) extends Table[Project](tag, "project"){
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def desc = column[String]("desc")
    def imgId = column[Option[Int]]("imgId")
    def personId = column[Int]("personId")
    def created = column[Timestamp]("created")
    def updated = column[Timestamp]("updated")
    
    def * = (id, name, desc, imgId, personId, created, updated) <> (Project.tupled, Project.unapply _)
  }
  
  private val projects = TableQuery[ProjectTable]
  
  def checkName(personId: Int, name: String): Future[Boolean] = for(v <- get(personId, name)) yield v.isDefined
  
  def get(id: Int): Future[Option[Project]] = db.run(projects.filter(_.id === id).result.headOption)
  def get(personId: Int, name: String): Future[Option[Project]] = {
    val q = for {
      prj <- projects if prj.personId === personId && prj.name.equals(name)
    } yield prj
    db.run(q.result.headOption)
  }
  def byUser(personId: Int): Future[Seq[Project]] = db.run(projects.filter(_.personId === personId).result)


  def insert(name: String, desc: String, imgId: Option[Int], personId: Int): Future[Option[Int]] = {
    val t = new Timestamp(System.currentTimeMillis())
    insert(Project(None, name, desc, imgId, personId, t, t))
  }

  //db.run((cvs returning cvs.map(_.id)) += cv)
  def insert(p: Project): Future[Option[Int]] = db.run(projects returning projects.map(_.id) += p)
  
  def update(p: Project): Future[Int] = {
    val upserAction = projects.filter(_.id === p.id).result.headOption.flatMap{
      case None => projects += p
      case Some(_) => projects.update(p)
    }
    db.run(upserAction)
  }
  
  
}