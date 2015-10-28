package wii.awa

import org.scalatest.{Matchers, WordSpec}
import wii.awa.InterpolatorSpec.InetAddr
import wiii.awa.Interpolator._

class InterpolatorSpec extends WordSpec with Matchers {
    "replacement" should {
        "work as expected" in {
            val addr = InetAddr("localhost", 999)
            val template = "hostname={{host}},port={{port}}"
            interpolate(template, addr) shouldBe "hostname=localhost,port=999"
        }
    }
    "empty template" should {
        "produce empty string" in {
            val addr = InetAddr("localhost", 999)
            val template = ""
            interpolate(template, addr) shouldBe empty
        }
    }
    "containing only a variable" should {
        "result in only replacement text" in {
            val addr = InetAddr("localhost", 999)
            val template = "{{host}}"
            interpolate(template, addr) shouldBe "localhost"
        }
    }
}

object InterpolatorSpec {
    case class InetAddr(host: String, port: Int)
}
