package core


import javax.inject.Singleton

import scala.util.Try

/**
 * Created by Liam.M(엄익훈) on 8/23/16.
 */
@Singleton
class Env {
  def as[T](key: String)(implicit ep: EnvParser[T]): Option[T] = ep.parse(System.getProperty(key))
}

trait EnvParser[T] {
  def parse(s: String) : Option[T]
}


object EnvParser {
  implicit val intEnvParser = new EnvParser[Int] {
    override def parse(s: String): Option[Int] = Try { s.toInt }.toOption
  }
  implicit val stringEnvParser = new EnvParser[String] {
    override def parse(s: String): Option[String] = Option(s)
  }

  implicit val booleanEnvParser = new EnvParser[Boolean] {
    override def parse(s: String): Option[Boolean] = Try { s.toBoolean }.toOption
  }
}
