import Dependency._
import Extension._
lazy val root =
  (project in file("."))
    .enablePlugins(BuildPlugin)
    .withKindProjector
    .withBetterMonadicFor
    .withHeader
    .withSonatypePublish
    .withCoverage
    .settings(
      name := "supabase-auth",
      libraryDependencies ++=
        Seq(
          catsEffect.core,
          catsEffect.std,
          circe.generic,
          circe.parser,
          circe.genericExtras,
          jwt.circe,
          http4s.circe,
          http4s.dsl,
          http4s.emberClient,
          /** Test */
          Testing.scalaTest,
          Testing.scalaTestFlatspec,
          Testing.scalaCheck
        )
    )
