import sbt._

object Dependencies {

  val fs2Version = "0.10.6"
  val catsVersion = "1.3.1"
  val scalaTestVersion = "3.0.5"
  val scalaCheckVersion = "1.14.0"
  lazy val CirceVersion = "0.9.0"

  lazy val cats = "org.typelevel" %% "cats-core" % catsVersion
  lazy val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % scalaCheckVersion
  lazy val circe = Seq(
    "io.circe"        %% "circe-core"       % CirceVersion,
    "io.circe"        %% "circe-generic"       % CirceVersion,
    "io.circe"        %% "circe-fs2"       % CirceVersion
  )
  lazy val fs2 = Seq(
    "co.fs2" %% "fs2-core" % fs2Version,
    "co.fs2" %% "fs2-io" % fs2Version,
  )
}
