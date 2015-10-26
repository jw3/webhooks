package wiii.awa

import java.util.UUID

case class HookSubscription(id: UUID, config: HookConfig)

case class HookConfig(host: String,
                port: Int = 8080,
                path: String = "/",
                body: String = "",
                span: String = "NONE", // "2x" | "30s" | "2x|30s"
                method: String = "PUT")
