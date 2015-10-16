/**
 *
 */
import play.api.GlobalSettings
import play.api.Application
import play.api.Play.current
import models._
import play.api.db.slick._
import play.api.Logger

/**
 * @author esvmart
 *
 */
object Global extends GlobalSettings {
  
  override def onStart(app: Application) {
    //InitialData.insert()
  }

}

object InitialData{
  
  /*def insert(): Unit = {
    DB.withSession{implicit s: Session =>
      if(PersonsD.count == 0){
        PersonsD.person_("martin", "martin", "msvens@gmail.com", "Martin Svensson", Some("Hacker Virtuoso")) match {
          case None => Logger.debug("did not insert")
          case Some(person) => {
            BlogsD.blog_(person.id.get, "Martins Blog", "Martins default blog") match {
              case None => Logger.debug("did not insert blog")
              case Some(b) => Logger.debug("insert person and blog")
            }
          }
        }
        PersonsD.person_("anna", "anna", "anna.nilsson78@gmail.com", "Anna Nilsson") match {
          case None => Logger.debug("did not insert")
          case Some(p) => BlogsD.blog_(p.id.get, "Annas Blog", "Annas default blog") match {
            case None => Logger.debug("did not insert blog")
            case Some(b) => Logger.debug("inserted person and blog")
          }
        }
      }
      
    }
  }*/
}