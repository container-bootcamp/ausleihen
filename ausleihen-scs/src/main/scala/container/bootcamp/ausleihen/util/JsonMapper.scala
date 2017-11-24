package container.bootcamp.ausleihen.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

/**
  * Provide serialization and deserialization for json
  */
object JsonMapper {

  import scala.reflect._

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  def fromJson[T: ClassTag](json: String): T = {
    mapper.readValue(json, classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }

  def toJson[T](o: T): String = mapper.writeValueAsString(o)

}

