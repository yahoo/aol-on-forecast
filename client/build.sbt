name := "forecast-api-client"

scalaVersion := "2.11.8"

version := "3.1.9"

organization := "com.aol.one.reporting"

publishMavenStyle := true

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

sonatypeProfileName := organization.value
useGpg := false

publishTo := {
  if (isSnapshot.value) Some(Opts.resolver.sonatypeSnapshots)
  else Some(Opts.resolver.sonatypeStaging)
}

resolvers += Resolver.mavenLocal

licenses := Seq("MIT" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/yahoo/aol-on-forecast"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/yahoo/aol-on-forecast"),
    "scm:git@github.com/yahoo/aol-on-forecast.git"
  ))

developers := List(
  Developer(
    id="One Reporting Team",
    name="One Reporting Team",
    email="noreply@oath.org",
    url=url("https://github.com/yahoo")
  ))
