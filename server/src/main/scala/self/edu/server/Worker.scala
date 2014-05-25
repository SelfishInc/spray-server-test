package self.edu.server

import akka.actor.{Actor, ActorLogging}
import self.edu.server.http.{CustomResponse, WorkerJsonRequest, SuccessResponse, WorkerRequest}

/**
 * Created by alexey on 5/25/14.
 */
class Worker extends Actor with ActorLogging {
  import spray.json._

  def receive: Receive = {
    case WorkerRequest =>
      sender() ! SuccessResponse
    case m: WorkerJsonRequest =>
      sender() ! CustomResponse(m.toJson.compactPrint)
  }
}
