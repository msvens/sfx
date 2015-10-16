package util

import java.nio.ByteBuffer
import java.nio.channels.{CompletionHandler, AsynchronousFileChannel}
import java.nio.file.{Path, StandardOpenOption}

import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.{Failure, Success}


class AsyncFileChannel(val p: Path, val ops: StandardOpenOption*)(implicit val ec: ExecutionContext) {

  val afc = AsynchronousFileChannel.open(p, ops:_*)

  def read(dst: ByteBuffer, pos: Long): Future[Int] = {
    val p = Promise[Int]()
    afc.read(dst,pos,p,handler)
    p.future
  }

  def write(src: ByteBuffer, pos: Long): Future[Int] = {
    val p = Promise[Int]()
    afc.write(src,pos,p,handler)
    p.future
  }

  def close() = afc.close()

  def force(metaData: Boolean) = afc.force(metaData)

  def size() = afc.size()

  private def handler: CompletionHandler[Integer, Promise[Int]] = new CompletionHandler[Integer, Promise[Int]] {

    override def completed(result: Integer, attachment: Promise[Int]): Unit = attachment.complete(Success(result))

    override def failed(exc: Throwable, attachment: Promise[Int]): Unit = attachment.complete(Failure(exc))
  }

}

object AsyncFileChannel{

  def apply(p: Path, ops: StandardOpenOption*)(implicit ec: ExecutionContext): AsyncFileChannel = {
    new AsyncFileChannel(p, ops:_*)
  }

  def read(p: Path)(implicit ec: ExecutionContext) = apply(p, StandardOpenOption.READ)
  def writ(p: Path)(implicit ec: ExecutionContext) = apply(p, StandardOpenOption.WRITE)
  def readwrite(p: Path)(implicit ec: ExecutionContext) = apply(p, StandardOpenOption.READ, StandardOpenOption.WRITE)
}