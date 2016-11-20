package webhooks

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.stream.ActorMaterializer
import webhooks.HookManager._
import webhooks.models._


object HookManager {
  def props()(implicit mat: ActorMaterializer) = Props(new HookManager)

  case class CreateHook(id: String, cfg: HookConfig)
  case class DeleteHook(id: String)
  case class UpdateHook()

  case class HookCreated(id: String, ref: ActorRef)
  case class HookDeleted(id: String)
  case class HookUpdated()

  case class HookNotFound(id: String)
}


class HookManager(implicit mat: ActorMaterializer) extends Actor with ActorLogging {
  def receive: Receive = {
    case CreateHook(id, cfg) ⇒
      val hook = context.actorOf(Hook.props(), id)
      hook ! cfg
      sender ! HookCreated(id, hook)

    case DeleteHook(id) ⇒
      context.child(id) match {
        case None ⇒ sender ! HookNotFound(id)
        case Some(h) ⇒
          h ! PoisonPill
          sender ! HookDeleted(id)
      }
  }
}
