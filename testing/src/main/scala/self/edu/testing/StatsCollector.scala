package self.edu.testing

import akka.actor.{ActorLogging, Actor}
import com.typesafe.config.ConfigFactory

/**
 * Created by alexey on 5/25/14.
 */
class StatsCollector extends Actor with ActorLogging {
  import StatsCollector._
  import context.dispatcher
  import concurrent.duration._
  val config = ConfigFactory.load()

  override def preStart(): Unit = {
    sch(System.currentTimeMillis())
  }

  var counter = 0L
  var prev = 0L

  def receive: Receive = {
    case RequestSent =>
      counter += 1
    case PrintStat(lastTs) =>
      val now = System.currentTimeMillis()
      //      log.debug(s"lastTs: $lastTs, count: $count, prev: $prev, now: $now")
      val rps = Math.round((counter - prev) * 1000.0 / (now - lastTs))
      prev = counter
      log.info(s"$rps rps, total: $counter")
      sch(now)
  }

  private def sch(last: Long): Unit = {
    context.system.scheduler.scheduleOnce(config.getInt("test.stats-interval").seconds) {
      self ! PrintStat(last)
    }
  }
}

object StatsCollector {
  case object RequestSent
  case class PrintStat(lastTs: Long)
}
