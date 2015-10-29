package wii.awa

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import org.scalatest.{FlatSpec, Matchers}
import wii.awa.WebHooksSpec._
import wiii.awa.WebHooks


class WebHooksSpec extends FlatSpec with Matchers with ScalatestRouteTest with WebHooks {
    override implicit val actorSystem: ActorSystem = ActorSystem("WebHooksSpec")
    override implicit val materializer: ActorMaterializer = ActorMaterializer()

    val subscribe = s"""{"host":"http://localhost"}"""

    "WebHooks" should "register new hooks" in {
        Put("/subscribe", JSON(subscribe)) ~> webhookRoutes ~> check {
            status shouldBe OK
            println(responseAs[String])
            // todo;; contentType shouldBe `application/json`
            //responseAs[IpInfo] shouldBe ip1Info
        }
    }

    it should "provide status" in {
        Get("/status") ~> webhookRoutes ~> check {
            status shouldBe OK
            contentType shouldBe `application/json`
            println(responseAs[String])
            //responseAs[IpInfo] shouldBe ip2Info
        }
    }
}

object WebHooksSpec {
    def JSON(json: String) = HttpEntity(ContentTypes.`application/json`, json)
}
