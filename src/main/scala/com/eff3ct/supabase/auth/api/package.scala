package com.eff3ct.supabase.auth

import cats.effect.Resource
import io.circe.{Encoder, Json}
import io.circe.generic.extras.Configuration
import io.circe.syntax.EncoderOps
import org.http4s.Uri
import org.http4s.client.Client

package object api {
  type ClientR[F[_]] = Resource[F, Client[F]]

  type SupabaseAuthAPIR[F[_]] = Resource[F, SupabaseAuthAPI[F]]

  private[api] implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames



  implicit def asJson[T](t: T)(implicit enc: Encoder[T]): Json = t.asJson

  implicit class ImplicitURI(uri: Uri) {
    def :?(redirectTo: Option[String]): Uri =
      redirectTo.fold(uri)(redirectTo =>
        uri.withQueryParam("redirect_to", Uri.encode(redirectTo))
      )

    def :+?(params: Map[String, String]): Uri =
      params.foldLeft(uri)((acc, param) => acc.withQueryParam(param._1, param._2))
  }
}
