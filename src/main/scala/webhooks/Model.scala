package webhooks

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpMethods
import spray.json.DefaultJsonProtocol


object models {
  import Defaults._

  case class HookConfig(host: String,
                        topics: Seq[String] = Seq.empty,
                        port: Int = defaultPort,
                        path: String = defaultPath,
                        body: String = defaultBody,
                        span: String = defaultSpan, // "2x" | "30s" | "2x|30s"
                        method: String = defaultMethod)

  case class HookConfigOpt(host: String,
                           topics: Option[Seq[String]],
                           port: Option[Int],
                           path: Option[String],
                           body: Option[String],
                           span: Option[String],
                           method: Option[String])

  case class HookSubscribe(id: String, config: HookConfig)
  case class HookUnsubscribe(id: String)



  object Defaults {
    val defaultPort = 8080
    val defaultPath = ""
    val defaultBody = ""
    val defaultSpan = "NONE"
    val defaultMethod = HttpMethods.POST.name
  }
}

object protocols extends DefaultJsonProtocol with SprayJsonSupport {
  import models._
  implicit val hookConfig = jsonFormat7(HookConfig.apply)
  implicit val hookConfigOpt = jsonFormat7(HookConfigOpt.apply)
  implicit val requestFormat = jsonFormat2(HookSubscribe)
  implicit val unsubscribe = jsonFormat1(HookUnsubscribe)
}
