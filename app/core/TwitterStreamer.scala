package core

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee, Enumerator, Iteratee}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.WS
import play.extras.iteratees.{Encoding, JsonIteratees}

/**
 * Created by Liam.M(엄익훈) on 8/24/16.
 */
class TwitterStreamer(out: ActorRef) extends Actor {
  override def receive: Receive = {
    case "subscribe" =>
      Logger.info("Received subscription from client")
      TwitterStreamer subscribe out
  }
}

object TwitterStreamer {
  def props(out: ActorRef): Props = Props(new TwitterStreamer(out))

  val env = new Env

  private var broadcastEnumerator : Option[Enumerator[JsObject]] = None

  def connect() : Unit = {
    credentials()
      .fold( Logger error "Twitter credentials missing" ) {
        case (consumerKey, requestToken) =>
          val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]

          // Enumerator[Array[Byte]] ==> Enumerator[JsObject] 로 변환
          val jsonStream: Enumerator[JsObject] =
            enumerator &> Encoding.decode() &> Enumeratee.grouped(JsonIteratees.jsSimpleObject)


          val (be, _) = Concurrent.broadcast(jsonStream)
          broadcastEnumerator = Some(be)
          val loggingIteratee: Iteratee[JsObject, Unit] = Iteratee.foreach[JsObject] { value =>
            Logger.info(value.toString)
          }
          broadcastEnumerator.foreach{ enumerator =>
            enumerator |>>> loggingIteratee
          }
          stream(consumerKey, requestToken, iteratee)

    }
  }

  def stream(consumerKey: ConsumerKey, requestToken: RequestToken, iteratee: Iteratee[Array[Byte], Unit]): Unit = {
    WS
      .url("https://stream.twitter.com/1.1/statuses/filter.json")
      .sign(OAuthCalculator(consumerKey, requestToken))
      .withQueryString("track" -> "reactive")
      .get { response =>
        Logger.info("Status: " + response.status)
        iteratee
      }
      .map { _ =>
        Logger.info("Stream Closed")
      }
  }

  def subscribe(out: ActorRef): Unit = {
    if(broadcastEnumerator.isEmpty) {
      connect()
    }
    val twitterClient = Iteratee.foreach[JsObject] { tweet => out ! tweet }
    broadcastEnumerator.foreach{ enumerator =>
      enumerator |>>> twitterClient
    }
  }
  def credentials(): Option[(ConsumerKey, RequestToken)] = for {
    apiKey <- env.as[String]("twitter.apiKey")
    apiSecret <- env.as[String]("twitter.apiSecret")
    token <- env.as[String]("twitter.token")
    tokenSecret <- env.as[String]("twitter.tokenSecret")
  } yield (ConsumerKey(apiKey, apiSecret), RequestToken(token, tokenSecret))
}
