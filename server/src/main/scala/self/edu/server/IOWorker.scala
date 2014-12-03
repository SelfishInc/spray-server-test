package self.edu.server

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import self.edu.server.http.{CustomResponse, SuccessResponse, WorkerRequest}

import scala.concurrent.Future



/**
 * Created by alexey on 5/25/14.
 */
class IOWorker extends BaseWorker {

  protected def pause(ms: Int): Unit = {
//    Thread.sleep(sleepTime)
  }
  
}
