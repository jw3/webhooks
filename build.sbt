organization := "com.github.jw3"
name := "webhooks"
version := "1.0.0-SNAPSHOT"
licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion := "2.11.7"
scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",

  "-feature",
  "-unchecked",
  "-deprecation",

  "-language:postfixOps",
  "-language:implicitConversions",

  "-Ywarn-unused-import",
  "-Xfatal-warnings",
  "-Xlint:_"
)

resolvers += "jw3 at bintray" at "https://dl.bintray.com/jw3/maven"

libraryDependencies ++= {
  val akkaVersion = "2.4.11"
  val scalatestVersion = "3.0.0"

  Seq(
    "com.iheart" %% "ficus" % "1.2.6",
    "com.elderresearch" %% "ssc" % "0.2.0",

    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,

    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,

    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",

    "org.scalactic" %% "scalactic" % scalatestVersion % Test,
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % Test
  )
}
