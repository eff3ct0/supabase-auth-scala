import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype._
import xerial.sbt.Sonatype.autoImport._

import scala.collection.Seq

object SonatypePublish {

  def projectSettings: Seq[Setting[_]] = Seq(
    ThisBuild / publish / skip         := false,
    ThisBuild / versionScheme          := Some("early-semver"),
    ThisBuild / sonatypeCredentialHost := sonatypeCentralHost,
    ThisBuild / organization           := "com.eff3ct",
    ThisBuild / organizationName       := "eff3ct",
    ThisBuild / homepage               := Some(url("https://github.com/eff3ct0/supabase-auth")),
    ThisBuild / licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    ThisBuild / scmInfo := Some(
      ScmInfo(
        browseUrl = url("https://github.com/eff3ct0/supabase-auth"),
        connection = "scm:git:git@github.com:eff3ct0/supabase-auth.git"
      )
    ),
    ThisBuild / developers := List(
      Developer(
        id = "rafafrdz",
        name = "Rafael Fernandez",
        email = "hi@rafaelfernandez.dev",
        url = url("https://rafaelfernandez.dev")
      )
    ),
    ThisBuild / sonatypeProjectHosting := Some(
      GitHubHosting("eff3ct0", "supabase-auth", "hi@rafaelfernandez.dev")
    )
  )

}
