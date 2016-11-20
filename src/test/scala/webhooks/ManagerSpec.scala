package webhooks

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import webhooks.HookManager._
import webhooks.models._


class ManagerSpec extends TestKit(ActorSystem()) with WordSpecLike with Matchers with ImplicitSender {
  implicit val mat = ActorMaterializer()

  "manager" should {
    val mgr = childActorOf(HookManager.props())
    val id = UUID.randomUUID.toString

    "register" in {
      val cfg = HookConfig("http://localhost")
      mgr ! CreateHook(id, cfg)
      expectMsgType[HookCreated]
    }

    "unregister" in {
      mgr ! DeleteHook(id)
      expectMsgType[HookDeleted]
    }
  }
}
