package self.edu.server.http

import spray.routing.{RequestContext, Route, HttpService}
import akka.actor._
import spray.http._
import spray.can.Http
import spray.httpx.unmarshalling._
import akka.pattern.{ask, pipe}
import spray.http.HttpResponse
import akka.util.Timeout
import spray.routing.RequestContext
import akka.actor.SupervisorStrategy.Stop
import scala.concurrent.Future
import spray.json.{JsonParser, JsonReader, DefaultJsonProtocol}
import com.sun.xml.internal.ws.encoding.soap.DeserializationException
import self.edu.server.json


/**
 * Created by alexey on 5/5/14.
 */
class RequestDispatcher extends Actor with HttpService with ActorLogging with PerRequestCreator {

  import json.CustomUnmarshaller._


  def actorRefFactory: ActorContext = context

  val worker = context.actorSelection("/user/Worker")
  val router = context.actorSelection("/user/Router")

  def receive: Receive = serviceMessage orElse runRoute(route)


  import concurrent.duration._
  import context.dispatcher
  implicit val requestTimeout = Timeout(3.seconds)


  val route: Route =
    (path("simple") & post) {
      complete(HttpResponse(StatusCodes.OK, "simple response"))
    } ~ (path("future") & post) {
      complete(Future(
        HttpResponse(StatusCodes.OK, "simple future response")
      ))
    } ~ (path("perRequest") & post) { ctx =>
      perRequest(ctx, worker, WorkerRequest)
    } ~ (path("askWorker") & post) {
      complete{ worker ? WorkerRequest map {
        case r: ResponseMessage => r.json
      }}
    } ~ (path("askRouter") & post) {
      complete{ router ? WorkerRequest map {
        case r: ResponseMessage => r.json
      }}
    } ~ (path("parseJson") & post) {
      entity(as[WorkerJsonRequest]) { request => ctx =>
        ctx.complete{
          router ? request map {
          case r: ResponseMessage => r.json
        }}
      }
    }


  def serviceMessage: Receive = {
    case Http.Bound(address) =>
      log.info(s"RequestDispatcher successfully bound to $address")
  }


}


trait RequestMessage
object WorkerRequest extends RequestMessage
case class WorkerJsonRequest(param1: String, param2: Option[String]) extends RequestMessage
object WorkerJsonRequest extends DefaultJsonProtocol {
  implicit val workerJsonRequestJF = jsonFormat(WorkerJsonRequest.apply, "param1", "param2")
}
object RouterRequest extends RequestMessage

trait ResponseMessage {def json: String}
object SuccessResponse extends ResponseMessage {
  val json = """{"success": true}"""
}
case class CustomResponse(json: String) extends ResponseMessage

trait RequestHandler extends Actor {
  import spray.http.StatusCodes._
  import spray.json._
  import concurrent.duration._

  import context._


  def r: RequestContext
  def target: ActorSelection
  def message: RequestMessage

  setReceiveTimeout(2.seconds)
  target ! message


  def receive = {
    case res: ResponseMessage => complete(OK, res.json)
    case ReceiveTimeout   => complete(GatewayTimeout, "Request timeout")
  }


  def complete[T <: AnyRef](status: StatusCode, result: String) = {
    r.responder ! HttpResponse(
      OK,
      HttpEntity(ContentType(MediaTypes.`application/json`, HttpCharsets.`UTF-8`), result)
    )
    stop(self)
  }


  override val supervisorStrategy =
    OneForOneStrategy() {
      case e =>
        complete(InternalServerError, e.getMessage)
        Stop
    }
}

object RequestHandler {
  case class WithActorSelection(r: RequestContext, target: ActorSelection, message: RequestMessage) extends RequestHandler
}


trait PerRequestCreator {
  this: Actor =>
  import RequestHandler._

  def perRequest(r: RequestContext, target: ActorSelection, message: RequestMessage) =
    context.actorOf(Props(new WithActorSelection(r, target, message)))

}
