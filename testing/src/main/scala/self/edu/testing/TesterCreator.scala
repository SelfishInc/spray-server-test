package self.edu.testing

import akka.actor.{Props, ActorLogging, Actor}
import com.typesafe.config.ConfigFactory

/**
 * Created by alexey on 5/25/14.
 */
class TesterCreator extends Actor with ActorLogging {

  val config = ConfigFactory.load()
  val accountsCount = config.getInt("test.number-of-accounts")

  override def preStart(): Unit = {
    for(i <- 1 to accountsCount){
      context.actorOf(Props[Tester], s"tester-$i")
    }
  }

  def receive = {
    case msg =>
  }
}
