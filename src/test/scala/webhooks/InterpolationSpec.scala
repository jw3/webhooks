package webhooks

import org.scalatest.{Matchers, WordSpec}
import webhooks.InterpolationSpec.{HasFirstAndLastName, HasName, NoName}
import webhooks.Interpolator._


class InterpolationSpec extends WordSpec with Matchers {
  val clientPort = 8080
  val uri = ""

  "replacement" should {
    "work as expected" in {
      val template = "name={{name}}"
      interpolate(template, HasName("Duffman")) shouldBe "name=Duffman"
    }
  }
  "empty template" should {
    "produce empty string" in {
      val template = ""
      interpolate(template, HasName("Duffman")) shouldBe empty
    }
  }
  "plain text template" should {
    "be unchanged" in {
      val template = "some plain text"
      interpolate(template, HasName("Duffman")) shouldBe template
    }
  }
  "template with one variable" should {
    "replace lone variable" in {
      val template = "{{name}}"
      interpolate(template, HasName("Duffman")) shouldBe "Duffman"
    }
    "result in empty string when variable is not available" in {
      val template = "{{name}}"
      interpolate(template, NoName()) shouldBe empty
    }
    "replace two variables" in {
      val template = "{{fname}} {{lname}}"
      interpolate(template, HasFirstAndLastName("Lionel", "Hutz")) shouldBe "Lionel Hutz"
    }
  }


  //  "interpolation" should {
  //    "received formatted content" in {
  //      {
  //        val json = s"""{"host":"http://localhost","port":$clientPort,"body":"{ \\"name\\":\\"{{name}}\\" }" }"""
  //        val f = Http().singleRequest(HttpRequest(HttpMethods.PUT, uri, entity = HttpEntity(ContentTypes.`application/json`, json)))
  //        subscription = Await.result(f.map(_.entity).flatMap(stringUnmarshaller(_)), 10 seconds)
  //      }
  //      val expected = UUID.randomUUID.toString
  //      server ! CallWithName(expected)
  //      clientProbe.expectMsgPF(10 seconds) {
  //        case OKWithName(name) if name == expected =>
  //      }
  //
  //      {
  //        val json = s"""{"id":"$subscription"}"""
  //        val f = Http().singleRequest(HttpRequest(HttpMethods.DELETE, uri, entity = HttpEntity(ContentTypes.`application/json`, json)))
  //        val r = Await.result(f.map(_.entity).flatMap(stringUnmarshaller(_)), 10 seconds)
  //      }
  //    }
  //  }
}

object InterpolationSpec {
  case class NoName()
  case class HasName(name: String)
  case class HasFirstAndLastName(fname: String, lname: String)
}
