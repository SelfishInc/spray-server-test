package self.edu.server.http

import spray.routing.{RequestContext, Route, HttpService}
import akka.actor._
import spray.http._
import spray.can.Http
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import akka.pattern.{ask, pipe}
import spray.http.HttpResponse
import akka.util.Timeout
import spray.routing.RequestContext
import akka.actor.SupervisorStrategy.Stop
import scala.concurrent.Future
import spray.json.{JsonParser, JsonReader, DefaultJsonProtocol}


/**
 * Created by alexey on 5/5/14.
 */
class RequestDispatcher extends Actor with HttpService with ActorLogging with PerRequestCreator {


  def actorRefFactory: ActorContext = context

  val router = context.actorSelection("/user/Router")

  def receive: Receive = serviceMessage orElse runRoute(route)


  import concurrent.duration._
  implicit val ec = context.dispatcher
//  implicit val ec = context.system.dispatchers.lookup("http-dispatcher")
  implicit val requestTimeout = Timeout(3.seconds)


  val route: Route =
    (path("simple") & get) {
      complete(ok("simple response"))
    } ~
    (path("run" / "cpu") & get) {
      complete{ router ? CPUWork() map {
        case r: ResponseMessage => ok(r.json)
      }}
    } ~
    (path("run" / "io") & get) {
      complete{ router ? IOWork() map {
        case r: ResponseMessage => ok(r.json)
      }}
    }

  private def ok(str: String) = HttpResponse(StatusCodes.OK, str)


  def serviceMessage: Receive = {
    case Http.Bound(address) =>
      log.info(s"RequestDispatcher successfully bound to $address")
  }


}


trait RequestMessage
class WorkerRequest(val aType: String) extends RequestMessage
case class CPUWork() extends WorkerRequest("cpu")
case class IOWork() extends WorkerRequest("io")

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
