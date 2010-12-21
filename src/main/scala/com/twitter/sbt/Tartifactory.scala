package com.twitter.sbt

import java.io.File
import scala.collection.jcl
import _root_.sbt._

trait Tartifactory {
  def artifactoryRoot = "http://artifactory.local.twitter.com"
  def proxyRepo = "repo"
  def snapshotDeployRepo = "libs-snapshots-local"
  def releaseDeployRepo = "libs-releases-local"
}

trait TartifactoryPublisher extends BasicManagedProject with Tartifactory { self: DefaultProject =>
  override def managedStyle = ManagedStyle.Maven
  Credentials(Path.userHome / ".ivy2" / "twitter-credentials", log)
  val publishTo = if (version.toString.endsWith("SNAPSHOT")) {
    "Twitter Artifactory" at (artifactoryRoot + "/" + snapshotDeployRepo)
  } else {
    "Twitter Artifactory" at (artifactoryRoot + "/" + releaseDeployRepo)
  }
}

trait TartifactoryRepos extends BasicManagedProject with Tartifactory { self: DefaultProject =>
  private val tartEnv = jcl.Map(System.getenv())
  def internalRepos = List("artifactory.remote" at (artifactoryRoot + "/" + proxyRepo))
  def externalRepos = List(
    "ibiblio" at "http://mirrors.ibiblio.org/pub/mirrors/maven2/",
    "twitter.com" at "http://maven.twttr.com/",
    "powermock-api" at "http://powermock.googlecode.com/svn/repo/",
    "scala-tools.org" at "http://scala-tools.org/repo-releases/",
    "testing.scala-tools.org" at "http://scala-tools.org/repo-releases/testing/",
    "oauth.net" at "http://oauth.googlecode.com/svn/code/maven",
    "download.java.net" at "http://download.java.net/maven/2/",
    "atlassian" at "https://m2proxy.atlassian.com/repository/public/",
    "jboss" at "http://repository.jboss.org/nexus/content/groups/public/")

  def useArtifactory = tartEnv.get("ARTIFACTORY_TWITTER") match {
    case Some(v) => v != "false"
    case _ => true
  }

  override def repositories = {
    val projectRepos = if (useArtifactory) {
      internalRepos ++ super.repositories
    } else {
      externalRepos ++ super.repositories
    }
    Set(projectRepos:_*)
  }

  override def ivyRepositories = {
    if (useArtifactory) {
      Seq(Resolver.defaultLocal(None)) ++ repositories.toList
    } else {
      super.ivyRepositories
    }
  }
}
