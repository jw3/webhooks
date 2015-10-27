package wiii.awa

import java.util.UUID

import wiii.awa.HookConfig.Defaults._


case class HookSubscription(id: UUID, config: HookConfig)

case class HookConfig(host: String,
                      port: Int = defaultPort,
                      path: String = defaultPath,
                      body: String = defaultBody,
                      span: String = defaultSpan, // "2x" | "30s" | "2x|30s"
                      method: String = defaultMethod)


object HookConfig {
    object Defaults {
        val defaultPort = 8080
        val defaultPath = ""
        val defaultBody = ""
        val defaultSpan = "NONE"
        val defaultMethod = "PUT"
    }
}


case class HookConfigOpt(host: String,
                         port: Option[Int],
                         path: Option[String],
                         body: Option[String],
                         span: Option[String],
                         method: Option[String])

object HookConfigOpt {
    import HookConfig.Defaults._

    implicit def opt2HookConfig(opt: HookConfigOpt): HookConfig = {
        HookConfig(opt.host, or(opt.port, defaultPort), or(opt.path, defaultPath), or(opt.body, defaultBody), or(opt.span, defaultSpan), or(opt.method, defaultMethod))
    }

    private def or[T](lhs: Option[T], rhs: T) = lhs.getOrElse(rhs)
}
