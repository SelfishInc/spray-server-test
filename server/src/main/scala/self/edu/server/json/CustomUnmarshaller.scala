package self.edu.server.json

import self.edu.server.http.{WorkerJsonRequest, RequestMessage}
import spray.json.{JsonParser, JsonReader}
import spray.httpx.unmarshalling.{MalformedContent, Deserialized, Deserializer, FromRequestUnmarshaller}
import spray.http.HttpRequest
import com.sun.xml.internal.ws.encoding.soap.DeserializationException

object CustomUnmarshaller {
  implicit val workerJsonRequestUM = unmarshallerFrom(WorkerJsonRequest.workerJsonRequestJF)
  
  def unmarshallerFrom[T <: RequestMessage](reader: JsonReader[T]): FromRequestUnmarshaller[T] =
    new Deserializer[HttpRequest, T] {
      override def apply(httpRequest: HttpRequest): Deserialized[T] = {
        try {
          val request = reader.read(JsonParser(httpRequest.entity.asString))
          Right(request)
        } catch {
          case e: DeserializationException =>
            Left(MalformedContent(e.getMessage, e))
        }
      }
    }
}
