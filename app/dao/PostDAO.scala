package dao

import javax.inject.{Singleton, Inject}

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import scala.concurrent.Future

@Singleton
class PostDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  
  //protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._
  import com.github.rjeschke.txtmark.Processor
  import java.sql.Timestamp
  
  class PostTable(tag: Tag) extends Table[Post](tag, "post"){
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def blogId = column[Int]("blogId")
    def personId = column[Int]("personId")
    def created = column[Timestamp]("created")
    def updated = column[Timestamp]("updated")
    def title = column[String]("title")
    def body = column[String]("body")
    def refId = column[Option[Int]]("refId")
    
    def * = (id, blogId, personId, created, updated, title, body, refId) <> (Post.tupled, Post.unapply _)
  }
  
  val posts = TableQuery[PostTable]
  
  def get(id: Int): Future[Option[Post]] = db.run(posts.filter(_.id === id).result.headOption)
  def getPerson(personId: Int): Future[Seq[Post]] = db.run(posts.filter(_.personId === personId).result)
  def getRef(refId: Option[Int]): Future[Seq[Post]] = refId match {
    case None => Future.successful(Nil)
    case Some(rid) => db.run(posts.filter(_.refId === rid).result)
  }
  
  def insert(p: Post): Future[Int] = db.run(posts += p)

  def insert(blgId: Int, personId: Int, title: String, body: String, refId: Option[Int]): Future[Int] = {
    val t = new Timestamp(System.currentTimeMillis())
    insert(new Post(None, blgId, personId, t, t, title, body, refId))
  }
  
  def toHTML(posts: Seq[Post]): Seq[Post] = posts.map(p => p.copy(body = Processor.process(p.body)))
  
}