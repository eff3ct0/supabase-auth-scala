import de.heikoseeberger.sbtheader.HeaderPlugin
import sbt._

object Extension {

  implicit class ProjectOps(project: Project) {

    def withKindProjector: Project =
      project.settings(
        Seq(
          addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full)
        )
      )

    def withBetterMonadicFor: Project =
      project.settings(
        Seq(
          addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
        )
      )

    def withHeader: Project =
      project
        .enablePlugins(HeaderPlugin)
        .settings(
          Header.projectSettings
        )

    def withSonatypePublish: Project =
      project
        .settings(
          SonatypePublish.projectSettings
        )

  }

}
