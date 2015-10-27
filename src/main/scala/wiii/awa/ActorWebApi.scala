package wiii.awa

import akka.actor.Actor
import akka.stream.ActorMaterializer

trait ActorWebApi extends Actor with WebApi {
    implicit val actorSystem = context.system
    implicit val materializer = ActorMaterializer()
}
