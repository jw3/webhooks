package webhooks

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, PoisonPill}
import webhooks.models.HookUnsubscribe

import scala.reflect.runtime.universe._
import scala.reflect.runtime.{currentMirror ⇒ m}
import scala.tools.reflect._


object Topics {
  type TopicHandler = (ActorRef, String) ⇒ Receive
  private val toolbox = m.mkToolBox()

  def compile(c: Class[_]): TopicHandler = {
    val s = m.classSymbol(c)
    val t = q"""(ref: akka.actor.ActorRef, fmt: String) ⇒ {{ case m: $s ⇒ ref ! webhooks.Hook.Body(webhooks.Interpolator.interpolate(fmt, m)) } : PartialFunction[Any, Unit]}"""
    toolbox.compile(t).apply().asInstanceOf[TopicHandler]
  }

  def compile(cs: Class[_]*): TopicHandler = {
    (ref: ActorRef, fmt: String) ⇒ cs.map(compile).foldLeft(common(ref))((x, y) ⇒ y(ref, fmt).orElse(x))
  }


  def common(ref: ActorRef): Receive = {
    case HookUnsubscribe(_) ⇒ ref ! PoisonPill
  }
}
