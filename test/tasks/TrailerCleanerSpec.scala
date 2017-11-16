package tasks

import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import scala.concurrent.duration._
import better.files._

class TrailerCleanerSpec extends FlatSpec {

  "TrailerCleaner" should "be able to retrieve files older than 15 minutes" in {
    val fake = File.newTemporaryFile(suffix = ".webm")
    fake.createIfNotExists(false, false)

    val files = TrailerCleaner.retrieveFilesOlderThan(15.minutes)

    assert(files.nonEmpty)
    files.map(println)
    assert(files.contains(fake) == false)
  }

}
