package self.edu.server

import akka.actor.{Actor, ActorLogging}
import self.edu.server.http.{CustomResponse, SuccessResponse, WorkerRequest}

import scala.concurrent.Future



/**
 * Created by alexey on 5/25/14.
 */
class CPUWorker extends BaseWorker {

  protected def pause(ms: Int): Unit = {
    val end = System.nanoTime() + ms * 1000000L
    while (end > System.nanoTime()) {
      1 + 1
    }
  }

}
