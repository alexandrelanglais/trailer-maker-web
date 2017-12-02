name := """Trailer maker web"""
organization := "io.trailermaker"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
//.disablePlugins(PlayFilters)

scalaVersion := "2.12.4"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
// You need to publish https://github.com/alexandrelanglais/trailer-maker/
// in your local repository for this dependency to work
libraryDependencies += "io.trailermaker" %% "trailer-maker" % "0.2"
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "io.trailermaker.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "io.trailermaker.binders._"

libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.2.0"

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false
