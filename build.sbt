import Dependency._
import Extension._
lazy val root =
  (project in file("."))
    .settings(BuildPlugin.projectSettings)
    .withKindProjector
    .withBetterMonadicFor
    .withHeader
    .withSonatypePublish
    .settings(
      name := "supabase-auth",
      libraryDependencies ++=
        Seq(
          catsEffect.core,
          catsEffect.std,
          circe.generic,
          circe.parser,
          circe.genericExtras,
          http4s.circe,
          http4s.dsl,
          http4s.emberClient
        )
    )
