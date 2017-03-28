package webhooks

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import org.scalatest.{EitherValues, Matchers, WordSpecLike}
import webhooks.Hook.NoBody
import webhooks.HookSpec._
import webhooks.models.HookConfig

import scala.concurrent.duration._

/**
 * envision two types of configuration
 * - config file, string based types to Hook
 * - Config() message that sets types on Hook
 */
class HookSpec extends TestKit(ActorSystem()) with WordSpecLike with Matchers with EitherValues with ImplicitSender {
  implicit val mat = ActorMaterializer()
  implicit val to = Timeout(10 seconds)


  "hook" should {
    "callback" when {
      val probe = TestProbe()
      val svc = testService(probe.ref)
      val cfg = testCfg(body = "{{id}}", "webhooks.TestingCC")
      val hook = testHook(cfg, svc)

      "with body" in {
        val id = "bam"
        hook ! TestingCC(id)
        probe.expectMsg(id)
      }

      "without body" in {
        hook ! NoBody
        probe.expectMsg(NoBody)
      }
    }
  }
}

case class TestingCC(id: String)

object HookSpec {
  case class M1(id: String)

  def testCfg(body: String, topics: String*) = HookConfig("http://foo.bar/callback", HttpMethods.POST, Some(body), topics = topics)

  def testHook(cfg: HookConfig, svc: Connection)(implicit system: ActorSystem, mat: ActorMaterializer) = TestActorRef[Hook](new Hook(cfg) {
    override def connection(host: String, port: Int, ssl: Boolean)(implicit system: ActorSystem): Connection =
      svc
  })

  def testService(ref: ActorRef)(implicit mat: ActorMaterializer) = {
    pathPrefix("callback") {
      post {
        extractRequest { r ⇒
          extractStrictEntity(10 seconds) { e ⇒
            val bs = e.data.utf8String
            if (bs.nonEmpty) ref ! bs
            else ref ! NoBody
            complete(StatusCodes.OK)
          }
        }
      }
    }
  }
}
