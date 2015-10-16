package dao

import javax.inject.{Inject,Singleton}

import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import scala.concurrent.Future

@Singleton
class PersonDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {


  //protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._
  import java.sql.Timestamp
  
  class PersonTable(tag: Tag) extends Table[Person](tag, "person"){
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def login = column[String]("login")
    def fullname = column[String]("fullname")
    def email = column[String]("email")
    def img = column[Option[Int]]("img")
    def bio = column[Option[String]]("bio")
    def password = column[String]("password")
    def created = column[Timestamp]("created")
    def updated = column[Timestamp]("updated")
    
    //def * = (id,name,email,phone,apt,role) <> (Member.tupled, Member.unapply _)
    def * = (id,login,fullname,email,img,bio,password,created,updated) <> (Person.tupled, Person.unapply _)
  }
  
  private val persons = TableQuery[PersonTable]
  
  def delete(p: Person): Future[Int] = delete(p.id.get)
  
  def delete(id: Int): Future[Int] = {
    val q = persons.filter(_.id === id)
    db.run(q.delete)
  }
  
  def get(id: Int): Future[Option[Person]] = {
    val q = persons.filter(_.id === id)
    db.run(q.result.headOption)
  }
  
  def get(login: String): Future[Option[Person]] = {
    val q = persons.filter(_.login === login)
    db.run(q.result.headOption)
  }
  
  def check(login: String, password: String): Future[Option[Person]] = for(p <- get(login)) yield p match {
    case Some(u) => u.password.equals(password) match {
      case true => Some(u)
      case false => None
    }
    case None => None
  }
  
  
  def insert(login: String, password: String, email: String, fullName: String, 
      bio: Option[String] = None): Future[Option[Int]] = {
    val d = new Timestamp(java.lang.System.currentTimeMillis())
    insert(Person(None, login, fullName, email, None, bio, password, d, d))
  }

  //def insert(p: Project): Future[Option[Int]] = db.run(projects returning projects.map(_.id) += p)
  def insert(p: Person): Future[Option[Int]] = db.run(persons returning persons.map(_.id) += p)
  
  def count: Future[Int] = db.run(persons.size.result)
  
  def list: Future[Seq[Person]] = {
    val q = for(t <- persons) yield t
    db.run(q.result)
  }
  
  def update(p: Person): Future[Int] = {
    //val c1 = p.copy(id = ID); //this might change in future versions
    val upserAction = persons.filter(_.id === p.id).result.headOption.flatMap{
      case None => persons += p
      case Some(_) => persons.update(p)
    }
    db.run(upserAction)
  }
}