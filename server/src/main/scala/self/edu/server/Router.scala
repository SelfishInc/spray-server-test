package self.edu.server

import akka.actor.{Props, ActorLogging, Actor}
import akka.event.LoggingReceive
import akka.routing.FromConfig
import self.edu.server.http.{IOWork, CPUWork, RequestMessage}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

/**
 * Created by alexey on 5/25/14.
 */
class Router extends Actor with ActorLogging {

  val cpuWorker = context.actorOf(Props[CPUWorker].withRouter(FromConfig()), "CPUWorker")
  val ioWorker = context.actorOf(Props[IOWorker].withRouter(FromConfig()), "IOWorker")

  def receive: Receive = {
    case m: CPUWork => cpuWorker forward m
    case m: IOWork => ioWorker forward m
  }

}
