package wiii.awa

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.ceedubs.ficus.Ficus._
import wiii.awa.WebApi._

import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Web interface mixin
 *
 * Configure with
 * {
 * webapi : {
 * host : "hostname",
 * port : portnumber
 * }
 * }
 *
 */
trait WebApi extends LazyLogging {
    implicit def actorSystem: ActorSystem
    implicit def materializer: ActorMaterializer

    def config: Option[Config] = None
    private var serverBinding: Option[Http.ServerBinding] = None

    def webstart(handler: Flow[HttpRequest, HttpResponse, Any]): Unit = {
        val host: String = cfgOr(hostKey, Defaults.host)
        val port: Int = cfgOr(portKey, Defaults.port)
        logger.info(s"${getClass.getName} web-api starting at [$host:$port]")
        Http().bindAndHandle(handler, host, port).onComplete(b => serverBinding = b.toOption)
        logger.trace(s"${getClass.getName} web-api started")
    }
    def webstop(): Unit = serverBinding.foreach(_.unbind())
    def cfgOr(cfgkey: String, or: String): String = config.flatMap(_.getAs[String](cfgkey)).getOrElse(or)
    def cfgOr(cfgkey: String, or: Int): Int = config.flatMap(_.getAs[Int](cfgkey)).getOrElse(or)
}

object WebApi {
    val webapiKey = "webapi"
    val hostKey = s"$webapiKey.host"
    val portKey = s"$webapiKey.port"

    object Defaults {
        val host = "localhost"
        val port = 8080
    }
}
