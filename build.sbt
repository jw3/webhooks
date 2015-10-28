organization := "wiii"
name := "awebapi"
version := "0.2-SNAPSHOT"
licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion := "2.11.5"
scalacOptions += "-target:jvm-1.8"

resolvers += "jw3 at bintray" at "https://dl.bintray.com/jw3/maven"
credentials += Credentials(Path.userHome / ".bintray" / ".credentials")

libraryDependencies ++= {
    val akkaVersion = "2.4.0"
    val akkaStreamVersion = "1.0"

    Seq(
        "org.scala-lang" % "scala-reflect" % "2.11.5",

        "gpio4s" %% "gpiocfg" % "0.1",

        "io.spray" %% "spray-json" % "1.3.2",
        "com.typesafe" % "config" % "1.3.0",
        "net.ceedubs" %% "ficus" % "1.1.2",

        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Runtime,

        "org.scalatest" %% "scalatest" % "2.2.5" % Test,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
        "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamVersion % Test
    )
}
