package webhooks

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods._
import spray.json.DefaultJsonProtocol


object models {

  case class HookConfig(url: String,
                        method: HttpMethod,
                        body: Option[String] = None,
                        topics: Seq[String])
  object HookConfig {
    def apply(opt: HookConfigOpt): HookConfig = new HookConfig(
      opt.url,
      opt.method.flatMap(getForKey).getOrElse(POST),
      opt.body,
      opt.topics.getOrElse(Seq.empty)
    )
  }

  case class HookConfigOpt(url: String,
                           method: Option[String] = None,
                           body: Option[String] = None,
                           topics: Option[Seq[String]] = None)

  case class HookSubscribe(id: String, config: HookConfigOpt)
  case class HookUnsubscribe(id: String)
}

object protocols extends DefaultJsonProtocol with SprayJsonSupport {
  import models._

  implicit val hookConfigOpt = jsonFormat4(HookConfigOpt)
  implicit val requestFormat = jsonFormat2(HookSubscribe)
  implicit val unsubscribe = jsonFormat1(HookUnsubscribe)
}
