package webhooks

import eri.commons.config.SSConfig


object Configuration extends SSConfig {

  object http {
    val host = Configuration.webhooks.host.asOption[String].getOrElse(defaults.host)
    val port = Configuration.webhooks.port.asOption[Int].getOrElse(defaults.port)
    val path = Configuration.webhooks.path.asOption[String].getOrElse(defaults.path)

    object defaults {
      val host = "localhost"
      val port = 9000
      val path = "hooks"
    }
  }
}
