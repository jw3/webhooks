import java.time.Duration
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import webhooks.models.{HookConfig, HookConfigOpt}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


package object webhooks {
  type Connection = Flow[HttpRequest, HttpResponse, _]

  implicit def sys2ec(implicit system: ActorSystem): ExecutionContext = system.dispatcher

  implicit def jd2s(d: Duration): FiniteDuration = {
    scala.concurrent.duration.Duration.create(d.getSeconds, TimeUnit.SECONDS)
  }

  implicit def opt2HookConfig(opt: HookConfigOpt): HookConfig = {
    import models.Defaults._
    HookConfig(opt.host, or(opt.topics, Seq.empty), or(opt.port, defaultPort), or(opt.path, defaultPath), or(opt.body, defaultBody), or(opt.span, defaultSpan), or(opt.method, defaultMethod))
  }

  private def or[T](lhs: Option[T], rhs: T) = lhs.getOrElse(rhs)
}
