package wii.awa

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.{ConfigRenderOptions, ConfigFactory}
import org.scalatest.{Matchers, WordSpecLike}
import wii.awa.CallbackSpec._
import wiii.awa.{HookConfig, WebApi, WebHooks}
import spray.json._
import wiii.awa.WebHookProtocol._

import scala.concurrent.duration.DurationInt

import scala.concurrent.Await

object CallbackSpec {
    val serverPort = 9999
    val clientPort = 8888

    case class OK()
    case class Call()

    def cfg(p: Int) = Option(ConfigFactory.parseString(s"webapi.port=$p"))
}

class CallbackSpec extends TestKit(ActorSystem(classOf[CallbackSpec].getSimpleName.dropRight(1)))
                           with ImplicitSender with WordSpecLike with Matchers {

    implicit val materializer = ActorMaterializer()
    val clientProbe = TestProbe()
    val client = system.actorOf(Client.props(clientProbe))
    val server = system.actorOf(Props[Server])

    "client" should {
        "reigster callback" in {
            val data = ConfigFactory.parseString("host=localhost").root().render(ConfigRenderOptions.concise())
            val f = Http().singleRequest(HttpRequest(HttpMethods.PUT, Uri(s"http://localhost:$serverPort/subscribe"), entity=HttpEntity.apply(ContentTypes.`application/json`, data)))
            Await.result(f, 10 seconds)
        }
        "be called back" in {

        }
    }
}

class Server extends Actor with WebHooks {
    override def config = cfg(serverPort)
    override def preStart(): Unit = webstart(webhooks)

    def receive: Receive = {
        case Call() => post(ConfigFactory.parseString("{}"))
    }
}

object Client {
    def props(probe: TestProbe) = Props(new Client(probe))
}

class Client(probe: TestProbe) extends Actor with WebApi {
    override def config = cfg(clientPort)
    override def preStart(): Unit = webstart(webhooks)

    def receive: Receive = {
        case ok @ OK() => probe.ref ! ok
    }

    val callbackUrl = "expected"

    val webhooks =
        post {
            path(callbackUrl) {
                complete {
                    self ! OK()
                    "OK"
                }
            }
        }
}
