package rxthings.webhooks

import org.scalatest.{FlatSpec, Matchers}
import rxthings.webhooks.WebHookProtocol._
import spray.json._

class ModelJsonSpec extends FlatSpec with Matchers {
    "Hook" should "roundtrip" in {
        val hook = HookConfig("localhost")
        val json = hook.toJson
        json.convertTo[HookConfig] shouldBe hook
    }
}
