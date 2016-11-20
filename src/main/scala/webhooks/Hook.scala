package webhooks

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import webhooks.Hook._
import webhooks.Topics.compile
import webhooks.models._

import scala.concurrent.Future

object Hook {
  def props()(implicit mat: ActorMaterializer): Props = Props(new Hook)

  def hookMethod(hook: HookConfig): HttpMethod = HttpMethods.getForKeyCaseInsensitive(hook.method).getOrElse(HttpMethods.getForKey(Defaults.defaultMethod).get)
  def validScheme(s: String): Boolean = s.startsWith("http://") || s.startsWith("https://")

  case class Body(payload: String)
}

class Hook(implicit mat: ActorMaterializer) extends Actor with ActorLogging {
  import context.system

  def inactive: Receive = {
    case cfg: HookConfig ⇒
      val fn = nospan(cfg.host, cfg.port, cfg.path, hookMethod(cfg))
      val topics = cfg.topics.map(Class.forName)
      context.become(compile(topics: _*)(self, cfg.body).orElse(fn))
  }

  def nospan(host: String, port: Int, path: String, method: HttpMethod): Receive = {
    val conn = connection(host, port)

    {
      case Body(t) ⇒
        streams.request(path)(r =>
          Future.successful(r.withMethod(method).withEntity(HttpEntity.apply(ContentTypes.`application/json`, t)))
        ).via(conn).runWith(Sink.ignore)
    }
  }

  def connection(host: String, port: Int = 8080, ssl: Boolean = false)(implicit system: ActorSystem): Connection = {
    if (ssl) Http().outgoingConnectionHttps(host, port)
    else Http().outgoingConnection(host, port)
  }

  def receive: Receive = inactive
}
