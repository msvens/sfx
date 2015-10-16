package dao

import javax.inject.{Singleton, Inject}

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import scala.concurrent.Future

@Singleton
class BlogDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  
  //protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._
  import java.sql.Timestamp

  class BlogTable(tag: Tag) extends Table[Blog](tag, "blog"){
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def personId = column[Int]("personId")
    def created = column[Timestamp]("created")
    def updated = column[Timestamp]("updated")
    def title = column[String]("title")
    def description = column[String]("description")
    def projectId = column[Option[Int]]("projectId")
    
    def * = (id, personId, created, updated, title, description, projectId) <> (Blog.tupled, Blog.unapply _)
  }
  
  private val blogs = TableQuery[BlogTable]
  
  def get(id: Int): Future[Option[Blog]] = db.run(blogs.filter(_.id === id).result.headOption)
  def getPerson(personId: Int): Future[Seq[Blog]] = db.run(blogs.filter(_.personId === personId).result)
  
  def insert(b: Blog): Future[Int] = db.run(blogs += b)
  
  
}