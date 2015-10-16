/**
 *
 */
package util

import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import java.sql.Timestamp
import javax.imageio.ImageIO

import dao.Img
import org.imgscalr.Scalr
import play.api.Play
import play.api.libs.MimeTypes._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.Try


/**
 * @author esvmart
 *
 */
object ImageUtils {

  sealed trait ClipType

  object ClipType {

    case object UpperLeft extends ClipType

    case object UpperRight extends ClipType

    case object Center extends ClipType

    case object LowerLeft extends ClipType

    case object LowerRight extends ClipType

  }

  val ValidTypes = Map(types("png") -> "png", types("jpg") -> "jpg", types("bmp") -> "bmp",
    types("tif") -> "tif", types("gif") -> "gif")

  val SfxDir = Play.current.configuration.getString("sfx.usr.dir").getOrElse("/tmp/sfx")
  val ThumbSize = 150
  val ImageSize = 550

  def imgDirName(id: Int): String = s"$SfxDir/$id/images"

  def imgDir(id: Int): Try[File] = Try {
    //here we should check if it is a directory as well
    val f = new File(imgDirName(id))
    if (!f.exists()) f.mkdirs()
    f
  }

  def thumbDirName(id: Int): String = s"$SfxDir/$id/thumbs"

  def thumbDir(id: Int): Try[File] = Try {
    val f = new File(thumbDirName(id))
    if (!f.exists()) f.mkdirs()
    f
  }

  def fileExtension(fileName: String): Option[String] = fileName.split('.').takeRight(1).headOption

  def fileWithoutExtension(fileName: String): Option[String] = fileName.split('.').take(1).headOption

  def extension(mimeType: String): Option[String] = ValidTypes.get(mimeType)

  def mediaType(fileName: String): Option[String] = forFileName(fileName)

  def fileName(t: Timestamp, name: String, mimeType: String): String = {
    t.getTime + "_" + name.hashCode + "." + extension(mimeType).get
  }

  def fileName(img: Img, mimeType: String): String = fileName(img.created, img.name, mimeType)

  def origSizeFileName(img: Img): Option[String] = {
    for {
      fn <- fileWithoutExtension(img.fileName)
      ext <- fileExtension(img.fileName)
      n <- Some(fn + "_orig." + ext)
    } yield n
  }

  def clipType(ct: String): ClipType = {
    import ClipType._
    val str = ct.toLowerCase
    str match {
      case "upper left" => UpperLeft
      case "upper right" => UpperRight
      case "center" => Center
      case "lower left" => LowerLeft
      case "lower right" => LowerRight
      case _ => Center
    }
  }

  def delImgDir(uid: Int): Unit = {

  }


  def thumbClip(src: BufferedImage, ct: Option[ClipType]): Option[Rectangle] = {
    ct match {
      case None => None
      case Some(c) =>
        import ClipType._
        val goal = new Rectangle(150, 150)
        val clip = goal.intersection(new Rectangle(src.getWidth, src.getHeight))
        c match {
          case Center =>
            clip.x = (src.getWidth - clip.width) / 2
            clip.y = (src.getHeight - clip.height) / 2
          case UpperLeft => clip.y = 0; clip.x = 0
          case UpperRight => clip.y = 0; clip.x = src.getWidth - clip.width
          case LowerLeft => clip.x = 0; clip.y = src.getHeight - clip.height
          case LowerRight =>
            clip.x = src.getWidth - clip.width
            clip.y = src.getHeight - clip.height
        }
        Some(clip)
    }
  }


  def readImageAsync(img: Img)(fdir: Int => Try[File] = imgDir): Future[Array[Byte]] = for {
    dir <- util.async.tryToFuture(fdir(img.personId.get))
    p = new File(dir, img.fileName).toPath
    //afile = AsynchronousFileChannel.open(p, StandardOpenOption.READ)
    afile = AsyncFileChannel.read(p)
    bb = ByteBuffer.allocate(afile.size().asInstanceOf[Int])
    ii <- afile.read(bb, 0)
  } yield bb.array()

  def readImage(img: Img)(fdir: Int => Try[File] = imgDir): Try[Array[Byte]] = {
    for {
      dir <- fdir(img.personId.get)
      f <- Try(new File(dir, img.fileName))
      source = scala.io.Source.fromFile(f)(scala.io.Codec.ISO8859)
      byteArray = source.map(_.toByte).toArray
      u = source.close()
    } yield byteArray
  }

  def origImageAsync(img: Img)(fdir: Int => Try[File] = imgDir): Future[Array[Byte]] = for {
    dir <- util.async.tryToFuture(fdir(img.personId.get))
    p = new File(dir, origSizeFileName(img).get).toPath
    afile = AsyncFileChannel.read(p)
    bb = ByteBuffer.allocate(afile.size().asInstanceOf[Int])
    ii <- afile.read(bb, 0)
  } yield bb.array()

  def origImage(img: Img)(fdir: Int => Try[File] = imgDir): Try[Array[Byte]] = {
    for {
      dir <- fdir(img.personId.get)
      f <- Try(new File(dir, origSizeFileName(img).get))
      source = scala.io.Source.fromFile(f)(scala.io.Codec.ISO8859)
      byteArray = source.map(_.toByte).toArray
      u = source.close()
    } yield byteArray
  }

  def readThumb(img: Img): Try[Array[Byte]] = {
    readImage(img)(thumbDir)
  }

  def readThumbAsync(img: Img): Future[Array[Byte]] = readImageAsync(img)(thumbDir)

  def storeThumb(src: BufferedImage, clip: Option[Rectangle], img: Img): Try[Unit] = Try {
    //create thumbnail directory
    val f = new File(thumbDirName(img.personId.get))
    if (!f.exists()) f.mkdirs()
    val t = clip match {
      case Some(rect) => Scalr.crop(src, rect.x, rect.y, rect.width, rect.height, null)
      case None => Scalr.resize(src, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, ThumbSize, null)
    }
    val output = new File(f, img.fileName)
    println("hello world: " + img.fileName)
    ImageIO.write(t, fileExtension(img.fileName).get, output)
  }

  def updateThumb(img: Img, x: Int, y: Int, w: Int, h: Int): Try[Unit] = Try {
    val rect = new Rectangle(x, y, w, h)
    for {
      f <- imgDir(img.personId.get)
      bi <- Try(ImageIO.read(new File(f, img.fileName)))
      tf <- thumbDir(img.personId.get)
      u <- storeThumb(bi, Some(rect), img)
    } yield u
  }

  def storeImage(img: Img): Try[BufferedImage] = {
    for {
      f <- imgDir(img.personId.get)
      src <- Try(ImageIO.read(new File(f, origSizeFileName(img).get)))
      bi <- Try(Scalr.resize(src, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, ImageSize, null))
      output = new File(f, img.fileName)
      u <- Try(ImageIO.write(bi, fileExtension(img.fileName).get, output))
    } yield bi
  }

  def createThumb(img: Img, ct: String, clipped: Boolean): Try[Unit] = Try {
    val ct1: Option[ClipType] = clipped match {
      case true => Some(clipType(ct))
      case false => None
    }
    println("trying to create thumb")
    for {
      bi <- storeImage(img)
      tf <- thumbDir(img.personId.get)
      clip <- Try(thumbClip(bi, ct1))
      u <- storeThumb(bi, clip, img)
    } yield u
  }

}