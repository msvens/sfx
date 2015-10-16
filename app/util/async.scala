package util


import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure, Try}

/**
 * Created by msvens on 09/10/15.
 */
object async {

  private def tToF[A](t:Try[A]): Future[A] = {
    t match{
      case Success(s) => Future.successful(s)
      case Failure(ex) => Future.failed(ex)
    }
  }

  def tryToFuture[A](t: => Try[A])(implicit ec: ExecutionContext): Future[A] = {
    Future{
      t
    }.flatMap{
      case Success(s) => Future.successful(s)
      case Failure(fail) => Future.failed(fail)
    }

  }


}
