name := """play-bank-account-api"""
organization := "com.example"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"
version := "2.8.8"
val scalikejdbcVersion = "3.5.0"
val circeVersion = "0.14.1"
val circeValidationVersion = "0.1.0"

libraryDependencies ++= Seq(
  guice,
  jdbc,
  evolutions,
  filters,
  "com.h2database" % "h2" % "1.4.192",
  "mysql" % "mysql-connector-java" % "8.0.26",
  "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbcVersion,
  //  "org.scalikejdbc" %% "scalikejdbc-jsr310" % scalikejdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.8.0-scalikejdbc-3.5",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "com.dripower" %% "play-circe" % "2814.1"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
