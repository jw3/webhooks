import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.scaladsl.Flow

import scala.concurrent.ExecutionContext


package object webhooks {
  type Connection = Flow[HttpRequest, HttpResponse, _]

  implicit def sys2ec(implicit system: ActorSystem): ExecutionContext = system.dispatcher

  implicit class RichUri(uri: Uri) {
    def host: String = uri.authority.host.toString
    def port: Int = uri.authority.port
    def nonEmptyPath: String = if (uri.path.isEmpty) "/" else uri.path.toString
    def ssl: Boolean = uri.scheme == "https"
  }
}
