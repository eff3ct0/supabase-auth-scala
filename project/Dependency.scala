import sbt._
object Dependency {

  object http4s {
    lazy val emberServer: ModuleID = "org.http4s" %% "http4s-ember-server" % Version.Http4s
    lazy val emberClient: ModuleID = "org.http4s" %% "http4s-ember-client" % Version.Http4s
    lazy val circe: ModuleID       = "org.http4s" %% "http4s-circe"        % Version.Http4s
    lazy val dsl: ModuleID         = "org.http4s" %% "http4s-dsl"          % Version.Http4s
  }

  object catsEffect {
    lazy val core: ModuleID = "org.typelevel" %% "cats-effect"     % Version.CatsEffect
    lazy val std: ModuleID  = "org.typelevel" %% "cats-effect-std" % Version.CatsEffect
  }

  object circe {
    lazy val generic: ModuleID       = "io.circe" %% "circe-generic"        % Version.Circe
    lazy val parser: ModuleID        = "io.circe" %% "circe-parser"         % Version.Circe
    lazy val genericExtras: ModuleID = "io.circe" %% "circe-generic-extras" % "0.14.4"
  }

}
