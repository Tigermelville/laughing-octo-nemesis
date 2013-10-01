import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "shorty"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "postgresql" % "postgresql" % "9.1-901.jdbc4",
    jdbc,
    anorm,
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
    "junit" % "junit" % "4.8.1" % "test"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers ++= Seq(
      DefaultMavenRepository,
      Resolver.url("Play", url("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesave/releases/"
  ))
}
