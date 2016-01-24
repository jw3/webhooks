package rxthings.webhooks

import akka.actor.Actor
import akka.stream.ActorMaterializer

/**
 * Mixin the [[WebApi]] to an [[Actor]]
 * Conveniently provides assignment of the ActorSystem and ActorMaterializer
 */
trait ActorWebApi extends Actor with WebApi {
    implicit val actorSystem = context.system
    implicit val materializer = ActorMaterializer()
}
