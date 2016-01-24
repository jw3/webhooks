package rxthings.webhooks

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.SchemeRejection
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import rxthings.webhooks.Interpolator._
import rxthings.webhooks.WebHookProtocol._
import rxthings.webhooks.WebHooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe._

/**
 * Mixin HTTP callbacks
 */
trait WebHooks extends WebApi with LazyLogging {
    val hooks = collection.mutable.Map[UUID, HookSubscription]()

    val webhookRoutes =
        path(cfgOr(subKey, defaultPath)) {
            (put & entity(as[HookConfigOpt])) { hook =>
                hook.host match {
                    case host if validScheme(host) =>
                        complete {
                            val sub = HookSubscription(UUID.randomUUID, hook)
                            logger.debug(s"added subscription $sub for $hook")
                            hooks(sub.id) = sub
                            sub.id.toString
                        }
                    case _ =>
                        reject(SchemeRejection("http, https"))
                }
            }
        } ~ path(cfgOr(unsubKey, defaultPath)) {
            (delete & entity(as[HookUnsubscribe])) { unsub =>
                complete {
                    hooks.get(unsub.id) match {
                        case Some(sub) =>
                            logger.debug(s"removing subscription ${sub.id} for ${sub.config}")
                            hooks.remove(sub.id)
                            StatusCodes.OK
                        case _ =>
                            StatusCodes.BadRequest
                    }
                }
            }
        } ~ path(cfgOr(statusKey, defaultPath)) {
            get { r =>
                r.complete(Marshal(hooks.values).toResponseFor(r.request))
            }
        }

    final def post[T: TypeTag](t: T): Seq[Future[HttpResponse]] = post(t, publish)
    final def post[T: TypeTag](t: T, pub: HttpRequest => Future[HttpResponse]): Seq[Future[HttpResponse]] = {
        logger.debug(s"posting $t")
        for (sub <- hooks.values.toList) yield pub(toRequest(sub.config, t))
    }
}


object WebHooks {
    val subKey = "web.hooks.subscribe"
    val unsubKey = "web.hooks.unsubscribe"
    val statusKey = "web.hooks.status"
    val defaultPath = "hook"

    private def publish(r: HttpRequest)(implicit sys: ActorSystem, mat: ActorMaterializer): Future[HttpResponse] = {
        Http().singleRequest(r)
    }

    def toRequest[T: TypeTag](hook: HookConfig, t: T): HttpRequest = {
        HttpRequest(method(hook), Uri.apply(s"${hook.host}:${hook.port}/${hook.path}"), entity = HttpEntity.apply(ContentTypes.`application/json`, interpolate(hook.body, t)))
    }

    def method(hook: HookConfig): HttpMethod = {
        HttpMethods.getForKeyCaseInsensitive(hook.method).getOrElse(HttpMethods.getForKey(HookConfig.Defaults.defaultMethod).get)
    }

    // unspecified, http, and https schemes are accepted
    def validScheme(s: String) = s.startsWith("http://") || s.startsWith("https://")
}
