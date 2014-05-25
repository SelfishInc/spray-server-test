package self.edu.testing

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory



/**
 * Created by alexey on 5/6/14.
 */
object Testing {
  val ResultsDirName = "/tmp/bst-testing/"
  val config = ConfigFactory.load()


  def main (args: Array[String]) {
    implicit val system = ActorSystem("testing")
    val tester = system.actorOf(Props[TesterCreator], "TesterCreator")
    val stats = system.actorOf(Props[StatsCollector], "StatsCollector")
  }
}
