package wiii.awa

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._

import scala.concurrent.ExecutionContext.Implicits.global


object WebApi {
    val cfg = "webapi"
    val host = s"$cfg.host"
    val port = s"$cfg.port"
}

trait WebApi extends Actor {
    def config: Option[Config] = None
    private var serverBinding: Option[Http.ServerBinding] = None

    implicit val _materializer = ActorMaterializer()(context)
    implicit val _actorSystem = context.system

    def webstart(handler: Flow[HttpRequest, HttpResponse, Any]): Unit = {
        val host: String = config.flatMap(_.getAs[String](WebApi.host)).getOrElse("localhost")
        val port: Int = config.flatMap(_.getAs[Int](WebApi.port)).getOrElse(8080)
        Http().bindAndHandle(handler, host, port).onComplete(b => serverBinding = b.toOption)
    }
    def webstop(): Unit = serverBinding.foreach(_.unbind())
}
