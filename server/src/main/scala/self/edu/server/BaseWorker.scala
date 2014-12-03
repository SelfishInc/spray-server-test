package self.edu.server

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.pattern.pipe
import self.edu.server.http.{CustomResponse, SuccessResponse, WorkerRequest}

import scala.concurrent.Future



/**
 * Created by alexey on 5/25/14.
 */
abstract class BaseWorker extends Actor with ActorLogging {

//  implicit def ec = context.dispatcher
  implicit def ec = context.system.dispatchers.lookup("db-dispatcher")
  protected val sleepTime = 1


  def receive: Receive = {
    case r: WorkerRequest =>
      Future {
        pause(sleepTime)
        CustomResponse(r.aType)
      } pipeTo sender()
  }


  protected def pause(ms: Int): Unit

}
