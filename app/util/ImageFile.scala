/**
 *
 */
package util

import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.sql.Timestamp
import javax.imageio.ImageIO

import dao.Img
import org.imgscalr.Scalr
import play.api.libs.Files.TemporaryFile
import play.api.libs.MimeTypes._
import play.api.{Logger, Play}

import scala.util.Try

trait ImageFile {
  
  sealed trait ClipType
  object ClipType {
    case object UpperLeft extends ClipType
    case object UpperRight extends ClipType
    case object Center extends ClipType
    case object LowerLeft extends ClipType
    case object LowerRight extends ClipType
  }
  
  def clip(ct: String): ClipType = {
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
  
  val ThumbSize = 150
  val ImageSize = 550
  
  def fileExtension(fileName: String): Option[String] = fileName.split('.').takeRight(1).headOption
  def fileNoExtension(fileName: String): Option[String] = fileName.split('.').take(1).headOption
  
  val img: Img
  //def mimeType: String 
  //val fileName: String =  img.created + "_" + img.name.hashCode + "." + extension(mimeType).get
  lazy val origFileName: Option[String] = {
    for{
      fn <- fileNoExtension(img.fileName)
      ext <- fileExtension(img.fileName)
      n <- Some(fn + "_orig." + ext)
    } yield n
  }
  
  
  /** get normally scaled image */
  def image: Try[Array[Byte]]
  /** get thumb size image */
  def thumb: Try[Array[Byte]]
  /** get orignal unscaled imaged */
  def original: Try[Array[Byte]]
  /** set a new thumb image based on clip */
  def thumb_(x: Int, y: Int, w: Int, h: Int): Try[Unit]
  
  //delete
  def deleteImages: Try[Boolean]
  
  
}

object ImageFile {
  
  val ValidTypes = Map(types("png") -> "png", types("jpg") -> "jpg", types("bmp") -> "bmp",
      types("tif") -> "tif", types("gif") -> "gif")
      
  def extension(mimeType: String): Option[String] = ValidTypes.get(mimeType)
  
  def fileName(t: Timestamp, name: String, mimeType: String): String = {
    t.getTime + "_" + name.hashCode + "." + extension(mimeType).get
  }
  
  def apply(img: Img): ImageFile = {
    new ImageFileDisc(img)
  }
  
  def apply(img: Img, tmpFile: TemporaryFile, clipType: String, clipped: Boolean): ImageFile = {
    val ifd = new ImageFileDisc(img)
    require(ifd.create(tmpFile, clipped, clipType).isSuccess, "could not create image files")
    ifd
  }
  
  def apply(name: String, desc: Option[String], pid: Option[Int], mimeType: String, tmpFile: TemporaryFile, clipType: String, clipped: Boolean): ImageFile = {
    val d = new Timestamp(java.lang.System.currentTimeMillis())
    val img = Img(None, name, desc, pid, None, fileName(d, name, mimeType), d, d)
    val ifd = new ImageFileDisc(img)
    require(ifd.create(tmpFile, clipped, clipType).isSuccess, "could not create image files")
    ifd
  }
  
 
  
}

/**
 * @author martins
 *
 */
class ImageFileDisc(val img: Img) extends ImageFile{
  
  val SfxDir = Play.current.configuration.getString("sfx.usr.dir").getOrElse("/tmp/sfx")
  val imgDirName: String = s"$SfxDir/${img.personId.get}/images"
  val thumbDirName: String = s"$SfxDir/${img.personId.get}/thumbs"
  
  def dir(d: String): Try[File] = Try {
    val f = new File(d)
    if(!f.exists()) f.mkdirs
    f
  }
  
  //some helpers
  def thumbDir = dir(thumbDirName).get
  def imgDir = dir(imgDirName).get
  def imgFile = new File(imgDir, img.fileName)
  def thumbFile = new File(thumbDir, img.fileName)
  def origFile = new File(imgDir, origFileName.get)
  
  //overwritten
  override def image = readImage(imgFile)
  override def thumb = readImage(thumbFile)
  override def original = readImage(origFile)
  override def thumb_(x: Int, y: Int, w: Int, h: Int) = Try{
    val bi = ImageIO.read(new File(imgDir, img.fileName))
    putThumb(bi, Some(new Rectangle(x,y,w,h)))
  }
  
  override def deleteImages = {
    for {
      ib <- Try(imgFile.delete())
      tb <- Try(thumbFile.delete())
      ob <- Try(origFile.delete())
    } yield ib && tb && ob
  }
  
  /** Creates thumb, image and original image and stores it on disc */
  def create(file: TemporaryFile, clipped: Boolean, clipType: String): Try[Unit] = Try{
    val orig: File = new File(imgDir+"/"+origFileName.get)
    Logger.debug(orig.getAbsolutePath())
    file.moveTo(orig)
    
    val ct1: Option[ClipType] = clipped match {
      case true => Some(clip(clipType))
      case false => None
    }
    
    for {
      bi <- putImage(orig)
      tf <- dir(thumbDirName)
      clip <- Try(thumbClip(bi, ct1))
      u <- putThumb(bi, clip)
    } yield u
  }
  
  private def putImage(orig: File): Try[BufferedImage] = {
    for {
      src <- Try(ImageIO.read(orig))
      bi <- Try(Scalr.resize(src, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, ImageSize, null))
      output = new File(imgDir, img.fileName)
      u <- Try(ImageIO.write(bi, fileExtension(img.fileName).get, output))
    } yield bi
  }
  
  private def putThumb(src: BufferedImage, clip: Option[Rectangle]): Try[Unit] = Try{
    val t = clip match {
      case Some(rect) => Scalr.crop(src, rect.x, rect.y, rect.width, rect.height, null)
      case None => Scalr.resize(src, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, ThumbSize, null)
    }
    val output = new File(thumbDir, img.fileName)
    ImageIO.write(t, fileExtension(img.fileName).get, output)
  }
  
  
  
  private def readImage(file: File): Try[Array[Byte]] = {
    for{
      f <- Try(file)
      source = scala.io.Source.fromFile(file)(scala.io.Codec.ISO8859)
      byteArray = source.map(_.toByte).toArray
      u = source.close()
    } yield byteArray 
  }
  
  private def thumbClip(src: BufferedImage, ct: Option[ClipType]): Option[Rectangle] = {
    ct match {
      case None => None
      case Some(c) => {
        import ClipType._
        val goal = new Rectangle(150, 150)
        val clip = goal.intersection(new Rectangle(src.getWidth, src.getHeight))
        c match {
          case Center => {
            clip.x = (src.getWidth - clip.width) / 2
            clip.y = (src.getHeight - clip.height) / 2
          }
          case UpperLeft => clip.y = 0; clip.x = 0
          case UpperRight => clip.y = 0; clip.x = src.getWidth - clip.width
          case LowerLeft => clip.x = 0; clip.y = src.getHeight - clip.height
          case LowerRight => {
            clip.x = src.getWidth - clip.width;
            clip.y = src.getHeight - clip.height;
          }
        }
        Some(clip)
      }
    }
  }

}