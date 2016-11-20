package webhooks

import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import webhooks.models.HookConfig
import webhooks.protocols._

class ModelSpec extends FlatSpec with Matchers {
  "Hook" should "roundtrip" in {
    val hook = HookConfig("localhost")
    val json = hook.toJson
    json.convertTo[HookConfig] shouldBe hook
  }
}
