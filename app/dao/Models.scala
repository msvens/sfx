package dao

import java.sql.Timestamp

sealed trait SFX {
  def touch: Timestamp
  def ident: Int
  def owner: Option[Int] = None
}

case class Person(id: Option[Int] = None, login: String, fullname: String, email: String, img: Option[Int] = None, bio: Option[String] = None,
                  password: String, created: Timestamp, updated: Timestamp) extends SFX {
  override def touch = updated
  override def ident = id.get
}

case class Blog(id: Option[Int], personId: Int, created: Timestamp, updated: Timestamp, title: String,
                description: String, projectId: Option[Int] = None) extends SFX {
  override def touch = updated
  override def ident = id.get
}

case class Cv(id: Option[Int], personId: Int, imgId: Option[Int] = None,
              created: Timestamp, updated: Timestamp, body: Array[Byte]) extends SFX {
  override def touch = updated
  override def ident = id.get
}

case class Img(id: Option[Int], name: String, desc: Option[String] = None, personId: Option[Int] = None,
               projectId: Option[Int] = None, fileName: String, created: Timestamp, updated: Timestamp) extends SFX {
  override def touch = updated
  override def ident = id.get
  override val owner = personId
}

case class Item(id: Option[Int], personId: Option[Int] = None, timestamp: Timestamp,
                desc: Option[String] = None, itemId: Int, action: String, itemType: String)

case class Post(id: Option[Int] = None, blogId: Int, personId: Int, created: Timestamp, updated: Timestamp,
                title: String, body: String, refId: Option[Int] = None) extends SFX {
  override def ident = id.get
  override def touch = updated
}

case class Project(id: Option[Int], name: String, desc: String, imgId: Option[Int] = None,
                   personId: Int, created: Timestamp, updated: Timestamp) extends SFX {
  def touch = updated
  def ident = id.get
}