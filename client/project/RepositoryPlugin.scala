import java.io.FileInputStream
import java.util.Properties

import sbt.Keys._
import sbt.{AutoPlugin, Credentials, _}

object RepositoryPlugin extends AutoPlugin {

  private val repoConfig = getRepoConfig
  private val (host, resolveRepo, publishRepo, url, user, pass) = getConfig

  override lazy val projectSettings = Seq(
    credentials += Credentials("Artifactory Realm", host, user, pass),
    resolvers += "Artifactory Realm" at s"$url/$resolveRepo",
    publishTo := Some("Artifactory" at s"$url/$publishRepo"),
    publishMavenStyle := true,
    isSnapshot := true
  )

  private def getConfig = {
    (
      get("ARTIFACTORY_CONTEXT_URL"),
      "libs-release",
      "libs-release-local",
      get("ARTIFACTORY_URL"),
      get("ARTIFACTORY_USER"),
      get("ARTIFACTORY_PW")
    )
  }
  
  private def get(key: String): String = {
    sys.env.get(key).getOrElse(Option(repoConfig.getProperty(key))
      .getOrElse(throw new IllegalStateException(s"Could not find repository setting for $key")))
  }

  private def getRepoConfig: Properties = {
    val cred = Path.userHome / ".sbt" / "repo.properties"
    val prop = new Properties()
    if (cred.exists()) {
      prop.load(new FileInputStream(cred))
    }
    prop
  }
}
