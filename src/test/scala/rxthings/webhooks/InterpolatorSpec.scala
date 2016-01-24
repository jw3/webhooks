package rxthings.webhooks

import org.scalatest.{Matchers, WordSpec}
import rxthings.webhooks.Interpolator._
import rxthings.webhooks.InterpolatorSpec._

class InterpolatorSpec extends WordSpec with Matchers {

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
}

object InterpolatorSpec {
    case class NoName()
    case class HasName(name: String)
    case class HasFirstAndLastName(fname: String, lname: String)
}
