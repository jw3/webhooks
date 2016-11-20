package webhooks

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{Matchers, WordSpecLike}
import webhooks.Hook.Body
import webhooks.TopicSpec._


class TopicSpec extends TestKit(ActorSystem()) with WordSpecLike with Matchers {

  "compilation" should {
    val probe = TestProbe()
    val tt = topictest(probe, "{{id}}")

    "handle single types" when {
      "case class" in {
        val expected = UUID.randomUUID.toString
        tt(Seq(classOf[CC]), CC(expected))
        probe.expectMsg(Body(expected))
      }

      "class" in {
        val expected = UUID.randomUUID.toString
        tt(Seq(classOf[C]), new C(expected))
        probe.expectMsg(Body(expected))
      }
    }

    "handle multiple types" when {
      "case class" in {
        val expected = UUID.randomUUID.toString
        tt(Seq(classOf[C], classOf[CC]), CC(expected))
        probe.expectMsg(Body(expected))
      }
      "class" in {
        val expected = UUID.randomUUID.toString
        tt(Seq(classOf[C], classOf[CC]), new C(expected))
        probe.expectMsg(Body(expected))
      }
    }
  }
}

object TopicSpec {
  class C(val id: String)
  case class CC(id: String)

  type TopicsTest = (Seq[Class[_]], Any) ⇒ Unit
  def topictest(probe: TestProbe, body: String): TopicsTest = Topics.compile(_: _*)(probe.ref, body)(_)
}
