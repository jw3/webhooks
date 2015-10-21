package wiii.awa

import java.util.UUID
import java.util.function.Predicate

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/*
"webhook" : {
    "method" : "POST",
    "host" : "remotehost",
    "port" : 8080,
    "path" : "/",
    "body" : "{json}"
    // todo;; span parsing
    "span" : "2x" | "30s" | "2x|30s"
}
*/

trait WebHooks extends WebApi {
    val hooks = collection.mutable.Set[Hook]()

    def endpointSub: String = "subscribe"
    def endpointUnsub: String = "unsubscribe"

    val webhooks =
        path(endpointSub) {
            put { ctx =>
                ctx.complete(entityToCfg(ctx.request.entity).map(cfgToHook).flatMap {
                    case Some(h) => Future {h.id.toString}
                    case None => Future {"failed to add hook"}
                })
            }
        }
    /*~
               pathPrefix(endpointUnsub / JavaUUID) { id =>
                   hooks.removeIf(p(_.id == id))
                   complete {
                       "OK"
                   }
               }*/

    final def post(cfg: Config): Seq[Future[HttpResponse]] = {
        val data = cfg.root.render()
        for (hook <- hooks.toList) yield publish(Hook.toRequest(hook, data))
    }

    // override to provide different http impl
    def publish(r: HttpRequest): Future[HttpResponse] = Http().singleRequest(r)

    def cfgToHook(cfg: Config): Option[Hook] = cfg.getAs[Config](Hook.cfg).map(Hook(_))
    def entityToCfg(e: HttpEntity)(implicit fm: Materializer): Future[Config] = PredefinedFromEntityUnmarshallers.byteStringUnmarshaller(fm)(e).map(_.utf8String).map(ConfigFactory.parseString)

    def p[T](f: T => Boolean) = new Predicate[T] {def test(t: T) = f(t)}
}

case class Hook(id: UUID, host: String, port: Int = 8080, path: String = "/", body: String = "", method: HttpMethod = HttpMethods.POST)

object Hook {
    val cfg = "webhook"
    val method = s"$cfg.method"
    val host = s"$cfg.host"
    val port = s"$cfg.port"
    val path = s"$cfg.path"
    val body = s"$cfg.body"
    val span = s"$cfg.span"

    // for now will do defaults on everything
    // may make the requirements more restrictive later
    val defaultMethod = HttpMethods.POST
    val defaultHost = "localhost"
    val defaultPort = 8080
    val defaultPath = "/"
    val defaultBody = ""
    val defaultSpan = ""

    def apply(cfg: Config): Hook = Hook(UUID.randomUUID,
        cfg.getAs[String](host).getOrElse(defaultHost),
        cfg.getAs[Int](port).getOrElse(defaultPort),
        cfg.getAs[String](path).getOrElse(defaultPath),
        cfg.getAs[String](body).getOrElse(defaultBody),
        cfg.getAs[String](method).flatMap(t => HttpMethods.getForKey(t)).getOrElse(HttpMethods.POST))

    def toRequest(hook: Hook, data: String = ""): HttpRequest = HttpRequest(hook.method, Uri.apply(s"$host:$port/$path"), entity = HttpEntity.apply(ContentTypes.`application/json`, data))
}
