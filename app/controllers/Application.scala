package controllers

import com.google.inject.Inject
import core.{Env, TwitterStreamer}
import play.api.Logger
import play.api.libs.iteratee.{Concurrent, Enumeratee, Enumerator, Iteratee}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.{WS, WSClient}
import play.api.mvc.{Action, Controller, WebSocket}
import play.extras.iteratees.{Encoding, JsonIteratees}
import play.api.Play.current

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by Liam.M(엄익훈) on 8/23/16.
 */
object Application extends Controller {

  val logger = Logger.logger

  def index = Action { implicit request =>
    Ok(views.html.index("Your application is ready"))
  }

  def tweets = WebSocket.acceptWithActor[String, JsValue] {
    request => out => TwitterStreamer.props(out)
  }
  /*
  def tweets = Action.async {
    credentials().fold(
      Future.successful(InternalServerError("Twitter credentials missing"))
    ) {
      case (consumerKey, requestToken) =>
        val (iteratee: Iteratee[Array[Byte], Unit], enumerator: Enumerator[Array[Byte]]) = Concurrent.joined[Array[Byte]]
        // Enumerator[Array[Byte]] ==> Enumerator[JsObject] 로 변환
        val jsonStream: Enumerator[JsObject] =
          enumerator &>
          Encoding.decode() &>
          Enumeratee.grouped(JsonIteratees.jsSimpleObject)

        val loggingIteratee: Iteratee[JsObject, Unit] = Iteratee.foreach[JsObject] { value =>
          Logger.info(value.toString)
        }

        // attache Enumerator[JsObject] to Iteratee[JsObject, Unit] for consuming JsObject
        jsonStream |>>> loggingIteratee

        WS
          .url("https://stream.twitter.com/1.1/statuses/filter.json")
          .sign(OAuthCalculator(consumerKey, requestToken))
          .withQueryString("track" -> "reactive")
          .get { response =>
            Logger.info("Status: " + response.status)
            iteratee
          }
          .map { _ =>
            Ok("Stream Closed")
          }
    }
  }
  */

  /// private
}
