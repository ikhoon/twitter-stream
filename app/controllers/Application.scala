package controllers

import com.google.inject.Inject
import core.Env
import play.api.Logger
import play.api.libs.iteratee.{Concurrent, Enumeratee, Enumerator, Iteratee}
import play.api.libs.json.JsObject
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import play.extras.iteratees.{Encoding, JsonIteratees}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created by Liam.M(엄익훈) on 8/23/16.
 */
class Application @Inject()(env: Env, ws: WSClient)
   extends Controller {

  val logger = Logger.logger

  def index = Action {
    Ok(views.html.index("Your application is ready"))
  }

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

        ws
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

  /// private
  def credentials(): Option[(ConsumerKey, RequestToken)] = for {
      apiKey <- env.as[String]("twitter.apiKey")
      apiSecret <- env.as[String]("twitter.apiSecret")
      token <- env.as[String]("twitter.token")
      tokenSecret <- env.as[String]("twitter.tokenSecret")
    } yield (ConsumerKey(apiKey, apiSecret), RequestToken(token, tokenSecret))
}
