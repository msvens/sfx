package dao

import javax.inject.{Singleton, Inject}

import com.google.inject.ImplementedBy

/**
 * Created by msvens on 14/10/15.
 */
@ImplementedBy(classOf[DefaultDataAccess])
trait DataAccess {

  def imgDAO: ImgDAO
  def blogDAO: BlogDAO
  def postDAO: PostDAO
  def itemDAO: ItemDAO
  def personDAO: PersonDAO
  def projectDAO: ProjectDAO
}

@Singleton
class DefaultDataAccess @Inject() (val imgDAO: ImgDAO, val blogDAO: BlogDAO, val postDAO: PostDAO,
                                  val itemDAO: ItemDAO, val personDAO: PersonDAO, val projectDAO: ProjectDAO) extends DataAccess