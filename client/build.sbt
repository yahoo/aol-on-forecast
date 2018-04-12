name := "forecast-api-client"

scalaVersion := "2.11.8"

version := "3.1.0"

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

useGpg := false
usePgpKeyHex("9CE03909AE4C7C0A")
pgpPublicRing := baseDirectory.value / "project" / ".gnupg" / "pubring.gpg"
pgpSecretRing := baseDirectory.value / "project" / ".gnupg" / "secring.gpg"
pgpPassphrase := sys.env.get("PGP_PASS").map(_.toArray)


sonatypeProfileName := organization.value

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  sys.env.getOrElse("SONATYPE_USER", ""),
  sys.env.getOrElse("SONATYPE_PASS", "")
)

isSnapshot := version.value endsWith "SNAPSHOT"

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

licenses := Seq("MIT" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/vidible/aol-on-forecast"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/vidible/aol-on-forecast"),
    "scm:git@github.com/vidible/aol-on-forecast.git"
  ))

developers := List(
  Developer(
    id="One Reporting Team",
    name="One Reporting Team",
    email="noreply@oath.org",
    url=url("https://github.com/vidible")
  ))
