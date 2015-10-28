package wii.awa

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}
import wii.awa.CallbackSpec._
import wiii.awa.{ActorWebApi, WebHooks}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt


class CallbackSpec extends TestKit(ActorSystem(classOf[CallbackSpec].getSimpleName.dropRight(1)))
                           with ImplicitSender with WordSpecLike with Matchers {

    implicit val materializer = ActorMaterializer()
    val clientProbe = TestProbe()
    val client = system.actorOf(Client.props(clientProbe))
    val server = system.actorOf(Props[Server])

    val subscribeUri = Uri(s"http://localhost:$serverPort/subscribe")
    val unsubscribeUri = Uri(s"http://localhost:$serverPort/unsubscribe")
    val statusUri = Uri(s"http://localhost:$serverPort/status")
    var subscription: String = _

    "client" should {
        "reigster callback" in {
            val json = s"""{"host":"localhost","port":$clientPort}"""
            val f = Http().singleRequest(HttpRequest(HttpMethods.PUT, subscribeUri, entity = HttpEntity(ContentTypes.`application/json`, json)))
            subscription = Await.result(f.map(_.entity).flatMap(stringUnmarshaller(materializer)(_)), 10 seconds)
        }
        "be called back" in {
            server ! Call()
            clientProbe.expectMsgType[OK]
        }
        "get status" in {
            val f = Http().singleRequest(HttpRequest(HttpMethods.GET, statusUri))
            val status = Await.result(f.map(_.entity).flatMap(stringUnmarshaller(materializer)(_)), 10 seconds)
            println(s"status:\n$status")
        }
        "be unsubscribed" in {
            val json = s"""{"id":"$subscription"}"""
            val f = Http().singleRequest(HttpRequest(HttpMethods.PUT, unsubscribeUri, entity = HttpEntity(ContentTypes.`application/json`, json)))
            val r = Await.result(f.map(_.entity).flatMap(stringUnmarshaller(materializer)(_)), 10 seconds)
        }
        "not be called back" in {
            server ! Call()
            clientProbe.expectNoMsg()
        }
    }
}

object CallbackSpec {
    val serverPort = 9999
    val clientPort = 8888

    case class OK()
    case class Call()

    def cfg(p: Int) = Option(ConfigFactory.parseString(s"webapi.port=$p"))
}

class Server extends Actor with ActorWebApi with WebHooks {
    override def config = cfg(serverPort)
    override def preStart(): Unit = webstart(webhooks)

    def receive: Receive = {
        case Call() => post(ConfigFactory.parseString("{}"))
    }
}

class Client(probe: TestProbe) extends Actor with ActorWebApi {
    override def config = cfg(clientPort)
    override def preStart(): Unit = webstart(webhooks)

    def receive: Receive = {
        case ok @ OK() => probe.ref ! ok
    }

    val webhooks =
        put {
            pathSingleSlash {
                complete {
                    self ! OK()
                    "OK"
                }
            }
        }
}

object Client {
    def props(probe: TestProbe) = Props(new Client(probe))
}
