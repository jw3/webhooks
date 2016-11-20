package webhooks

import java.time.Duration

import akka.util.Timeout
import eri.commons.config.SSConfig


object Configuration extends SSConfig {

  object http {
    val host = Configuration.webhooks.host.asOption[String].getOrElse(defaults.host)
    val port = Configuration.webhooks.port.asOption[Int].getOrElse(defaults.port)
    val path = Configuration.webhooks.path.asOption[String].getOrElse(defaults.path)
    implicit val timeout = Configuration.webhooks.timeout.asOption[Duration].map(Timeout(_)).getOrElse(Timeout(defaults.timeout))

    object defaults {
      val host = "localhost"
      val port = 8080
      val path = "hooks"
      val timeout = Duration.ofSeconds(3L)
    }
  }
}
