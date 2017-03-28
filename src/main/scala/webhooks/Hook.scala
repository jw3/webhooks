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
  def props(hook: HookConfig)(implicit mat: ActorMaterializer): Props = Props(new Hook(hook))

  case object Start
  case class Body(payload: String)
}

class Hook(hook: HookConfig)(implicit mat: ActorMaterializer) extends Actor with ActorLogging {
  import context.system

  def inactive: Receive = {
    case Start ⇒
      val topics = hook.topics.map(Class.forName)
      context.become(compile(topics: _*)(self, hook.body.getOrElse("")).orElse(nospan))
  }

  def nospan: Receive = {
    val uri = Uri(hook.url)

    {
      case Body(t) ⇒
        streams.request(uri.nonEmptyPath)(r =>
          Future.successful(r.withMethod(hook.method).withEntity(HttpEntity.apply(ContentTypes.`application/json`, t)))
        ).via(connection(uri.host, uri.port, uri.ssl)).runWith(Sink.ignore)
    }
  }

  def connection(host: String, port: Int, ssl: Boolean)(implicit system: ActorSystem): Connection = {
    if (ssl) Http().outgoingConnectionHttps(host, port)
    else Http().outgoingConnection(host, port)
  }

  def receive: Receive = inactive
}
