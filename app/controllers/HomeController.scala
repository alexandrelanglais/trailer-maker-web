package controllers

import javax.inject._

import akka.actor.ActorSystem
import io.trailermaker.core.TrailerMaker
import play.api._
import play.api.mvc._
import better.files._
import play.api.data.Form
import play.api.i18n.Lang
import play.api.i18n.Langs
import play.api.i18n.MessagesImpl
import play.api.i18n.MessagesProvider
import play.api.libs.concurrent.CustomExecutionContext

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(langs: Langs, cc: ControllerComponents, tc: TrailersController, configuration: play.api.Configuration)
    extends AbstractController(cc) {
  val lang: Lang = langs.availables.head

  implicit val messagesProvider: MessagesProvider = {
    MessagesImpl(lang, messagesApi)
  }

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(tc.trailerForm.fill(TrailerData(duration = 15000, length = 1000, videoref = "")), configuration))
  }

}
