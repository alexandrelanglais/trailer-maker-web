package controllers

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

import play.api.i18n.Langs
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import play.mvc.Results

@Singleton
class ExtAssetsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def at(filePath: String) = {
    val file = new File(s"/opt/binaries/${filePath}")
    Results.ok(file, true)
  }
}

