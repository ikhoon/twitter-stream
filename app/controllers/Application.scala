package controllers

import com.google.inject.Inject
import core.Env
import play.api.Logger
import play.api.mvc.{Action, Controller}

/**
 * Created by Liam.M(엄익훈) on 8/23/16.
 */
class Application @Inject()(env: Env)  extends Controller {

  val logger = Logger.logger

  def index = Action {
    logger.info("api key : " + env.get("twitter.apiKey"))
    Ok(views.html.index("Your application is ready"))
  }

  def tweets = Action {
    Ok
  }

  /// private
}
