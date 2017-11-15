package controllers

import javax.inject.Inject
import javax.inject.Singleton

import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

case class TrailerInput(fileName: String)

@Singleton
class TrailersController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {


  def ajaxCall = Action { implicit request =>
    Ok("Ajax Call!")
  }
}