package webhooks

import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import webhooks.models.HookConfigOpt
import webhooks.protocols._

class ModelSpec extends FlatSpec with Matchers {
  "Hook" should "roundtrip" in {
    val hook = HookConfigOpt("localhost")
    val json = hook.toJson
    json.convertTo[HookConfigOpt] shouldBe hook
  }
}
