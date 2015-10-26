package wii.awa

import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import wiii.awa.HookConfig
import wiii.awa.WebHookProtocol._

class ModelJsonSpec extends FlatSpec with Matchers {
    "Hook" should "roundtrip" in {
        val hook = HookConfig("localhost")
        val json = hook.toJson
        json.convertTo[HookConfig] shouldBe hook
    }
}
