package wiii.awa

import java.util.function.Predicate

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.{PredefinedFromEntityUnmarshallers => unmarshal}
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._
import wiii.awa.WebHookProtocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait WebHooks extends WebApi {
    val hooks = collection.mutable.Set[Hook]()

    def endpointSub: String = "subscribe"
    def endpointUnsub: String = "unsubscribe"

    val webhooks =
        path(endpointSub) {
            put { ctx =>
                ctx.complete(entityToHook(ctx.request.entity).flatMap {
                    case h: Hook => Future {h.id.toString}
                    case _ => Future {"failed to add hook"}
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
        for (hook <- hooks.toList) yield publish(toRequest(hook, data))
    }

    // override to provide different http impl
    def publish(r: HttpRequest): Future[HttpResponse] = Http().singleRequest(r)

    def entityToHook(e: HttpEntity)(implicit fm: Materializer): Future[Hook] = unmarshal.byteStringUnmarshaller(fm)(e).map(_.utf8String).map(_.toJson.convertTo[Hook])
    def entityToCfg(e: HttpEntity)(implicit fm: Materializer): Future[Config] = unmarshal.byteStringUnmarshaller(fm)(e).map(_.utf8String).map(ConfigFactory.parseString)

    def p[T](f: T => Boolean) = new Predicate[T] {def test(t: T) = f(t)}

    def toRequest(hook: Hook, data: String = ""): HttpRequest = HttpRequest(HttpMethods.POST, Uri.apply(s"${hook.host}:${hook.port}/${hook.path}"), entity = HttpEntity.apply(ContentTypes.`application/json`, data))
}



