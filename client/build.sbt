name := "forecast-api-client"

scalaVersion := "2.11.8"

version := "3.0.0"

organization := "com.aol.one.reporting"

enablePlugins(RepositoryPlugin)

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.3.5",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "com.typesafe" % "config" % "1.3.0",

  // test
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "org.powermock" % "powermock-api-mockito" % "1.6.4" % Test,
  "junit" % "junit" % "4.12" % Test
)

// code coverage
coverageEnabled in(Test, compile) := true
coverageEnabled in(Compile, compile) := false
coverageFailOnMinimum := true
