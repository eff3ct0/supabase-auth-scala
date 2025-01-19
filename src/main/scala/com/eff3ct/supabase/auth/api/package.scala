package com.eff3ct.supabase.auth

import cats.effect.Resource
import io.circe.generic.extras.Configuration
import org.http4s.client.Client

package object api {
  type ClientR[F[_]] = Resource[F, Client[F]]

  type SupabaseAuthAPIR[F[_]] = Resource[F, SupabaseAuthAPI[F]]

  private[api] implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames
}
