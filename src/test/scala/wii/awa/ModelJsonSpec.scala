package wii.awa

import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import wiii.awa.Hook
import wiii.awa.WebHookProtocol._

class ModelJsonSpec extends FlatSpec with Matchers {
    "Hook" should "roundtrip" in {
        val hook = Hook("localhost")
        hook.toJson.convertTo[Hook] shouldBe hook
    }
}
