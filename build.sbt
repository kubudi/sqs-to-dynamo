resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "2.4.15" % "test",
  "com.typesafe" % "config" % "1.2.1",
  "com.github.seratch" %% "awscala" % "0.4.+",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2",
  "com.typesafe.play" % "play-json_2.11" % "2.4.0-M2"
)

scalaVersion := "2.11.5"