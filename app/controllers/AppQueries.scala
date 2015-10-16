/**
 *
 */
package controllers

import play.api.mvc.Call
import dao._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
 * Utility Object for returning play friendly stuff...
 * @author msvens
 *
 */
case class ItemLink(item: Item, link: Option[Call])

object queries {
  
 
  def itemsWithLinks(from: Int, n: Int, itemDao: ItemDAO)(implicit ec: ExecutionContext): Future[TraversableOnce[ItemLink]] = {
    import dao.ItemDAO.ItemType._
    import dao.ItemDAO.ItemType
    itemDao.page(from, n).map(_.map{item =>
      val l = ItemType.withName(item.itemType) match {
        case PERSON => Some(routes.Application.person(item.itemId))
        case PROJECT => Some(routes.Application.project(item.personId.get, item.itemId))
        case BLOG => Some(routes.Application.blog(item.personId.get, item.itemId , true))
        case IMG => Some(routes.Application.image(item.personId.get, item.itemId))
        case POST => None //should be changed to retrieve blog
        case _ => None
      }
      ItemLink(item, l)
    })
  }

  def itemsWithLinks(max: Int, itemDao: ItemDAO)(implicit ec: ExecutionContext): Future[TraversableOnce[ItemLink]] = itemsWithLinks(0, max, itemDao)

  def blogPosts(blog: Option[Blog], render: Boolean, postDao: PostDAO)(implicit ec: ExecutionContext): Future[Seq[Post]] = blog match {
    case None => Future.successful(Nil)
    case Some(b) => for(l <- postDao.getPerson(b.personId)) yield if(render) postDao.toHTML(l) else l
  }


}