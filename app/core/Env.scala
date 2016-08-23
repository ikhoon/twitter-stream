package core

import com.google.inject.Singleton

/**
 * Created by Liam.M(엄익훈) on 8/23/16.
 */
@Singleton
class Env {
  def get(key: String): Option[String] = Option(System.getProperty(key))
}
