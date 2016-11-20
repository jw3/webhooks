package webhooks

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import webhooks.Configuration.http
import webhooks.HookManager._
import webhooks.HttpInterface.{HttpStart, HttpStop, _}
import webhooks.models._

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}


object HttpInterface {
  def props(mgr: ActorRef)(implicit mat: ActorMaterializer) = Props(new HttpInterface(mgr))
  def uid() = UUID.randomUUID.toString

  case class HttpStart(host: String, port: Int = 8080)
  case class HttpStop()

  case class UnknownState() extends RuntimeException("request completed in an unknown state")
}


class HttpInterface(mgr: ActorRef)(implicit mat: ActorMaterializer) extends Actor with ActorLogging {
  import context.dispatcher

  def stopped: Receive = {
    case HttpStart(h, p) =>
      log.debug("starting webhooks api at {}:{}", h, p)
      Http()(context.system).bindAndHandle(routes, h, p).onComplete {
        case Success(b) => context.become(started(b))
        case Failure(e) => // log ex
      }
  }

  def started(binding: ServerBinding): Receive = {
    log.debug("webhooks api started")

    {
      case HttpStop() =>
        binding.unbind()
        context.become(stopped)
    }
  }

  val routes = {
    import protocols._
    implicit val timeout = Timeout(10 seconds)

    pathPrefix(http.path) {
      (get & path(Segment)) { id ⇒
        complete(StatusCodes.NotImplemented)
      } ~
      (put & entity(as[HookConfigOpt])) { cfg ⇒
        onComplete(mgr ? CreateHook(uid(), cfg)) {
          case Success(HookCreated(id, _)) ⇒ complete(id)
          case _ ⇒ complete(StatusCodes.InternalServerError)
        }
      } ~
      (delete & path(Segment)) { id ⇒
        onComplete(mgr ? DeleteHook(id)) {
          case Success(HookDeleted(_)) ⇒ complete(StatusCodes.OK)
          case _ ⇒ complete(StatusCodes.InternalServerError)
        }
      }
    }
  }

  def receive: Receive = stopped
}
