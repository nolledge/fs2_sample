import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "de.cnolle",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "esailors-case-study",
    libraryDependencies ++= Seq(
      cats,
      scalaTest % Test,
      scalaCheck % Test
    )
  )

libraryDependencies ++= fs2
libraryDependencies ++= circe

scalacOptions := Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification"
)
