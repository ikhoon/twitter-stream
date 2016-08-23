name := "twitter-stream"

version := "1.0"

scalaVersion := "2.11.8"

lazy val `twitter-stream` =
  (project in file("."))
    .enablePlugins(PlayScala)