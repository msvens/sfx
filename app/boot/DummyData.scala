package boot

import javax.inject.Inject

import dao._
import java.sql.Timestamp

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
 * Created by msvens on 14/10/15.
 */

private[boot] class DummyData @Inject()(personDAO: PersonDAO, blogDAO: BlogDAO) {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  private def insert(pb: (Person,Blog)): Future[Int]  = for {
    pid <- personDAO.insert(pb._1) if pid.isDefined
    ins <- blogDAO.insert(pb._2.copy(personId = pid.get))
  } yield ins

  def insertData: Unit = {
    val futures: Seq[Future[Int]] = for(pb <- DummyData.persons) yield insert(pb)
    val futureOfList = Future.sequence(futures)
    Try(Await.result(futureOfList, Duration.Inf))
  }

  insertData

}

private[boot] object DummyData {

  lazy val date: java.sql.Timestamp = new Timestamp(System.currentTimeMillis())

  def persons: Seq[(Person, Blog)] = Seq(
    (Person(None, "martin", "Martin Svensson", "msvens@gmail.com", None, Some("Hacker Virtuoso"), "martin", date, date),
      Blog(None, -1, date, date, "Martins Blog", "Martins default blog", None)
      ),
    (Person(None, "anna", "Anna Nilsson", "anna.nilsson78@gmail.com", None, None, "anna", date, date),
      Blog(None, -1, date, date, "Annas Blog", "Annas default blog", None)
      )
  )


}