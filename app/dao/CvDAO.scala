package dao

import javax.inject.{Singleton, Inject}

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import scala.concurrent.Future

@Singleton
class CvDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  
  //protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._
  import java.sql.Timestamp

   class CvTable(tag: Tag) extends Table[Cv](tag, "project"){
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def personId = column[Int]("personId")
    def imgId = column[Option[Int]]("imgId")
    def created = column[Timestamp]("created")
    def updated = column[Timestamp]("updated")
    def body = column[Array[Byte]]("body")
    
    def * = (id, personId, imgId, created, updated, body) <> (Cv.tupled, Cv.unapply _)
    
  }
  
  private val cvs = TableQuery[CvTable]
  
  def get(id: Int): Future[Option[Cv]] = db.run(cvs.filter(_.id === id).result.headOption)
  
  def byUser(personId: Int): Future[Seq[Cv]] = db.run(cvs.filter(_.personId === personId).result)

  def insert(cv: Cv): Future[Option[Int]] = db.run((cvs returning cvs.map(_.id)) += cv)
    
  
}