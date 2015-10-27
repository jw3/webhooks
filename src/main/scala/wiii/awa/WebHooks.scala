package wiii.awa

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._
import wiii.awa.WebHookOptProtocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait WebHooks extends WebApi {
    import WebHooks._

    val hooks = collection.mutable.Set[HookSubscription]()

    def endpointSub: String = "subscribe"
    def endpointUnsub: String = "unsubscribe"

    val webhooks =
        path(endpointSub) {
            (put & entity(as[HookConfigOpt])) { hookOpt =>
                complete {
                    val sub = HookSubscription(UUID.randomUUID, hookOpt)
                    hooks.add(sub)
                    Future {sub.id.toString}
                }
            }
        }
    /*~
     pathPrefix(endpointUnsub / JavaUUID) { id =>
       hooks.removeIf(p(_.id == id))
       complete {
           "OK"
       }
     }*/

    final def post(cfg: Config): Seq[Future[HttpResponse]] = post(cfg, publish)
    final def post(cfg: Config, pub: HttpRequest => Future[HttpResponse]): Seq[Future[HttpResponse]] = {
        val data = cfg.root.render()
        for (sub <- hooks.toList) yield pub(toRequest(sub.config, data))
    }
}


object WebHooks {
    private def publish(r: HttpRequest)(implicit sys: ActorSystem, mat: ActorMaterializer): Future[HttpResponse] = {
        Http().singleRequest(r)
    }

    def toRequest(hook: HookConfig, data: String = ""): HttpRequest = {
        HttpRequest(HttpMethods.POST, Uri.apply(s"http://${hook.host}:${hook.port}/${hook.path}"), entity = HttpEntity.apply(ContentTypes.`application/json`, data))
    }
    def entityToHook(e: HttpEntity)(implicit fm: Materializer): Future[HookConfigOpt] = {
        stringUnmarshaller(fm)(e).map(x => x.parseJson.convertTo[HookConfigOpt])
    }
    def entityToCfg(e: HttpEntity)(implicit fm: Materializer): Future[Config] = {
        stringUnmarshaller(fm)(e).map(ConfigFactory.parseString)
    }
}
