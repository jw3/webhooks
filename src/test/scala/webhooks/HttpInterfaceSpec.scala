package webhooks

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.RequestEntity
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest.{Matchers, WordSpecLike}
import webhooks.Configuration.http
import webhooks.HookManager.{CreateHook, DeleteHook}
import webhooks.HttpInterfaceSpec._
import webhooks.models._
import webhooks.protocols._


class HttpInterfaceSpec extends TestKit(ActorSystem()) with WordSpecLike with Matchers {
  implicit val mat = ActorMaterializer()
  val endpoint = s"/${http.path}"

  val probe = TestProbe()
  val conn = testService(probe.ref)

  "client" should {
    "reigster callback" in {
      streams.put(endpoint).mapAsync(1)(r ⇒ Marshal(HookConfigOpt("http://localhost")).to[RequestEntity].map(r.withEntity)).via(conn).runWith(Sink.ignore)

      probe.expectMsgPF() {
        case CreateHook(_, _) ⇒
      }
    }

    "unreigster callback" in {
      val Id = UUID.randomUUID.toString
      streams.delete(s"$endpoint/$Id").via(conn).runWith(Sink.ignore)

      probe.expectMsgPF() {
        case DeleteHook(Id) ⇒
      }
    }
  }
}

object HttpInterfaceSpec {
  def testService(probe: ActorRef)(implicit system: ActorSystem, mat: ActorMaterializer) = TestActorRef[HttpInterface](HttpInterface.props(probe)).underlyingActor.routes
}
