package wiii.awa

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.SchemeRejection
import akka.stream.ActorMaterializer
import wiii.awa.Interpolator._
import wiii.awa.WebHookProtocol._
import wiii.awa.WebHooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/**
 * Mixin HTTP callbacks
 */
trait WebHooks extends WebApi {
    val hooks = collection.mutable.Map[UUID, HookSubscription]()

    val webhookRoutes =
        path(cfgOr(subKey, Defaults.defaultSub)) {
            (put & entity(as[HookConfigOpt])) { hook =>
                hook.host match {
                    case host if validScheme(host) =>
                        complete {
                            val sub = HookSubscription(UUID.randomUUID, hook)
                            hooks(sub.id) = sub
                            Future {sub.id.toString}
                        }
                    case _ =>
                        reject(SchemeRejection("<none>, http, https"))
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

    final def post[T <: AnyRef : ClassTag : TypeTag](t: T): Seq[Future[HttpResponse]] = post(t, publish)
    final def post[T <: AnyRef : ClassTag : TypeTag](t: T, pub: HttpRequest => Future[HttpResponse]): Seq[Future[HttpResponse]] = {
        for (sub <- hooks.values.toList) yield pub(toRequest(sub.config, t))
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

    def toRequest[T <: AnyRef : ClassTag : TypeTag](hook: HookConfig, t: T): HttpRequest = {
        HttpRequest(method(hook), Uri.apply(s"http://${hook.host}:${hook.port}/${hook.path}"), entity = HttpEntity.apply(ContentTypes.`application/json`, interpolate(hook.body, t)))
    }

    def method(hook: HookConfig): HttpMethod = {
        HttpMethods.getForKeyCaseInsensitive(hook.method).getOrElse(HttpMethods.getForKey(HookConfig.Defaults.defaultMethod).get)
    }

    // unspecified, http, and https schemes are accepted
    def validScheme(s: String) = !s.contains("://") || s.startsWith("http://") || s.startsWith("https://")
}
