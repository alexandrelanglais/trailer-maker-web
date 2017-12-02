package tasks

import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

import akka.actor.ActorSystem
import better.files.File

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import play.api.Logger

object TrailerCleaner {

  def cleanTemporaryDir(maxAge: FiniteDuration): Int = {
    val files = retrieveFilesOlderThan(maxAge)
    files.map(f => f.delete())
    files.size
  }

  def retrieveFilesOlderThan(maxAge: FiniteDuration): List[File] = {
    val dir = File("/tmp")

    dir.list
      .filter(
        f =>
          (f.extension.map(_.equalsIgnoreCase(".webm")).getOrElse(false)
            && f.attributes.lastModifiedTime.to(MILLISECONDS) + maxAge.toMillis < new Date().getTime)
      )
      .toList

  }
}

@Singleton
class TrailerCleaner @Inject()(actorSystem: ActorSystem)(implicit executionContext: ExecutionContext, configuration: play.api.Configuration) {

  actorSystem.scheduler.schedule(initialDelay = 10.seconds, interval = 1.minute) {
    // the block of code that will be executed
    Logger.info("Deleting old trailers")
    val n = TrailerCleaner.cleanTemporaryDir(configuration.get[Int]("trailer.available.for").minutes)
    Logger.info(s"Deleted $n trailers")
  }
}
