package controllers

import java.nio.file.Paths
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Source
import akka.util.ByteString
import better.files._
import io.trailermaker.core.AvConvInfo
import io.trailermaker.core.TMOptions
import io.trailermaker.core.TrailerMaker
import play.api.data.Forms._
import play.api.data._
import play.api.http.HttpEntity
import play.api.i18n.Lang
import play.api.i18n.MessagesImpl
import play.api.i18n.MessagesProvider
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import play.api.mvc.ResponseHeader
import play.api.mvc.Result

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

case class TrailerData(duration: Int, length:         Int, videoref: String)
case class UploadedFile(name:    String, description: String)

@Singleton
class TrailersController @Inject()(langs: Langs, cc: ControllerComponents, configuration: play.api.Configuration)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  val lang: Lang = langs.availables.head

  implicit val messagesProvider: MessagesProvider = {
    MessagesImpl(lang, messagesApi)
  }

  implicit val ufFormat = Json.format[UploadedFile]

  val minTrailerDuration = configuration.underlying.getInt("trailer.duration.min")
  val maxTrailerDuration = configuration.underlying.getInt("trailer.duration.max")

  val minTrailerCutLength = configuration.underlying.getInt("trailer.cutlength.min")
  val maxTrailerCutLength = configuration.underlying.getInt("trailer.cutlength.max")

  val trailerForm = Form(
    mapping(
      "duration" -> number(min = minTrailerDuration, max = maxTrailerDuration),
      "length"   -> number(min = minTrailerCutLength, max = maxTrailerCutLength),
      "videoref"  -> nonEmptyText
    )(TrailerData.apply)(TrailerData.unapply)
  )

  def makeTrailer = Action.async(parse.multipartFormData) { implicit request =>
    request.body
      .file("video")
      .filterNot(_.ref.length == 0)
      .map { video =>
        AvConvInfo
          .readFileInfo(File(video.ref))
          .filter(_.duration != 0.seconds)
          .flatMap(_ => {
            val filename = UUID.randomUUID().toString
            val tmpFile  = video.ref.moveTo(Paths.get(s"/tmp/$filename"), replace = true)

            trailerForm.bindFromRequest.fold(
              formWithErrors =>
                Future {
                  BadRequest(views.html.index(formWithErrors, configuration))
              },
              userData => {
                val outStr    = UUID.randomUUID().toString
                val outFolder = configuration.underlying.getString("output.folder")
                val opts =
                  Some(TMOptions(duration = Some(userData.duration), length = Some(userData.length), outputFile = Some(File(outFolder + "/" + outStr))))
                val fut = TrailerMaker.makeTrailer(File(tmpFile.path), opts)
                fut.map(f => Ok(views.html.progress(f.name, configuration)))
              }
            )
          })
          .recoverWith {
            case _ =>
              Future {
                Redirect(routes.HomeController.index).flashing("error" -> "Please choose a video file")
              }
          }
      }
      .getOrElse {
        Future { Redirect(routes.HomeController.index).flashing("error" -> "Please choose a file") }
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

  def upload = Action.async(parse.multipartFormData) { implicit request =>
    request.body
      .file("video")
      .filterNot(_.ref.length == 0)
      .map { video =>
        AvConvInfo
          .readFileInfo(File(video.ref))
          .filter(_.duration != 0.seconds)
          .flatMap(_ => {
            val filename = UUID.randomUUID().toString
            val tmpFile  = video.ref.moveTo(Paths.get(s"/tmp/$filename"), replace = true)
            Future { Ok(Json.toJson(UploadedFile(name = filename, description = "File uploaded"))) }
          })
          .recoverWith {
            case _ =>
              Future {
                Ok(Json.toJson(UploadedFile(name = "", description = "Could not parse file as video file")))
              }
          }
      }
      .getOrElse {
        Future { Redirect(routes.HomeController.index).flashing("error" -> "Please choose a file") }
      }

  }

  def makeTrailerAsync = Action { implicit request =>
    trailerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.trailerform(formWithErrors, configuration)),
      userData => {
        val outStr    = UUID.randomUUID().toString
        val outFolder = configuration.underlying.getString("output.folder")
        val inFile    = userData.videoref
        val opts =
          Some(
            TMOptions(
              duration     = Some(userData.duration),
              length       = Some(userData.length),
              outputFile   = Some(File(outFolder + "/" + outStr)),
              progressFile = Some(File(outFolder + "/" + outStr + ".txt"))
            ))
        TrailerMaker.makeTrailer(File(s"/tmp/$inFile"), opts)
        Ok(outStr)
      }
    )
  }

  def showProgress(videoref: String) = Action { implicit request =>
    try {
      val line = File(videoref).newFileReader.buffered.readLine()
      Ok(line)
    } catch { case _: Throwable => BadRequest("Bad ref") }
  }

}
