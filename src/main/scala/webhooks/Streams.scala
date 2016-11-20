package webhooks

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import spray.json.DeserializationException

import scala.concurrent.Future
import scala.reflect._


object streams {
  type Connection = Flow[HttpRequest, HttpResponse, _]
  type RequestBuilder = HttpRequest => Future[HttpRequest]


  /**
   * create a http connection to the host and port as a Flow[HttpRequest, HttpResponse, ]
   */
  def connection(host: String, port: Int = 8080, ssl: Boolean = false)(implicit system: ActorSystem): Connection = {
    if (ssl) Http().outgoingConnectionHttps(host, port)
    else Http().outgoingConnection(host, port)
  }


  /**
   * Create a GET HttpRequest as a Source
   */
  def get(path: String)(implicit system: ActorSystem): Source[HttpRequest, NotUsed] = {
    request(path)(r => Future.successful(r.withMethod(HttpMethods.GET)))
  }


  /**
   * Create a POST HttpRequest as a Source
   */
  def post(path: String)(implicit system: ActorSystem): Source[HttpRequest, NotUsed] = {
    request(path)(r => Future.successful(r.withMethod(HttpMethods.POST)))
  }


  /**
   * Create a PUT HttpRequest as a Source
   */
  def put(path: String)(implicit system: ActorSystem): Source[HttpRequest, NotUsed] = {
    request(path)(r => Future.successful(r.withMethod(HttpMethods.PUT)))
  }


  /**
   * Create a DELETE HttpRequest as a Source
   */
  def delete(path: String)(implicit system: ActorSystem): Source[HttpRequest, NotUsed] = {
    request(path)(r => Future.successful(r.withMethod(HttpMethods.DELETE)))
  }


  /**
   * Create a HttpRequest as a Source with access to modify its construction
   */
  def request(path: String)(builder: RequestBuilder)(implicit system: ActorSystem): Source[HttpRequest, NotUsed] = {
    Source.fromFuture(builder(HttpRequest(uri = path)))
  }


  /**
   * extract a HttpResponse from the Stream
   */
  def unmarshal[T: ClassTag](implicit system: ActorSystem, mat: ActorMaterializer, um: Unmarshaller[HttpResponse, T]): Flow[HttpResponse, Either[StatusCode, T], _] = {
    import system.dispatcher
    Flow[HttpResponse].mapAsync(1) {
      case r@HttpResponse(StatusCodes.OK, _, _, _) ⇒
        Unmarshal(r).to[T].map(Right(_)) recover {
          case ex@DeserializationException(_, _, _) ⇒
            system.log.error("failed to deserialize response to a {}, {}", classTag[T], ex.msg)
            Left(StatusCodes.UnprocessableEntity)
          case t ⇒
            system.log.error("failed to process response as a {}, {}", classTag[T], t.getMessage)
            Left(StatusCodes.InternalServerError)
        }

      case HttpResponse(c, _, _, _) =>
        system.log.error("failed to process response as a {}", classTag[T])
        Future.successful(Left(c))
    }
  }
}
