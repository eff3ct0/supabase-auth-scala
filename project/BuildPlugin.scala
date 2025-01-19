import de.heikoseeberger.sbtheader.HeaderPlugin
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
object BuildPlugin extends AutoPlugin {

  override def requires: Plugins = JvmPlugin && HeaderPlugin

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    organizationName         := "supabase-auth",
    organization             := "com.eff3ct",
    scalaVersion             := Version.Scala,
    crossScalaVersions       := Vector(scalaVersion.value),
    crossVersion             := CrossVersion.binary,
    publish / skip           := true,
    run / fork               := true,
    Test / fork              := true,
    Test / parallelExecution := true,
    scalafmtOnCompile        := true,
    updateOptions := updateOptions.value
      .withCachedResolution(cachedResolution = false),
    // do not build and publish scaladocs
    Compile / doc / sources                := Seq.empty,
    Compile / packageDoc / publishArtifact := false,
    scalacOptions ++= Seq(
      "-Ymacro-annotations"
    )
  ) ++ SonatypePublish.projectSettings

}
