package controllers

import java.nio.file.Paths
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

case class TrailerData(duration: Int, length: Int)

object TrailersController {

  val trailerForm = Form(
    mapping(
      "duration" -> number(min = 2000, max = 30000),
      "length"   -> number(min = 1000, max = 5000),
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

  def makeTrailer = Action.async(parse.multipartFormData) { implicit request =>
    request.body
      .file("video")
      .filterNot(_.ref.length == 0)
      .map { video =>
        val filename = UUID.randomUUID().toString
        val tmpFile  = video.ref.moveTo(Paths.get(s"/tmp/$filename"), replace = true)

        trailerForm.bindFromRequest.fold(
          formWithErrors =>
            Future {
              BadRequest(views.html.index(formWithErrors))
            },
          userData => {
            val outStr    = UUID.randomUUID().toString
            val outFolder = configuration.underlying.getString("output.folder")
            val opts      = Some(TMOptions(duration = Some(15000L), outputFile = Some(File(outFolder + "/" + outStr))))
            val fut       = TrailerMaker.makeTrailer(File(tmpFile.path), opts)
            fut.map(f => Ok(views.html.progress(f.name)))
          }
        )
      }
      .getOrElse {
        Future { Redirect(routes.HomeController.index).flashing("error" -> "Missing file") }
      }

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
