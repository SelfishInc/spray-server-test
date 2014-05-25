package self.edu.server

import akka.actor.{Props, ActorLogging, Actor}
import self.edu.server.http.RequestMessage
import akka.pattern.{ask, pipe}
import akka.util.Timeout

/**
 * Created by alexey on 5/25/14.
 */
class Router extends Actor with ActorLogging {
  import concurrent.duration._
  import context.dispatcher

  val worker = context.actorOf(Props[Worker])
  implicit val timeout = Timeout(3.seconds)

  def receive: Receive = {
    case m: RequestMessage =>
      worker ? m pipeTo sender()
  }
}
