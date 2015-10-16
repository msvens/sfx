package dao


import javax.inject.{Singleton, Inject}

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import scala.concurrent.Future

object ItemDAO {
  object Action extends Enumeration {
    type Action = Value
    val DELETE, CREATE, UPDATE = Value
  }
  
  object ItemType extends Enumeration {
    type ItemType = Value
    val PERSON, BLOG, POST, IMG, PROJECT, CV, UNKNOWN = Value
  }
}

@Singleton
class ItemDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {


  

  //protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._
  import java.sql.Timestamp
  
  class ItemTable(tag: Tag) extends Table[Item](tag, "item"){
    
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def personId = column[Option[Int]]("personId")
    def timestamp = column[Timestamp]("timestamp")
    def desc = column[Option[String]]("desc")
    def itemId = column[Int]("itemId")
    def action = column[String]("action")
    def itemType = column[String]("itemType")
    
    def * = (id, personId, timestamp, desc, itemId, action, itemType) <> (Item.tupled, Item.unapply _)
    
  }
  
  private val items = TableQuery[ItemTable]
  
  def list: Future[Seq[Item]] = db.run(items.result)
  
  def after(t: Timestamp): Future[Seq[Item]] = db.run(items.filter(_.timestamp >= t).result)
  def before(t: Timestamp): Future[Seq[Item]] = db.run(items.filter(_.timestamp <= t).result)
  def latest(n: Int) = db.run(items.take(n).sortBy(_.id.desc).result)
  def paged(from: Int, n: Int): Future[Seq[Item]] = db.run(items.drop(from).take(n).sortBy(_.id.desc).result)
  def page(from: Int, n: Int): Future[Seq[Item]] = db.run(items.drop(from).take(n).result)
  
  def insert(i: Item): Future[Int] = db.run(items += i)
  
  
  
  
  
  
  
}