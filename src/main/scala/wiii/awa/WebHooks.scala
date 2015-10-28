package wiii.awa

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, _}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import wiii.awa.WebHookProtocol._
import wiii.awa.WebHooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Mixin HTTP callbacks
 */
trait WebHooks extends WebApi {
    val hooks = collection.mutable.Map[UUID, HookSubscription]()

    val webhooks =
        path(cfgOr(subKey, Defaults.defaultSub)) {
            (put & entity(as[HookConfigOpt])) { hookOpt =>
                complete {
                    val sub = HookSubscription(UUID.randomUUID, hookOpt)
                    hooks(sub.id) = sub
                    Future {sub.id.toString}
                }
            }
        } ~ path(cfgOr(unsubKey, Defaults.defaultUnsub)) {
            (put & entity(as[HookUnsubscribe])) { unsub =>
                complete {
                    hooks.get(unsub.id) match {
                        case Some(sub) =>
                            hooks.remove(sub.id)
                            "OK"
                        case _ => "error 001"
                    }
                }
            }
        } ~ path(cfgOr(statusKey, Defaults.defaultStatus)) {
            get { r =>
                r.complete(Marshal(hooks.values).toResponseFor(r.request))
            }
        }

    final def post(cfg: Config): Seq[Future[HttpResponse]] = post(cfg, publish)
    final def post(cfg: Config, pub: HttpRequest => Future[HttpResponse]): Seq[Future[HttpResponse]] = {
        val data = cfg.root.render()
        for (sub <- hooks.values.toList) yield pub(toRequest(sub.config, data))
    }
}


object WebHooks {
    val subKey = "web.hooks.subscribe"
    val unsubKey = "web.hooks.unsubscribe"
    val statusKey = "web.hooks.status"

    object Defaults {
        val defaultSub = "subscribe"
        val defaultUnsub = "unsubscribe"
        val defaultStatus = "status"
    }

    private def publish(r: HttpRequest)(implicit sys: ActorSystem, mat: ActorMaterializer): Future[HttpResponse] = {
        Http().singleRequest(r)
    }

    def toRequest(hook: HookConfig, data: String = ""): HttpRequest = {
        HttpRequest(HttpMethods.POST, Uri.apply(s"http://${hook.host}:${hook.port}/${hook.path}"), entity = HttpEntity.apply(ContentTypes.`application/json`, data))
    }
}
