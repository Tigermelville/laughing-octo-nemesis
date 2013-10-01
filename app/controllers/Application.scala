package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json
import play.api.libs.json._
import service._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Get Shorty!", models.ShortUrl.count, play.api.Play.current.configuration.getString("shorty.host").getOrElse("")))
  }
  
  /**
   * The body is a javascript array with
   * a single element, the url
   * 
   * @return The url or None if the request is bogus
   */
  def getUrlFromRequest(request: Request[AnyContent]): Option[String] = {
    val optJson = request.body.asJson
    if (!optJson.isDefined)
      None
    else {
      try {
        val vec = optJson.get.asInstanceOf[JsArray]
        if (vec.value.length == 0 || vec.value.length > 1)
          None
        else
            Some(vec(0).as[String])
      } catch {
        case _: Exception =>
          None
      }
    }
  }

  def getShorty = Action { implicit request =>
    val optUrl = getUrlFromRequest(request)
    
    if (!optUrl.isDefined)
      Results.BadRequest("Body must be a Json string with the Url")
    else {
      try {
        Ok(JsString(Shorty.shortenUrl(optUrl.get)))
      } catch {
        case ex: BadUrlException =>
          Results.BadRequest(ex.getMessage)
      }
    }
  }

  def redirectShorty(encoding: String) = Action { implicit request =>
    val optUrl = Shorty.encodingToUrl(encoding)
    
    if (!optUrl.isDefined)
      Results.BadRequest("Invalid shortened url")
    else {
      val url = optUrl.get
      if (url.indexOf("://") == -1)
        // Http or the redirect will retain the
        // port of the shorty service
        Redirect("http://" + url)
      else
        Redirect(url)
    }
  }

  def countShorty() = Action {
    val res = models.ShortUrl.count
    Ok(JsNumber(res))
  }
}
