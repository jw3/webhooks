package wiii.awa

import java.util.UUID


case class Hook(id: UUID,
                host: String,
                port: Int,
                path: String,
                body: String,
                span: String, // "2x" | "30s" | "2x|30s"
                method: String)

object Hook {
    def apply(host: String,
              port: Int = 8080,
              path: String = "/",
              body: String = "",
              span: String = "NONE",
              method: String = "PUT"): Hook = Hook(UUID.randomUUID, host, port, path, body, span, method)
}
