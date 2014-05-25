package self.edu.testing

import akka.actor.{Actor, ActorLogging}
import scala.concurrent.{Await, Future}
import self.edu.server.http.WorkerJsonRequest
import spray.httpx.encoding.Gzip
import self.edu.testing.StatsCollector.RequestSent


/**
 * Created by alexey on 4/21/14.
 */
class Tester extends Actor with ActorLogging {
  import spray.http._
  import spray.client.pipelining._
  import Tester._
  import context.dispatcher
  import concurrent.duration._



  val host = Testing.config.getString("test.http.host")
  val port = Testing.config.getInt("test.http.port")
  val stats = context.actorSelection("/user/StatsCollector")

  override def preStart(): Unit = {
    context.system.scheduler.schedule(0.second, 1.second, self, ParseJson)
  }


  val pipeline: HttpRequest => Future[HttpResponse] =
    (
      addHeaders(
        HttpHeaders.Host(host, port) :: HttpHeaders.`Accept-Encoding`(HttpEncodings.gzip) :: Nil
      )
        ~> sendReceive
        ~> decode(Gzip)
      )

  import spray.json._
  val jsonRequest = Post("/parseJson", WorkerJsonRequest("test", Some("optional")).toJson.compactPrint)
  def receive: Receive = {
    case SimpleRequest =>
      sendRequest("/simple")
    case FutureRequest =>
      sendRequest("/future")
    case PerRequestRequest =>
      sendRequest("/perRequest")
    case AskWorker =>
      sendRequest("/askWorker")
    case AskRouter =>
      sendRequest("/askRouter")
    case ParseJson =>
      pipeline(jsonRequest)
      stats ! RequestSent

  }


  private def sendRequest(url: String): Unit = {
    pipeline(Post(url))
    stats ! RequestSent
  }
}

object Tester {
  case object SimpleRequest
  case object FutureRequest
  case object PerRequestRequest
  case object AskWorker
  case object AskRouter
  case object ParseJson
}
