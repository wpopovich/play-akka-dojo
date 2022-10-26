name := """play-akka-dojo"""
organization := "com.tiendanube"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"


val AkkaVersion = "2.6.19"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.4" % Test


libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion
libraryDependencies +="com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % Test
libraryDependencies +="net.codingwell" %% "scala-guice" % "5.1.0"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.tiendanube.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.tiendanube.binders._"
