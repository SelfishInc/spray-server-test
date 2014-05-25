import sbt._
import Keys._
import sbt.Package.ManifestAttributes


object ApplicationBuild extends Build {
  import Versions._

  val appName       = "spray-server-test"
  val isSnapshot = true
  val version = "1.0.0" + (if (isSnapshot) "-SNAPSHOT" else "")

  import com.typesafe.sbt.SbtNativePackager._
  import com.typesafe.sbt.packager.Keys._


  val nativePackSetting = packagerSettings ++ packageArchetype.java_server ++ Seq(
    maintainer := "Alexey Kardapoltsev <alexey.kardapoltsev@frumatic.com>",
    packageSummary := "spray tests",
    packageDescription := "spray server tests"
  )


  val resolvers = Seq(
    "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe Maven" at "http://repo.typesafe.com/typesafe/maven-releases",
    "Maven central" at "http://oss.sonatype.org/content/repositories/releases",
    "spray" at "http://repo.spray.io/",
    "spray nightlies repo" at "http://nightlies.spray.io/",
    "The New Motion Repository" at "http://nexus.thenewmotion.com/content/repositories/releases-public"
  )


  val buildSettings = Defaults.defaultSettings ++ nativePackSetting ++ Seq (
    organization := "self.edu",
    Keys.version := version,
    scalaVersion := scalaVer,
    scalacOptions in ThisBuild ++= Seq(
      "-feature",
//      "-Xlog-implicits",
      "-language:postfixOps",
      "-deprecation"),
    retrieveManaged := true,
    parallelExecution in Test := false,
    testOptions in Test := Nil,
    Keys.externalResolvers := Resolver.withDefaultResolvers(resolvers)
  )

  val appDependencies = Seq(
   "com.typesafe.akka"     %% "akka-actor"              % AkkaVersion,
   "com.typesafe.akka"     %% "akka-slf4j"              % AkkaVersion,
   "com.typesafe.akka"     %% "akka-testkit"            % AkkaVersion  % "test",
   "org.scalatest"         %% "scalatest"               % ScalaTestVersion % "test",
   "ch.qos.logback"        %  "logback-classic"         % LogbackVersion,
   "io.spray"              %% "spray-json"              % SprayJson,
   "io.spray"              %  "spray-can"               % SprayVersion,
   "io.spray"              %  "spray-client"            % SprayVersion,
   "io.spray"              %  "spray-routing"           % SprayVersion
  )


  val server = Project(
    "server", file("./server"),
    settings = buildSettings ++ Seq(
      mainClass := Some("self.edu.server.Server"),
      libraryDependencies ++= appDependencies
    ))
  val testing = Project(
    "testing", file("./testing"),
    settings = buildSettings ++ Seq(
      mainClass := Some("self.edu.testing.Testing"),
      libraryDependencies ++= appDependencies
    )
  ) dependsOn server

  val main = Project(
    appName,
    file(".")
  ).aggregate(server, testing)
}

object Versions {
  val LogbackVersion = "1.1.2"
  val scalaVer = "2.10.4"
  val AkkaVersion = "2.3.3"
  val SprayJson = "1.2.6"
  val SprayVersion = "1.3.1"
  val ScalaTestVersion = "2.1.7"
}
