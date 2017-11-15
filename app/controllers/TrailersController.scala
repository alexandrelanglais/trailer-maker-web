package controllers

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Source
import akka.util.ByteString
import better.files._
import io.trailermaker.core.TMOptions
import io.trailermaker.core.TrailerMaker
import play.api.data.Forms._
import play.api.data._
import play.api.http.HttpEntity
import play.api.i18n.Lang
import play.api.i18n.MessagesImpl
import play.api.i18n.MessagesProvider
import play.api.i18n._
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import play.api.mvc.ResponseHeader
import play.api.mvc.Result

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class TrailerData(duration: Int)

object TrailersController {

  val trailerForm = Form(
    mapping(
      "duration" -> number(min = 2000, max = 30000),
    )(TrailerData.apply)(TrailerData.unapply)
  )
}

@Singleton
class TrailersController @Inject()(langs: Langs, cc: ControllerComponents, configuration: play.api.Configuration)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  import TrailersController._
  val lang: Lang = langs.availables.head

  implicit val messagesProvider: MessagesProvider = {
    MessagesImpl(lang, messagesApi)
  }

  def ajaxCall = Action { implicit request =>
    Ok("Ajax Call!")
  }

  def makeTrailer = Action.async { implicit request =>
    trailerForm.bindFromRequest.fold(
      formWithErrors =>
        Future {
          // binding failure, you retrieve the form containing errors:
          Ok(views.html.index(formWithErrors))
      },
      userData => {
        val outStr    = UUID.randomUUID().toString
        val outFolder = configuration.underlying.getString("output.folder")
        val opts      = Some(TMOptions(duration = Some(15000L), outputFile = Some(File(outFolder + "/" + outStr))))
        val fut       = TrailerMaker.makeTrailer(file"/home/edoc2/Vidéos/futfut.avi", opts)
        fut.map(f => Ok(views.html.progress(f.name)))
      }
    )
  }

  def getTrailer(fileName: String) = Action { implicit request =>
    val outFolder = configuration.underlying.getString("output.folder")

    val file = new java.io.File(outFolder + "/" + fileName)
    val path:   java.nio.file.Path    = file.toPath
    val source: Source[ByteString, _] = FileIO.fromPath(path)

    val contentLength = Some(file.length())

    Result(
      header = ResponseHeader(200, Map.empty),
      body   = HttpEntity.Streamed(source, contentLength, Some("application/webm"))
    )
//    Ok.sendFile(content = new java.io.File(), inline = false, fileName = _ => "trailer.webm")
  }
}
