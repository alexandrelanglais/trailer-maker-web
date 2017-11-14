package controllers

import javax.inject._

import akka.actor.ActorSystem
import io.trailermaker.core.TrailerMaker
import play.api._
import play.api.mvc._
import better.files._
import play.api.libs.concurrent.CustomExecutionContext

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def makeTrailer() = Action.async {
    TrailerMaker
      .makeTrailer(file"/tmp/futfut.avi")
      .map(f =>
        Ok(views.html.trailermade(f.pathAsString))
      )
  }
}
