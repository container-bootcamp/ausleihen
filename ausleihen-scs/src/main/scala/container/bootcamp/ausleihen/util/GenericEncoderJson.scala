package container.bootcamp.ausleihen.util

import akka.persistence.pg.JsonString
import akka.persistence.pg.event.JsonEncoder

import scala.reflect.ClassTag

/**
  * Encode / Decode the akka persistence events to write them as json into the postgres db.
  */
class GenericEncoderJson extends JsonEncoder {

  /**
    * A partial function that serializes an event to a json representation
    *
    * @return the json representation
    */
  def toJson: PartialFunction[Any, JsonString] = {
    case event => JsonString(JsonMapper.toJson(event))
  }

  /**
    * A partial function that deserializes an event from a json representation
    *
    * @return the event
    */
  def fromJson: PartialFunction[(JsonString, Class[_]), Nothing] = {
    case (json, c) =>
      JsonMapper.fromJson(json.value)(ClassTag(c))
  }
}
